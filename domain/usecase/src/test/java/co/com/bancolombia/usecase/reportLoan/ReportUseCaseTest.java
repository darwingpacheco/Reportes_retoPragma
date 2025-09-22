package co.com.bancolombia.usecase.reportLoan;

import co.com.bancolombia.model.Report;
import co.com.bancolombia.model.exceptionApi.ApiError;
import co.com.bancolombia.model.gateways.ReportRepository;
import co.com.bancolombia.model.gateways.UserGateway;
import co.com.bancolombia.usecase.ReportUseCase;
import co.com.bancolombia.usecase.exception.ConflictException;
import co.com.bancolombia.usecase.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportUseCaseTest {
    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private ReportUseCase reportUseCase;

    private final BigDecimal validMonto = BigDecimal.valueOf(500000);

    @BeforeEach
    void setUp() {

    }

    @Test
    void actualizarReporte_debeEjecutarseConExito() {
        when(reportRepository.actualizarReporte(validMonto)).thenReturn(Mono.empty());

        StepVerifier.create(reportUseCase.actualizarReporte(validMonto))
                .verifyComplete();

        verify(reportRepository).actualizarReporte(validMonto);
    }

    @Test
    void obtenerReporte_tokenValido_retornaReporte() {
        String token = "Bearer some-valid-token";
        Report reporte = Report.builder()
                .metrica("10")
                .valor(BigDecimal.valueOf(1000000))
                .build();

        when(userGateway.validateToken(token)).thenReturn(Mono.empty());
        when(reportRepository.obtenerReporte()).thenReturn(Mono.just(reporte));

        StepVerifier.create(reportUseCase.obtenerReporte(token))
                .expectNextMatches(r -> r.getMetrica().equals("10") &&
                        r.getValor().compareTo(BigDecimal.valueOf(1000000)) == 0)
                .verifyComplete();

        verify(userGateway).validateToken(token);
        verify(reportRepository).obtenerReporte();
    }

    @Test
    void obtenerReporte_tokenInvalido_debeRetornarError() {
        String token = "Bearer invalid-token";

        ApiError apiError = new ApiError();
        apiError.setStatus(401);
        apiError.setMessage("Token inválido");
        apiError.setError("Unauthorized");

        CustomException exception = new CustomException(apiError);

        // Simula error en token
        when(userGateway.validateToken(eq(token))).thenReturn(Mono.error(exception));

        // Muy importante: evitar que repository.obtenerReporte() sea null
        when(reportRepository.obtenerReporte()).thenReturn(Mono.just(new Report())); // <- necesario aunque no se llame

        StepVerifier.create(reportUseCase.obtenerReporte(token))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof CustomException);
                    assertEquals("Token inválido", error.getMessage());
                })
                .verify();

        verify(userGateway).validateToken(token);
    }

}