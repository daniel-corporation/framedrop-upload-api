package com.framedrop.upload_api.core.domain.ports.out;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ValidateVideoOutputPort {
    boolean isValidFormatVideo(MultipartFile file) throws IOException;
}
