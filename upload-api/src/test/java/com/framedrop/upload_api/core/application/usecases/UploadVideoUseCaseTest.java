package com.framedrop.upload_api.core.application.usecases;

import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;
import com.framedrop.upload_api.adapters.out.dynamodb.VideoDynamoAdapter;
import com.framedrop.upload_api.core.domain.model.Video;
import com.framedrop.upload_api.core.domain.ports.out.UploadVideoOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.ValidateVideoOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadVideoUseCaseTest {

    @Mock
    private UploadVideoOutputPort uploadVideoOutputPort;

    @Mock
    private VideoDynamoAdapter videoDynamoAdapter;

    @Mock
    private ValidateVideoOutputPort validateVideoOutputPort;

    @Mock
    private MultipartFile videoFile;

    @InjectMocks
    private UploadVideoUseCase uploadVideoUseCase;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO("user123", "John Doe");
        lenient().when(videoFile.getOriginalFilename()).thenReturn("video.mp4");
    }

    @Test
    void shouldUploadVideoSuccessfully() throws IOException {
        when(validateVideoOutputPort.isValidFormatVideo(videoFile)).thenReturn(true);

        uploadVideoUseCase.uploadVideo(videoFile, userDTO);

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoDynamoAdapter, times(1)).save(videoCaptor.capture());
        verify(uploadVideoOutputPort, times(1)).uploadVideoToStorage(anyString(), eq(videoFile));

        Video savedVideo = videoCaptor.getValue();
        assertNotNull(savedVideo);
        assertEquals("user123", savedVideo.getUserId());
        assertEquals("John Doe", savedVideo.getUserName());
        assertEquals("video.mp4", savedVideo.getFileName());
        assertTrue(savedVideo.getVideoPath().startsWith("videos/user123/video.mp4_"));
        assertNotNull(savedVideo.getVideoId());
        assertNotNull(savedVideo.getDateUploaded());
    }

    @Test
    void shouldThrowExceptionWhenVideoFormatIsInvalid() throws IOException {
        when(validateVideoOutputPort.isValidFormatVideo(videoFile)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                uploadVideoUseCase.uploadVideo(videoFile, userDTO)
        );

        assertTrue(exception.getMessage().contains("Failed to upload video"));
        assertEquals("Invalid video format", exception.getCause().getMessage());
        verify(videoDynamoAdapter, never()).save(any());
        verify(uploadVideoOutputPort, never()).uploadVideoToStorage(anyString(), any());
    }

    @Test
    void shouldThrowExceptionWhenDynamoAdapterFails() throws IOException {
        when(validateVideoOutputPort.isValidFormatVideo(videoFile)).thenReturn(true);
        doThrow(new RuntimeException("DynamoDB error")).when(videoDynamoAdapter).save(any(Video.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                uploadVideoUseCase.uploadVideo(videoFile, userDTO)
        );

        assertTrue(exception.getMessage().contains("Failed to upload video"));
        verify(uploadVideoOutputPort, never()).uploadVideoToStorage(anyString(), any());
    }

    @Test
    void shouldThrowExceptionWhenUploadToStorageFails() throws IOException {
        when(validateVideoOutputPort.isValidFormatVideo(videoFile)).thenReturn(true);
        doThrow(new RuntimeException("Storage error")).when(uploadVideoOutputPort)
                .uploadVideoToStorage(anyString(), any(MultipartFile.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                uploadVideoUseCase.uploadVideo(videoFile, userDTO)
        );

        assertTrue(exception.getMessage().contains("Failed to upload video"));
        verify(videoDynamoAdapter, times(1)).save(any(Video.class));
    }

    @Test
    void shouldGenerateUniqueVideoPathWithTimestamp() throws IOException {
        when(validateVideoOutputPort.isValidFormatVideo(videoFile)).thenReturn(true);

        uploadVideoUseCase.uploadVideo(videoFile, userDTO);

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoDynamoAdapter).save(videoCaptor.capture());

        Video savedVideo = videoCaptor.getValue();
        assertTrue(savedVideo.getVideoPath().matches("videos/user123/video\\.mp4_\\d+"));
    }

    @Test
    void shouldGenerateUniqueVideoId() throws IOException {
        when(validateVideoOutputPort.isValidFormatVideo(videoFile)).thenReturn(true);

        uploadVideoUseCase.uploadVideo(videoFile, userDTO);

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoDynamoAdapter).save(videoCaptor.capture());

        Video savedVideo = videoCaptor.getValue();
        assertNotNull(savedVideo.getVideoId());
        assertFalse(savedVideo.getVideoId().isEmpty());
    }

    @Test
    void shouldCallUploadToStorageWithCorrectPath() throws IOException {
        when(validateVideoOutputPort.isValidFormatVideo(videoFile)).thenReturn(true);

        uploadVideoUseCase.uploadVideo(videoFile, userDTO);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(uploadVideoOutputPort).uploadVideoToStorage(pathCaptor.capture(), eq(videoFile));

        String uploadPath = pathCaptor.getValue();
        assertTrue(uploadPath.startsWith("videos/user123/video.mp4_"));
    }
}
