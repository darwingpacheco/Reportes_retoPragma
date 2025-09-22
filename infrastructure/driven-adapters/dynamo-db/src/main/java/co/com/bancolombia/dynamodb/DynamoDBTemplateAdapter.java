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
    private final String singlePk;

    public DynamoDBTemplateAdapter(
            DynamoDbAsyncClient ddb,
            DynamoDbEnhancedAsyncClient enhanced,
            ObjectMapper mapper,
            @Value("${app.dynamo.table}") String table,
            @Value("${app.dynamo.pk}") String pk
    ) {
        this.ddb = ddb;
        this.enhanced = enhanced;
        this.mapper = mapper;
        this.tableName = table;
        this.singlePk = pk;
    }

    private DynamoDbAsyncTable<ModelEntity> table() {
        return enhanced.table(tableName, TableSchema.fromBean(ModelEntity.class));
    }

    @Override
    public Mono<Void> updateReportDynamo(BigDecimal amount) {
        if (amount == null) {
            log.error("fallo actualización de reporte en dynamo: amount=null");
            return Mono.error(new IllegalArgumentException("approvedAmountCents no puede ser null"));
        }

        log.info("Se procede a actualizar reporte en tabla={} pk={} amount={}", tableName, singlePk, amount);

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
                        ":amount", AttributeValue.builder().n(amount.stripTrailingZeros().toPlainString()).build(),
                        ":now",    AttributeValue.builder().s(java.time.Instant.now().toString()).build()
                ))
                .build();

        log.debug("UpdateItemRequest={}", req);

        return Mono.fromFuture(ddb.updateItem(req))
                .doOnSuccess(resp -> log.info("Reporte actualizado con exito en DynamoDB. pk={}", singlePk))
                .doOnError(e -> log.error("Error actualizando reporte pk={} error={}", singlePk, e.toString()))
                .then();
    }

    @Override
    public Mono<Report> obtainReportDynamo() {
        Key key = Key.builder().partitionValue(singlePk).build();

        log.info("Se pasa a traer reporte en Dynamo con tabla={} pk={}", tableName, singlePk);

        return Mono.fromFuture(table().getItem(r -> r.key(key).consistentRead(true)))
                .doOnNext(item -> {
                    if (item == null)
                        log.warn("No se encontró reporte con pk={}", singlePk);
                    else
                        log.debug("Item recibido: {}", item);
                })
                .flatMap(e -> {
                    if (e == null)
                        return Mono.empty();
                    Report reportObtain = Report.builder()
                            .metrica(e.getCount() == null ? "" : String.valueOf(e.getCount()))
                            .valor(e.getTotalAmountCents())
                            .build();
                    log.info("Reporte obtenido: metrica={}, valor={}", reportObtain.getMetrica(), reportObtain.getValor());
                    return Mono.just(reportObtain);
                })
                .doOnError(e -> log.error("Error obteniendo reporte pk={} error={}", singlePk, e.toString()));
    }
}
