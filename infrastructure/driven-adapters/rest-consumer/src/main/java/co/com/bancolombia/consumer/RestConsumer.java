package co.com.bancolombia.consumer;

import co.com.bancolombia.model.exceptionApi.ApiError;
import co.com.bancolombia.model.gateways.UserGateway;
import co.com.bancolombia.model.responseToken.ValidationResponse;
import co.com.bancolombia.usecase.exception.CustomException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RestConsumer implements UserGateway {
    private final WebClient client;

    @Override
    public Mono<ValidationResponse> validateToken(String token) {
        return client.get()
                .uri("http://localhost:8081/api/v1/validateToken/reports")
                .header(HttpHeaders.AUTHORIZATION, token)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Map.class)
                                .defaultIfEmpty(Map.of())
                                .map(body -> new ValidationResponse(response.statusCode().value(),
                                        (String) body.getOrDefault("message", "")));
                    } else {
                        return response.bodyToMono(Map.class)
                                .defaultIfEmpty(Map.of())
                                .flatMap(body -> {
                                    ApiError apiError = new ApiError();
                                    apiError.setStatus(response.statusCode().value());
                                    String authMessage = (String) body.getOrDefault("message", "");
                                    apiError.setMessage(authMessage);
                                    apiError.setError((String) body.getOrDefault("error", "Undefined"));

                                    return Mono.error(new CustomException(apiError));
                                });
                    }
                });
    }
}
