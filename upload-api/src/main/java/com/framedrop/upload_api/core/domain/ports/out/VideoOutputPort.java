package com.framedrop.upload_api.core.domain.ports.out;

import com.framedrop.upload_api.core.domain.model.Video;

import java.util.List;

public interface VideoOutputPort {

    Video getVideoById(String videoId);
    List<Video> getVideosByUserId(String userId);
    void save(Video video);
    List<Video> getAll();

}
