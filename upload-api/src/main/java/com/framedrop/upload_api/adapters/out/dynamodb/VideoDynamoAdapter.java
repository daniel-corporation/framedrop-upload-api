package com.framedrop.upload_api.adapters.out.dynamodb;

import com.framedrop.upload_api.adapters.out.dynamodb.entity.VideoEntity;
import com.framedrop.upload_api.adapters.out.dynamodb.repository.VideoRepository;
import com.framedrop.upload_api.core.domain.model.Video;
import com.framedrop.upload_api.core.domain.ports.out.VideoOutputPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VideoDynamoAdapter implements VideoOutputPort {

    VideoRepository repository;

    public VideoDynamoAdapter(VideoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Video getVideoById(String videoId) {
        VideoEntity entity = repository.getVideoById(videoId);
        return new Video(entity.getVideoId(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getEmail(),
                entity.getVideoPath(),
                entity.getFileName(),
                entity.getDateUploaded(),
                entity.getStatusProcess());
    }

    @Override
    public List<Video> getVideosByUserId(String userId) {

        return repository.listAll().stream().map(dynamoEntity -> new Video(
                dynamoEntity.getVideoId(),
                dynamoEntity.getUserId(),
                dynamoEntity.getUserName(),
                dynamoEntity.getEmail(),
                dynamoEntity.getVideoPath(),
                dynamoEntity.getFileName(),
                dynamoEntity.getDateUploaded(),
                dynamoEntity.getStatusProcess()
        )).toList();
    }

    @Override
    public void save(Video video) {
        VideoEntity entity = new VideoEntity();
        entity.setVideoId(video.getVideoId());
        entity.setUserId(video.getUserId());
        entity.setUserName(video.getUserName());
        entity.setEmail(video.getEmail());
        entity.setVideoPath(video.getVideoPath());
        entity.setFileName(video.getFileName());
        entity.setFileExtension(video.getFileExtension());
        entity.setDateUploaded(video.getDateUploaded());
        entity.setStatusProcess(video.getStatusProcess());
        entity.setUrlPreSigned(video.getUrlPreSigned());

        repository.save(entity);

    }

    @Override
    public List<Video> getAll() {
        return repository.listAll().stream().map(dynamoEntity -> new Video(
                dynamoEntity.getVideoId(),
                dynamoEntity.getUserId(),
                dynamoEntity.getUserName(),
                dynamoEntity.getEmail(),
                dynamoEntity.getVideoPath(),
                dynamoEntity.getFileName(),
                dynamoEntity.getDateUploaded(),
                dynamoEntity.getStatusProcess()
        )).toList();
    }
}
