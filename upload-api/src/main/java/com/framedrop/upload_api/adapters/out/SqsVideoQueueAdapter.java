package com.framedrop.upload_api.adapters.out;

import com.framedrop.upload_api.adapters.out.dto.VideoMetadata;
import com.framedrop.upload_api.core.domain.ports.out.VideoProcessQueueOutPut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class SqsVideoQueueAdapter implements VideoProcessQueueOutPut {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.video-processing-queue-url}")
    private String queueUrl;


    @Override
    public void pushToQueue(VideoMetadata videoMetadata) {
        String jsonMessage = objectMapper.writeValueAsString(videoMetadata);

        SendMessageRequest sendMsqsageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(jsonMessage)
                .delaySeconds(0)
                .build();

        sqsClient.sendMessage(sendMsqsageRequest);
    }
}
