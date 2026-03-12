package com.framedrop.upload_api.core.application.usecases;

import com.framedrop.upload_api.adapters.in.controller.dto.VideoDTO;
import com.framedrop.upload_api.core.domain.model.Video;
import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;
import com.framedrop.upload_api.core.domain.ports.out.PreSignedUrlOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.VideoOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoUseCaseTest {

    @Mock
    private VideoOutputPort videoOutputPort;

    @Mock
    private PreSignedUrlOutputPort preSignedUrlOutputPort;

    private VideoUseCase videoUseCase;

    private Video video1;
    private Video video2;

    @BeforeEach
    void setUp() {
        videoUseCase = new VideoUseCase(videoOutputPort, preSignedUrlOutputPort);
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        video1 = new Video("video1", "user123", "John Doe", "john@example.com", "videos/user123/1772584537277_video1.mp4", "video1.mp4", now, StatusProcess.PENDING);
        video2 = new Video("video2", "user123", "John Doe", "john@example.com", "videos/user123/1772584537277_video2.mkv", "video2.mkv", now, StatusProcess.COMPLETED);
    }

    @Test
    void shouldGetAllVideosByUserId() {
        String userId = "user123";
        when(videoOutputPort.getVideosByUserId(userId)).thenReturn(Arrays.asList(video1, video2));

        List<VideoDTO> result = videoUseCase.getAllVideosByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("video1", result.get(0).videoId());
        assertEquals("video2", result.get(1).videoId());
        assertEquals(StatusProcess.PENDING, result.get(0).statusProcess());
        assertEquals(StatusProcess.COMPLETED, result.get(1).statusProcess());
        verify(videoOutputPort, times(1)).getVideosByUserId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenNoVideosFound() {
        String userId = "user456";
        when(videoOutputPort.getVideosByUserId(userId)).thenReturn(Collections.emptyList());

        List<VideoDTO> result = videoUseCase.getAllVideosByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(videoOutputPort, times(1)).getVideosByUserId(userId);
    }

    @Test
    void shouldUpdateVideoStatusToCompletedAndGeneratePreSignedUrl() {
        String videoId = "video1";
        String newStatus = "COMPLETED";
        String expectedUrl = "https://s3.amazonaws.com/bucket/presigned-url";
        String expectedZipPath = "processed/user123/video1_1772584537277_video1_frames.zip";
        when(videoOutputPort.getVideoById(videoId)).thenReturn(video1);
        when(preSignedUrlOutputPort.generatePreSignedUrl(expectedZipPath)).thenReturn(expectedUrl);

        videoUseCase.updateVideoStatus(videoId, newStatus);

        assertEquals(StatusProcess.COMPLETED, video1.getStatusProcess());
        assertEquals(expectedUrl, video1.getUrlPreSigned());
        verify(videoOutputPort, times(1)).getVideoById(videoId);
        verify(preSignedUrlOutputPort, times(1)).generatePreSignedUrl(expectedZipPath);
        verify(videoOutputPort, times(1)).save(video1);
    }

    @Test
    void shouldUpdateVideoStatusToPendingWithoutGeneratingPreSignedUrl() {
        String videoId = "video2";
        String newStatus = "PENDING";
        when(videoOutputPort.getVideoById(videoId)).thenReturn(video2);

        videoUseCase.updateVideoStatus(videoId, newStatus);

        assertEquals(StatusProcess.PENDING, video2.getStatusProcess());
        assertNull(video2.getUrlPreSigned());
        verify(videoOutputPort, times(1)).getVideoById(videoId);
        verify(videoOutputPort, times(1)).save(video2);
        verify(preSignedUrlOutputPort, never()).generatePreSignedUrl(any());
    }

    @Test
    void shouldThrowExceptionWhenInvalidStatusProvided() {
        String videoId = "video1";
        String invalidStatus = "INVALID_STATUS";
        when(videoOutputPort.getVideoById(videoId)).thenReturn(video1);

        assertThrows(IllegalArgumentException.class, () ->
                videoUseCase.updateVideoStatus(videoId, invalidStatus)
        );

        verify(videoOutputPort, times(1)).getVideoById(videoId);
        verify(videoOutputPort, never()).save(any());
    }

    @Test
    void shouldMapVideoToDTOCorrectly() {
        String userId = "user123";
        when(videoOutputPort.getVideosByUserId(userId)).thenReturn(List.of(video1));

        List<VideoDTO> result = videoUseCase.getAllVideosByUserId(userId);

        VideoDTO dto = result.get(0);
        assertEquals(video1.getVideoId(), dto.videoId());
        assertEquals(video1.getUserId(), dto.userId());
        assertEquals(video1.getUserName(), dto.userName());
        assertEquals(video1.getVideoPath(), dto.videoPath());
        assertEquals(video1.getFileName(), dto.fileName());
        assertEquals(video1.getFileExtension(), dto.fileExtension());
        assertEquals(video1.getDateUploaded(), dto.dateUploaded());
        assertEquals(video1.getStatusProcess(), dto.statusProcess());
        assertEquals(video1.getUrlPreSigned(), dto.urlPreSigned());
    }
}
