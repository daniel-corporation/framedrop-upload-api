package com.framedrop.upload_api.adapters.out.dynamodb;

import com.framedrop.upload_api.adapters.out.dynamodb.entity.VideoEntity;
import com.framedrop.upload_api.adapters.out.dynamodb.repository.VideoRepository;
import com.framedrop.upload_api.core.domain.model.Video;
import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoDynamoAdapterTest {

    @Mock
    private VideoRepository repository;

    private VideoDynamoAdapter videoDynamoAdapter;

    private VideoEntity videoEntity;
    private Video video;

    @BeforeEach
    void setUp() {
        videoDynamoAdapter = new VideoDynamoAdapter(repository);

        videoEntity = new VideoEntity();
        videoEntity.setVideoId("video123");
        videoEntity.setUserId("user123");
        videoEntity.setUserName("John Doe");
        videoEntity.setEmail("john@example.com");
        videoEntity.setVideoPath("videos/user123/video.mp4");
        videoEntity.setFileName("video.mp4");
        videoEntity.setFileExtension("mp4");
        videoEntity.setDateUploaded(LocalDateTime.now());
        videoEntity.setStatusProcess(StatusProcess.PROCESSING);

        video = new Video("video123", "user123", "John Doe", "john@example.com",
                "videos/user123/video.mp4", "video.mp4", LocalDateTime.now(), StatusProcess.PROCESSING);
    }

    @Test
    void shouldGetVideoByIdSuccessfully() {
        when(repository.getVideoById("video123")).thenReturn(videoEntity);

        Video result = videoDynamoAdapter.getVideoById("video123");

        assertNotNull(result);
        assertEquals("video123", result.getVideoId());
        assertEquals("user123", result.getUserId());
        assertEquals("John Doe", result.getUserName());
        assertEquals("videos/user123/video.mp4", result.getVideoPath());
        assertEquals("video.mp4", result.getFileName());
        assertEquals("PROCESSING", result.getStatusProcess().toString());
        verify(repository, times(1)).getVideoById("video123");
    }

    @Test
    void shouldGetVideosByUserIdSuccessfully() {
        VideoEntity entity2 = new VideoEntity();
        entity2.setVideoId("video456");
        entity2.setUserId("user123");
        entity2.setUserName("John Doe");
        entity2.setEmail("john@example.com");
        entity2.setVideoPath("videos/user123/video2.mp4");
        entity2.setFileName("video2.mp4");
        entity2.setDateUploaded(LocalDateTime.now());
        entity2.setStatusProcess(StatusProcess.COMPLETED);

        when(repository.listAll()).thenReturn(Arrays.asList(videoEntity, entity2));

        List<Video> result = videoDynamoAdapter.getVideosByUserId("user123");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("video123", result.get(0).getVideoId());
        assertEquals("video456", result.get(1).getVideoId());
        verify(repository, times(1)).listAll();
    }

    @Test
    void shouldSaveVideoSuccessfully() {
        videoDynamoAdapter.save(video);

        ArgumentCaptor<VideoEntity> entityCaptor = ArgumentCaptor.forClass(VideoEntity.class);
        verify(repository, times(1)).save(entityCaptor.capture());

        VideoEntity capturedEntity = entityCaptor.getValue();
        assertEquals("video123", capturedEntity.getVideoId());
        assertEquals("user123", capturedEntity.getUserId());
        assertEquals("John Doe", capturedEntity.getUserName());
        assertEquals("videos/user123/video.mp4", capturedEntity.getVideoPath());
        assertEquals("video.mp4", capturedEntity.getFileName());
        assertEquals("PROCESSING", capturedEntity.getStatusProcess().toString());
    }

    @Test
    void shouldGetAllVideosSuccessfully() {
        when(repository.listAll()).thenReturn(Arrays.asList(videoEntity));

        List<Video> result = videoDynamoAdapter.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("video123", result.get(0).getVideoId());
        verify(repository, times(1)).listAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoVideosExist() {
        when(repository.listAll()).thenReturn(Arrays.asList());

        List<Video> result = videoDynamoAdapter.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).listAll();
    }

    @Test
    void shouldHandleMultipleVideosInGetAll() {
        VideoEntity entity2 = new VideoEntity();
        entity2.setVideoId("video789");
        entity2.setUserId("user456");
        entity2.setUserName("Jane Smith");
        entity2.setEmail("jane@example.com");
        entity2.setVideoPath("videos/user456/video.avi");
        entity2.setFileName("video.avi");
        entity2.setDateUploaded( LocalDateTime.now());
        entity2.setStatusProcess(StatusProcess.PENDING);

        when(repository.listAll()).thenReturn(Arrays.asList(videoEntity, entity2));

        List<Video> result = videoDynamoAdapter.getAll();

        assertEquals(2, result.size());
        assertEquals("video123", result.get(0).getVideoId());
        assertEquals("video789", result.get(1).getVideoId());
    }

    @Test
    void shouldMapAllFieldsCorrectlyWhenSaving() {
        Video videoWithExtension = new Video("video999", "user999", "Test User", "test@example.com",
                "path/to/video.webm", "test.webm",LocalDateTime.now(), StatusProcess.COMPLETED);

        videoDynamoAdapter.save(videoWithExtension);

        ArgumentCaptor<VideoEntity> entityCaptor = ArgumentCaptor.forClass(VideoEntity.class);
        verify(repository).save(entityCaptor.capture());

        VideoEntity saved = entityCaptor.getValue();
        assertEquals(".webm", saved.getFileExtension());
    }
}
