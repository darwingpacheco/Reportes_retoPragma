package co.com.bancolombia.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.math.BigDecimal;

@DynamoDbBean
public class ModelEntity {
    private String reportId;
    private Long count;
    private BigDecimal totalAmountCents;
    private String updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbSecondaryPartitionKey(indexNames = "metricId-updatedAt-index")
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    @DynamoDbAttribute("count")
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    @DynamoDbAttribute("totalAmountCents")
    public BigDecimal getTotalAmountCents() { return totalAmountCents; }
    public void setTotalAmountCents(BigDecimal totalAmountCents) { this.totalAmountCents = totalAmountCents; }

    @DynamoDbSecondarySortKey(indexNames = "metricId-updatedAt-index")
    @DynamoDbAttribute("updatedAt")
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}