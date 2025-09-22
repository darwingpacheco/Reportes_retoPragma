package co.com.bancolombia.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Report {
    private String metrica;
    private BigDecimal valor;
}
