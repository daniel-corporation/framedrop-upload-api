package com.framedrop.upload_api.adapters.config;

import com.framedrop.upload_api.adapters.out.PreSignedUrlS3Adapter;
import com.framedrop.upload_api.adapters.out.dynamodb.VideoDynamoAdapter;
import com.framedrop.upload_api.core.application.usecases.VideoUseCase;
import com.framedrop.upload_api.core.domain.ports.out.PreSignedUrlOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.VideoOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


@Configuration
@RequiredArgsConstructor
public class VideoConfig {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Bean
    public PreSignedUrlOutputPort preSignedUrlOutputPort(S3Presigner s3Presigner) {
        return new PreSignedUrlS3Adapter(s3Presigner, bucketName);
    }

    @Bean
    public VideoUseCase createVideoUseCase(VideoOutputPort videoOutputPort, PreSignedUrlOutputPort preSignedUrlOutputPort) {
        return new VideoUseCase(videoOutputPort, preSignedUrlOutputPort);
    }

}
