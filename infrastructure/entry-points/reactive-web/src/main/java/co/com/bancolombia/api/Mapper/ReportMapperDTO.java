package co.com.bancolombia.api.Mapper;

import co.com.bancolombia.api.dto.ResponseReportDTO;
import co.com.bancolombia.model.Report;

public class ReportMapperDTO {
    private ReportMapperDTO() {}

    public static ResponseReportDTO toDto(Report reporte) {
        if (reporte == null) return null;
        return new ResponseReportDTO(
                reporte.getMetrica(),
                reporte.getValor() != null ? reporte.getValor().toPlainString() : null
        );
    }
}
