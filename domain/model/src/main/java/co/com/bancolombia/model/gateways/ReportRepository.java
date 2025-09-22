package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.Report;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ReportRepository {
    Mono<Void> actualizarReporte(BigDecimal monto);
    Mono<Report> obtenerReporte();
}
