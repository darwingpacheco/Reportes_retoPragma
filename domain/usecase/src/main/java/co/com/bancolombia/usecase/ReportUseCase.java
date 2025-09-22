package co.com.bancolombia.usecase;

import co.com.bancolombia.model.Report;
import co.com.bancolombia.model.gateways.ReportRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ReportUseCase {

    private final ReportRepository repository;

    public Mono<Void> actualizarReporte(BigDecimal monto) {
        return repository.actualizarReporte(monto);
    }

    public Mono<Report> obtenerReporte() {
        return repository.obtenerReporte();
    }
}
