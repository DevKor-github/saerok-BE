package org.devkor.apu.saerok_server.global.core.config.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public S3Client s3Client(StaticCredentialsProvider creds, Region region) {
        return S3Client.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(StaticCredentialsProvider creds, Region region) {
        return S3Presigner.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();
    }

    @Bean
    public KmsClient kmsClient(StaticCredentialsProvider creds, Region region) {
        return KmsClient.builder()
                .region(region)
                .credentialsProvider(creds)
                .build();
    }
}
