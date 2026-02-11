package com.framedrop.upload_api.core.application.usecases;

import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;
import com.framedrop.upload_api.adapters.out.dto.VideoMetadata;
import com.framedrop.upload_api.adapters.out.dynamodb.VideoDynamoAdapter;
import com.framedrop.upload_api.core.domain.model.Video;
import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;
import com.framedrop.upload_api.core.domain.ports.in.UploadVideoInputPort;
import com.framedrop.upload_api.core.domain.ports.out.UploadVideoOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.ValidateVideoOutputPort;
import com.framedrop.upload_api.core.domain.ports.out.VideoProcessQueueOutPut;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;


public class UploadVideoUseCase implements UploadVideoInputPort {

    private final UploadVideoOutputPort uploadVideoOutputPort;

    private final VideoDynamoAdapter videoDynamoAdapter;

    private final ValidateVideoOutputPort validateVideoOutputPort;

    private final VideoProcessQueueOutPut videoProcessQueueOutPut;

    public UploadVideoUseCase(UploadVideoOutputPort uploadVideoOutputPort,
                              VideoDynamoAdapter videoDynamoAdapter,
                              ValidateVideoOutputPort validateVideoOutputPort,
                              VideoProcessQueueOutPut videoProcessQueueOutPut) {

        this.uploadVideoOutputPort = uploadVideoOutputPort;
        this.videoDynamoAdapter = videoDynamoAdapter;
        this.validateVideoOutputPort = validateVideoOutputPort;
        this.videoProcessQueueOutPut = videoProcessQueueOutPut;
    }

    @Override
    public void uploadVideo(MultipartFile videoFile, UserDTO userDto) {
        try {

            if(!validateVideoOutputPort.isValidFormatVideo(videoFile)){
                throw new IllegalArgumentException("Invalid video format");
            }

            Video newVideo = new Video(
                    UUID.randomUUID().toString(),
                    userDto.userId(),
                    userDto.userName(),
                    "videos/" + userDto.userId()+ "/" + videoFile.getOriginalFilename() + "_" + System.currentTimeMillis(),
                    videoFile.getOriginalFilename(),
                    LocalDateTime.now(),
                    StatusProcess.PENDING);

            videoDynamoAdapter.save(newVideo);
            uploadVideoOutputPort.uploadVideoToStorage(newVideo.getVideoPath(), videoFile);

            videoProcessQueueOutPut.pushToQueue(
                    new VideoMetadata(newVideo.getVideoId(),
                            newVideo.getUserId(),
                            newVideo.getVideoPath(),
                            newVideo.getStatusProcess().toString()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload video", e);
        }
    }
}
