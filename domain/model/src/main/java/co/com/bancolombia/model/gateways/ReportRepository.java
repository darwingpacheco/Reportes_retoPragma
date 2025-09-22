package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.Report;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ReportRepository {
    Mono<Void> updateReportDynamo(BigDecimal amount);
    Mono<Report> obtainReportDynamo();
}
