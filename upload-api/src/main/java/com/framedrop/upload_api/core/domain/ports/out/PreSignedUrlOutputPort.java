package com.framedrop.upload_api.core.domain.ports.out;

public interface PreSignedUrlOutputPort {
    String generatePreSignedUrl(String videoPath);
}
