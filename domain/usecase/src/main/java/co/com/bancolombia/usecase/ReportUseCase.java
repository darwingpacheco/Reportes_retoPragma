package co.com.bancolombia.usecase;

import co.com.bancolombia.model.Report;
import co.com.bancolombia.model.gateways.ReportRepository;
import co.com.bancolombia.model.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ReportUseCase {

    private final ReportRepository repository;
    private final UserGateway userGateway;

    public Mono<Void> updateReport(BigDecimal amount) {
        return repository.updateReportDynamo(amount);
    }

    public Mono<Report> obtainReport(String token) {
        return userGateway.validateToken(token)
                .then(repository.obtainReportDynamo());
    }
}
