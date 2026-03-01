package com.framedrop.upload_api.core.application.usecases;

import com.framedrop.upload_api.adapters.in.controller.dto.VideoDTO;
import com.framedrop.upload_api.core.domain.model.Video;
import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;
import com.framedrop.upload_api.core.domain.ports.in.VideoInputPort;
import com.framedrop.upload_api.core.domain.ports.out.VideoOutputPort;

import java.util.List;

public class VideoUseCase implements VideoInputPort {

    private final VideoOutputPort videoOutputPort;

    public VideoUseCase(VideoOutputPort videoOutputPort) {
        this.videoOutputPort = videoOutputPort;
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
                v.getStatusProcess()
        )).toList();
    }

    @Override
    public void updateVideoStatus(String videoId, String status) {
        Video video = videoOutputPort.getVideoById(videoId);
        video.setStatusProcess(StatusProcess.valueOf(status));
        videoOutputPort.save(video);
    }
}
