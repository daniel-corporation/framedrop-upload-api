package com.framedrop.upload_api.adapters.config;

import com.framedrop.upload_api.adapters.out.UploadS3Adapter;
import com.framedrop.upload_api.adapters.out.dynamodb.VideoDynamoAdapter;
import com.framedrop.upload_api.core.application.usecases.UploadVideoUseCase;
import com.framedrop.upload_api.core.domain.ports.in.UploadVideoInputPort;
import com.framedrop.upload_api.core.domain.ports.out.UploadVideoOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.ValidateVideoOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.VideoProcessQueueOutPut;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class UploadVideoConfig {


    @Bean
    public UploadVideoInputPort createUploadVideoInputPort(UploadVideoOutputPort uploadVideoOutputPort,
                                                           VideoDynamoAdapter videoDynamoAdapter,
                                                           ValidateVideoOutputPort validateVideoOutputPort,
                                                           VideoProcessQueueOutPut videoProcessQueueOutPut) {

        return new UploadVideoUseCase(uploadVideoOutputPort, videoDynamoAdapter, validateVideoOutputPort, videoProcessQueueOutPut);
    }

    @Bean
    public UploadVideoOutputPort createUploadVideoOutputPort(){
        return new UploadS3Adapter(S3Client.builder().region(Region.US_EAST_1).build());
    }
}
