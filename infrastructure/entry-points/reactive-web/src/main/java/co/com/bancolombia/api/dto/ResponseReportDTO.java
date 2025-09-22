package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseReportDTO {

    @Schema(description = "Total de aprobaciones", example = "2")
    private String totalApprovals;

    @Schema(description = "Monto total", example = "10000000")
    private String totalAmount;

}
