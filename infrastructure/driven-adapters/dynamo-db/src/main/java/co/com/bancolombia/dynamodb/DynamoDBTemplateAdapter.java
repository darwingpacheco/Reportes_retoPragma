package co.com.bancolombia.dynamodb;

import co.com.bancolombia.dynamodb.helper.TemplateAdapterOperations;
import co.com.bancolombia.model.Report;
import co.com.bancolombia.model.gateways.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Repository
@Slf4j
public class DynamoDBTemplateAdapter implements ReportRepository {

    private final DynamoDbAsyncClient ddb;
    private final DynamoDbEnhancedAsyncClient enhanced;
    private final ObjectMapper mapper;
    private final String tableName;
    private final String indexName;
    private final String singlePk;

    public DynamoDBTemplateAdapter(
            DynamoDbAsyncClient ddb,
            DynamoDbEnhancedAsyncClient enhanced,
            ObjectMapper mapper,
            @Value("${app.dynamo.table}") String table,
            @Value("${app.dynamo.gsi:}") String gsi,
            @Value("${app.dynamo.pk}") String pk
    ) {
        this.ddb = ddb;
        this.enhanced = enhanced;
        this.mapper = mapper;
        this.tableName = table;
        this.indexName = gsi;
        this.singlePk = pk;
    }

    private DynamoDbAsyncTable<ModelEntity> table() {
        return enhanced.table(tableName, TableSchema.fromBean(ModelEntity.class));
    }

    @Override
    public Mono<Void> actualizarReporte(BigDecimal monto) {
        if (monto == null) {
            log.error("[DynamoDB] actualizarReporte falló: monto=null");
            return Mono.error(new IllegalArgumentException("approvedAmountCents no puede ser null"));
        }

        log.info("[DynamoDB] Actualizando reporte en tabla={} pk={} monto={}", tableName, singlePk, monto);

        var key = Map.of("reportId", AttributeValue.builder().s(singlePk).build());

        var req = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression(
                        "SET " +
                                "#count = if_not_exists(#count, :zero) + :one, " +
                                "#total = if_not_exists(#total, :zeroDec) + :amount, " +
                                "#updatedAt = :now"
                )
                .expressionAttributeNames(Map.of(
                        "#count", "count",
                        "#total", "totalAmountCents",
                        "#updatedAt", "updatedAt"
                ))
                .expressionAttributeValues(Map.of(
                        ":zero",   AttributeValue.builder().n("0").build(),
                        ":one",    AttributeValue.builder().n("1").build(),
                        ":zeroDec",AttributeValue.builder().n("0").build(),
                        ":amount", AttributeValue.builder().n(monto.stripTrailingZeros().toPlainString()).build(),
                        ":now",    AttributeValue.builder().s(java.time.Instant.now().toString()).build()
                ))
                .build();

        log.debug("[DynamoDB] UpdateItemRequest={}", req);

        return Mono.fromFuture(ddb.updateItem(req))
                .doOnSuccess(resp -> log.info("[DynamoDB] Reporte actualizado con éxito. pk={}", singlePk))
                .doOnError(e -> log.error("[DynamoDB] Error actualizando reporte pk={} error={}", singlePk, e.toString()))
                .then();
    }

    @Override
    public Mono<Report> obtenerReporte() {
        Key key = Key.builder().partitionValue(singlePk).build();

        log.info("[DynamoDB] Consultando reporte en tabla={} pk={}", tableName, singlePk);

        return Mono.fromFuture(table().getItem(r -> r.key(key).consistentRead(true)))
                .doOnNext(item -> {
                    if (item == null) {
                        log.warn("[DynamoDB] No se encontró reporte con pk={}", singlePk);
                    } else {
                        log.debug("[DynamoDB] Item recibido: {}", item);
                    }
                })
                .flatMap(e -> {
                    if (e == null) return Mono.empty();
                    Report reporte = Report.builder()
                            .metrica(e.getCount() == null ? null : String.valueOf(e.getCount()))
                            .valor(e.getTotalAmountCents())
                            .build();
                    log.info("[DynamoDB] Reporte obtenido: metrica={}, valor={}", reporte.getMetrica(), reporte.getValor());
                    return Mono.just(reporte);
                })
                .doOnError(e -> log.error("[DynamoDB] Error obteniendo reporte pk={} error={}", singlePk, e.toString()));
    }
}
