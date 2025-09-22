package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.model.Report;
import co.com.bancolombia.usecase.ReportUseCase;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.math.BigDecimal;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ReportUseCase reportUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        String body = message.body();
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                    .build();
            JsonNode root = mapper.readTree(body);

            String metrical = root.path("metrical").asText(null);
            BigDecimal amountApproved = root.path("amount").isMissingNode() || root.path("amount").isNull()
                    ? null
                    : new BigDecimal(root.path("amount").asText());

            if (metrical == null || amountApproved == null)
                return Mono.error(new IllegalArgumentException("Payload inválido: se requiere 'metrical' y 'monto'"));

            Report report = Report.builder()
                    .metrica(metrical)
                    .valor(amountApproved)
                    .build();

            return reportUseCase.updateReport(report.getValor()).then();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
