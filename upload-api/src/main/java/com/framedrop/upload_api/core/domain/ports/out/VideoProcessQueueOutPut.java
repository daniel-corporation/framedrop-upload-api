package com.framedrop.upload_api.core.domain.ports.out;

import com.framedrop.upload_api.adapters.out.dto.VideoMetadata;

public interface VideoProcessQueueOutPut {
    void pushToQueue(VideoMetadata videoMetadata);
}
