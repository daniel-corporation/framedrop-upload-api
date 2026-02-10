package com.framedrop.upload_api.core.domain.model;

import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;

import java.time.LocalDateTime;

public class Video {
    private String videoId;
    private String userId;
    private String userName;
    private String videoPath;
    private String fileName;
    private String fileExtension;
    private LocalDateTime dateUploaded;
    private StatusProcess statusProcess;

    public Video(String videoId, String userId, String userName, String videoPath, String fileName, LocalDateTime dateUploaded, StatusProcess statusProcess) {
        this.videoId = videoId;
        this.userId = userId;
        this.userName = userName;
        this.videoPath = videoPath;
        this.fileName = fileName;
        this.fileExtension = getFileExtensionFromFileName(fileName);
        this.dateUploaded = dateUploaded;
        this.statusProcess = statusProcess;
        this.validate();
    }

    public String getVideoId() {
        return videoId;
    }
    public String getUserId() {
        return userId;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public LocalDateTime getDateUploaded() {
        return dateUploaded;
    }

    public StatusProcess getStatusProcess() {
        return statusProcess;
    }

    public String getUserName() {
        return userName;
    }

    public void setStatusProcess(StatusProcess statusProcess) {
        this.statusProcess = statusProcess;
        validateStatusProcess();
    }

    private void validate() {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (videoPath == null || videoPath.isEmpty()) {
            throw new IllegalArgumentException("Video path cannot be null or empty");
        }
        validateFileName();
        validateFileExtension();
        validateDateUploaded();
        validateStatusProcess();

    }

    private void validateFileName() {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
    }

    private void validateDateUploaded() {
        if (dateUploaded.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date uploaded cannot be in the future");
        }

        if (dateUploaded == null) {
            throw new IllegalArgumentException("Date uploaded cannot be null");
        }
    }

    private void validateFileExtension() {
        if (fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("File extension cannot be null or empty");
        }

        if (!fileExtension.matches("\\.(mp4|mkv|webm|mov|avi)$")) {
            throw new IllegalArgumentException("File extension not supported. Allowed extensions are: .mp4, .mkv, .webm, .mov, .avi");
        }
    }

    private void validateStatusProcess() {
        if (statusProcess == null) {
            throw new IllegalArgumentException("Status process cannot be null");
        }
    }

    private String getFileExtensionFromFileName(String fileName) {
        validateFileName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("File name must contain an extension");
        }
        return fileName.substring(lastDotIndex);
    }


}
