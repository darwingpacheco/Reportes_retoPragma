package co.com.bancolombia.api;

import co.com.bancolombia.api.Mapper.ReportMapperDTO;
import co.com.bancolombia.usecase.ReportUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final ReportUseCase reportUseCase;
    public Mono<ServerResponse> listenGETReports(ServerRequest serverRequest) {
        log.info("Se ingresa en listenGETReport");

        String token = serverRequest.headers().firstHeader("Authorization");

        return reportUseCase.obtainReport(token)
                .map(ReportMapperDTO::toDto)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .switchIfEmpty(ServerResponse.noContent().build());
    }

}
