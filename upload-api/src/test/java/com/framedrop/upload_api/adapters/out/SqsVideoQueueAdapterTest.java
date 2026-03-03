package com.framedrop.upload_api.adapters.out;

import com.framedrop.upload_api.adapters.out.dto.VideoMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsVideoQueueAdapterTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    private SqsVideoQueueAdapter sqsVideoQueueAdapter;

    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789/video-queue";

    @BeforeEach
    void setUp() {
        sqsVideoQueueAdapter = new SqsVideoQueueAdapter(sqsClient, objectMapper);
        ReflectionTestUtils.setField(sqsVideoQueueAdapter, "queueUrl", QUEUE_URL);
    }

    @Test
    void shouldPushVideoMetadataToQueueSuccessfully(){
        VideoMetadata videoMetadata = new VideoMetadata("user123", "userId123", "user@test.com", "videos/user123/video.mp4","");

        String jsonMessage = "{\"userId\":\"user123\",\"videoPath\":\"videos/user123/video.mp4\"}";

        when(objectMapper.writeValueAsString(videoMetadata)).thenReturn(jsonMessage);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-123").build());

        sqsVideoQueueAdapter.pushToQueue(videoMetadata);

        verify(objectMapper, times(1)).writeValueAsString(videoMetadata);
        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void shouldSendMessageWithCorrectParameters(){
        VideoMetadata videoMetadata = new VideoMetadata("user456", "userId456", "user@test.com", "videos/user456/video.mp4","");
        String jsonMessage = "{\"userId\":\"user456\",\"videoPath\":\"videos/user456/video.mp4\"}";

        when(objectMapper.writeValueAsString(videoMetadata)).thenReturn(jsonMessage);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().build());

        sqsVideoQueueAdapter.pushToQueue(videoMetadata);

        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(requestCaptor.capture());

        SendMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals(QUEUE_URL, capturedRequest.queueUrl());
        assertEquals(jsonMessage, capturedRequest.messageBody());
        assertEquals(0, capturedRequest.delaySeconds());
    }



    @Test
    void shouldThrowExceptionWhenSqsClientFails(){
        VideoMetadata videoMetadata = new VideoMetadata("user123", "userId123", "user@test.com", "videos/user123/video.mp4","");
        String jsonMessage = "{\"userId\":\"user123\",\"videoPath\":\"videos/user123/video.mp4\"}";

        when(objectMapper.writeValueAsString(videoMetadata)).thenReturn(jsonMessage);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(SqsException.builder().message("SQS error").build());

        assertThrows(SqsException.class, () ->
                sqsVideoQueueAdapter.pushToQueue(videoMetadata)
        );

        verify(objectMapper, times(1)).writeValueAsString(videoMetadata);
        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void shouldHandleDifferentVideoMetadata(){
        VideoMetadata videoMetadata = new VideoMetadata("user789", "userId789", "user@test.com", "custom/path/video.avi","");
        String jsonMessage = "{\"userId\":\"user789\",\"videoPath\":\"custom/path/video.avi\"}";

        when(objectMapper.writeValueAsString(videoMetadata)).thenReturn(jsonMessage);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().build());

        sqsVideoQueueAdapter.pushToQueue(videoMetadata);

        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(requestCaptor.capture());

        assertEquals(jsonMessage, requestCaptor.getValue().messageBody());
    }

    @Test
    void shouldSetDelaySecondsToZero(){
        VideoMetadata videoMetadata = new VideoMetadata("user123", "userId123", "user@test.com", "videos/user123/video.mp4","");
        String jsonMessage = "{}";

        when(objectMapper.writeValueAsString(videoMetadata)).thenReturn(jsonMessage);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().build());

        sqsVideoQueueAdapter.pushToQueue(videoMetadata);

        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(requestCaptor.capture());

        assertEquals(0, requestCaptor.getValue().delaySeconds());
    }
}
