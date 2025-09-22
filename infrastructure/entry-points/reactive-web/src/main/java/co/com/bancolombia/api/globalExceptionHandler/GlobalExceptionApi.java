package co.com.bancolombia.api.globalExceptionHandler;

import co.com.bancolombia.model.exceptionApi.ApiError;
import co.com.bancolombia.usecase.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionApi {

    @ExceptionHandler(CustomException.class)
    public Mono<ResponseEntity<ApiError>> handleCustomException(CustomException ex) {
        ApiError apiError = ex.getApiError();
        return Mono.just(ResponseEntity
                .status(apiError.getStatus())
                .body(apiError));
    }
}
