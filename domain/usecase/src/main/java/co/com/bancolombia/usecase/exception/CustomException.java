package co.com.bancolombia.usecase.exception;


import co.com.bancolombia.model.exceptionApi.ApiError;

public class CustomException extends RuntimeException {
    private final ApiError apiError;

    public CustomException(ApiError apiError) {
        super(apiError.getMessage());
        this.apiError = apiError;
    }

    public ApiError getApiError() {
        return apiError;
    }
}
