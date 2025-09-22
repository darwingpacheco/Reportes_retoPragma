package co.com.bancolombia.dynamodb.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;
import java.util.Optional;

@Configuration
public class DynamoDBConfig {

    @Bean
    public DynamoDbAsyncClient amazonDynamoDB(@Value("${aws.region}") String region,
                                              @Value("${aws.dynamodb.endpoint:}") Optional<String> endpoint,
                                              ObjectProvider<MetricPublisher> publisher) {
        var builder = DynamoDbAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        endpoint.filter(s -> !s.isBlank())
                .ifPresent(uri -> builder.endpointOverride(URI.create(uri)));

        return builder
                .overrideConfiguration(o -> publisher.ifAvailable(o::addMetricPublisher))
                .build();
    }

    @Bean
    public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient(DynamoDbAsyncClient client) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build();
    }

}
