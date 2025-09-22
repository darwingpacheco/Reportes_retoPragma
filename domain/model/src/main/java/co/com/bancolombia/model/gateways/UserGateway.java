package co.com.bancolombia.model.gateways;

import co.com.bancolombia.model.responseToken.ValidationResponse;
import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<ValidationResponse> validateToken(String token);
}
