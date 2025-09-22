package co.com.bancolombia.usecase;

import co.com.bancolombia.model.Report;
import co.com.bancolombia.model.exceptionApi.ApiError;
import co.com.bancolombia.model.gateways.ReportRepository;
import co.com.bancolombia.model.gateways.UserGateway;
import co.com.bancolombia.usecase.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
public class ReportUseCase {

    private final ReportRepository repository;
    private final UserGateway userGateway;

    public Mono<Void> actualizarReporte(BigDecimal monto) {
        return repository.actualizarReporte(monto);
    }

    public Mono<Report> obtenerReporte(String token) {
        return userGateway.validateToken(token)
                .then(repository.obtenerReporte());

    }
}
