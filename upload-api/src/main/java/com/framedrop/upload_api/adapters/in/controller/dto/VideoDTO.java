package com.framedrop.upload_api.adapters.in.controller.dto;

import com.framedrop.upload_api.core.domain.model.enums.StatusProcess;

import java.time.LocalDateTime;

public record VideoDTO( String videoId,
         String userId,
         String userName,
         String email,
         String videoPath,
         String fileName,
         String fileExtension,
         LocalDateTime dateUploaded,
         StatusProcess statusProcess,
         String urlPreSigned) {
}
