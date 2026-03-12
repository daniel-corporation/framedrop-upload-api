package com.framedrop.upload_api.core.domain.ports.in;

import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadVideoInputPort {
    void uploadVideo(MultipartFile videoFile, UserDTO userDTO) throws IOException;
}
