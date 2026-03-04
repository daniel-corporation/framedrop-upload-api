package com.framedrop.upload_api.core.application.usecases;

import com.framedrop.upload_api.adapters.in.controller.dto.VideoDTO;
import com.framedrop.upload_api.core.domain.model.Video;
import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;
import com.framedrop.upload_api.core.domain.ports.in.VideoInputPort;
import com.framedrop.upload_api.core.domain.ports.out.PreSignedUrlOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.VideoOutputPort;

import java.util.List;

public class VideoUseCase implements VideoInputPort {

    private final VideoOutputPort videoOutputPort;
    private final PreSignedUrlOutputPort preSignedUrlOutputPort;

    public VideoUseCase(VideoOutputPort videoOutputPort, PreSignedUrlOutputPort preSignedUrlOutputPort) {
        this.videoOutputPort = videoOutputPort;
        this.preSignedUrlOutputPort = preSignedUrlOutputPort;
    }

    @Override
    public List<VideoDTO> getAllVideosByUserId(String userId) {

        return videoOutputPort.getVideosByUserId(userId).stream().map(v -> new VideoDTO(
                v.getVideoId(),
                v.getUserId(),
                v.getUserName(),
                v.getEmail(),
                v.getVideoPath(),
                v.getFileName(),
                v.getFileExtension(),
                v.getDateUploaded(),
                v.getStatusProcess(),
                v.getUrlPreSigned()
        )).toList();
    }

    @Override
    public void updateVideoStatus(String videoId, String status) {
        Video video = videoOutputPort.getVideoById(videoId);
        StatusProcess newStatus = StatusProcess.valueOf(status);
        video.setStatusProcess(newStatus);

        if (newStatus == StatusProcess.COMPLETED) {
            String processedZipPath = buildProcessedZipPath(video);
            String preSignedUrl = preSignedUrlOutputPort.generatePreSignedUrl(processedZipPath);
            video.setUrlPreSigned(preSignedUrl);
        }

        videoOutputPort.save(video);
    }

    private String buildProcessedZipPath(Video video) {
        // videoPath format: videos/{userId}/{timestamp}_{originalFileName}
        String videoPathFileName = video.getVideoPath().substring(video.getVideoPath().lastIndexOf('/') + 1);
        String timestampAndFileName = videoPathFileName.contains(".")
                ? videoPathFileName.substring(0, videoPathFileName.lastIndexOf('.'))
                : videoPathFileName;
        return "processed/" + video.getUserId() + "/" + video.getVideoId() + "_" + timestampAndFileName + "_frames.zip";
    }
}
