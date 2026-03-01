package com.framedrop.upload_api.adapters.in.controller;

import com.framedrop.upload_api.adapters.in.controller.dto.VideoDTO;
import com.framedrop.upload_api.adapters.in.controller.dto.VideoStatusDTO;
import com.framedrop.upload_api.adapters.out.SqsVideoQueueAdapter;
import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;
import com.framedrop.upload_api.core.domain.ports.in.VideoInputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private VideoInputPort videoInputPort;

    @Mock
    private SqsVideoQueueAdapter sqsVideoQueueAdapter;

    private VideoController videoController;

    private VideoDTO videoDTO1;
    private VideoDTO videoDTO2;

    @BeforeEach
    void setUp() {
        videoController = new VideoController(videoInputPort, sqsVideoQueueAdapter);

        videoDTO1 = new VideoDTO("video123", "user123", "John Doe", "john@example.com",
                "videos/user123/video1.mp4", "video1.mp4", "mp4", LocalDateTime.of(2026,02,8,1,1,1),StatusProcess.COMPLETED);
        videoDTO2 = new VideoDTO("video456", "user123", "John Doe", "john@example.com",
                "videos/user123/video2.mp4", "video2.mp4", "mp4", LocalDateTime.of(2026,02,8,1,1,1), StatusProcess.PROCESSING);
    }

    @Test
    void shouldGetVideosByUserIdSuccessfully() {
        List<VideoDTO> expectedVideos = Arrays.asList(videoDTO1, videoDTO2);
        when(videoInputPort.getAllVideosByUserId("user123")).thenReturn(expectedVideos);

        ResponseEntity<List<VideoDTO>> response = videoController.getVideos("user123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("video123", response.getBody().get(0).videoId());
        assertEquals("video456", response.getBody().get(1).videoId());
        verify(videoInputPort, times(1)).getAllVideosByUserId("user123");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoVideos() {
        when(videoInputPort.getAllVideosByUserId("user999")).thenReturn(List.of());

        ResponseEntity<List<VideoDTO>> response = videoController.getVideos("user999");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(videoInputPort, times(1)).getAllVideosByUserId("user999");
    }

    @Test
    void shouldUpdateVideoStatusSuccessfully() {
        String videoId = "video123";
        VideoStatusDTO statusDTO = new VideoStatusDTO("COMPLETED");

        ResponseEntity<String> response = videoController.updateVideoStatus(videoId, statusDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Video status updated", response.getBody());
        verify(videoInputPort, times(1)).updateVideoStatus(videoId, "COMPLETED");
    }

    @Test
    void shouldUpdateVideoStatusWithDifferentStatuses() {
        String videoId = "video456";
        VideoStatusDTO statusDTO = new VideoStatusDTO("FAILED");

        videoController.updateVideoStatus(videoId, statusDTO);

        verify(videoInputPort).updateVideoStatus(videoId, "FAILED");
    }

    @Test
    void shouldCallUpdateVideoStatusWithCorrectParameters() {
        String videoId = "video789";
        VideoStatusDTO statusDTO = new VideoStatusDTO("PROCESSING");

        videoController.updateVideoStatus(videoId, statusDTO);

        ArgumentCaptor<String> videoIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);

        verify(videoInputPort).updateVideoStatus(videoIdCaptor.capture(), statusCaptor.capture());

        assertEquals(videoId, videoIdCaptor.getValue());
        assertEquals("PROCESSING", statusCaptor.getValue());
    }



    @Test
    void shouldHandleDifferentUserIdsInGetVideos() {
        when(videoInputPort.getAllVideosByUserId("user456")).thenReturn(List.of(videoDTO1));

        ResponseEntity<List<VideoDTO>> response = videoController.getVideos("user456");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(videoInputPort).getAllVideosByUserId("user456");
    }
}
