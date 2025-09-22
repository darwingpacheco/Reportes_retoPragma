package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.ResponseReportDTO;
import co.com.bancolombia.model.Report;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/reportes",
                    produces = { "application/json" },
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenGETReports",
                    operation = @Operation(
                            operationId = "getReport",
                            summary = "Consultar reporte consolidado de préstamos",
                            description = "Este endpoint permite obtener el reporte acumulado de préstamos procesados con el monto total prestado. Requiere autenticación mediante un token JWT válido en el encabezado 'Authorization'.",
                            tags = { "Reportes" },
                            parameters = {
                                    @Parameter(
                                            name = "Authorization",
                                            in = ParameterIn.HEADER,
                                            required = true,
                                            description = "Token JWT para autenticación. Formato: Bearer {token}",
                                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Reporte obtenido exitosamente",
                                            content = @Content(schema = @Schema(implementation = ResponseReportDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "No tiene permisos para acceder a este recurso",
                                            content = @Content(
                                                    schema = @Schema(example = "{\n" +
                                                            "    \"error\": \"Forbidden\",\n" +
                                                            "    \"message\": \"No tiene permisos para acceder a este recurso\",\n" +
                                                            "    \"status\": 403\n" +
                                                            "}")
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "409",
                                            description = "Token no válido",
                                            content = @Content(
                                                    schema = @Schema(example = "{\n" +
                                                            "    \"error\": \"Conflict\",\n" +
                                                            "    \"message\": \"Token no valido\",\n" +
                                                            "    \"status\": 409\n" +
                                                            "}")
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal Sever Error",
                                            content = @Content(
                                                    schema = @Schema(example = "{\n" +
                                                            "    \"error\": \"Internal Server Error\",\n" +
                                                            "    \"message\": \"Ha ocurrido un error interno en el servidor\",\n" +
                                                            "    \"status\": 500\n" +
                                                            "}")
                                            )
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/reportes"), handler::listenGETReports);
    }
}
