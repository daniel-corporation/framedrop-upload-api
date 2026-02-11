package com.framedrop.upload_api.core.domain.model;

import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VideoTest {

    @Test
    void shouldCreateVideoWithValidData() {
        String videoId = "123";
        String userId = "user123";
        String userName = "John Doe";
        String videoPath = "/videos/test.mp4";
        String fileName = "test.mp4";
        LocalDateTime dateUploaded = LocalDateTime.now().minusHours(1);
        StatusProcess statusProcess = StatusProcess.PENDING;

        Video video = new Video(videoId, userId, userName, videoPath, fileName, dateUploaded, statusProcess);

        assertNotNull(video);
        assertEquals(videoId, video.getVideoId());
        assertEquals(userId, video.getUserId());
        assertEquals(userName, video.getUserName());
        assertEquals(videoPath, video.getVideoPath());
        assertEquals(fileName, video.getFileName());
        assertEquals(".mp4", video.getFileExtension());
        assertEquals(dateUploaded, video.getDateUploaded());
        assertEquals(statusProcess, video.getStatusProcess());
    }

    @Test
    void shouldExtractFileExtensionCorrectly() {
        Video video = new Video("1", "user1", "User", "/path", "video.mkv", LocalDateTime.now().minusDays(1), StatusProcess.PENDING);
        assertEquals(".mkv", video.getFileExtension());
    }

    @Test
    void shouldThrowExceptionWhenFileNameHasNoExtension() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", "/path", "videofile", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenFileExtensionIsNotSupported() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", "/path", "video.txt", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldAcceptAllSupportedFileExtensions() {
        String[] validExtensions = {"test.mp4", "test.mkv", "test.webm", "test.mov", "test.avi"};

        for (String fileName : validExtensions) {
            assertDoesNotThrow(() ->
                    new Video("1", "user1", "User", "/path", fileName, LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
            );
        }
    }

    @Test
    void shouldThrowExceptionWhenDateUploadedIsInFuture() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", "/path", "video.mp4", futureDate, StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", null, "User", "/path", "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "", "User", "/path", "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", null, "/path", "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenUserNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "", "/path", "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenVideoPathIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", null, "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenVideoPathIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", "", "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenFileNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", "/path", null, LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenFileNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", "/path", "", LocalDateTime.now().minusDays(1), StatusProcess.PENDING)
        );
    }

    @Test
    void shouldThrowExceptionWhenStatusProcessIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Video("1", "user1", "User", "/path", "video.mp4", LocalDateTime.now().minusDays(1), null)
        );
    }

    @Test
    void shouldUpdateStatusProcess() {
        Video video = new Video("1", "user1", "User", "/path", "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING);

        video.setStatusProcess(StatusProcess.COMPLETED);

        assertEquals(StatusProcess.COMPLETED, video.getStatusProcess());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusProcessToNull() {
        Video video = new Video("1", "user1", "User", "/path", "video.mp4", LocalDateTime.now().minusDays(1), StatusProcess.PENDING);

        assertThrows(IllegalArgumentException.class, () ->
                video.setStatusProcess(null)
        );
    }
}
