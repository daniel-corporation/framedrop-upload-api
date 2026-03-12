package com.framedrop.upload_api.adapters.in.controller;

import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;
import com.framedrop.upload_api.core.domain.ports.in.TokenInputPort;
import com.framedrop.upload_api.core.domain.ports.in.UploadVideoInputPort;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadVideoInputPort uploadVideoInputPort;
    private final TokenInputPort tokenInputPort;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @RequestHeader("Authorization") String bearerToken,
            @RequestPart("videoFile") MultipartFile videoFile,
            @Email @RequestPart("email") String email) {

        if (videoFile == null || videoFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }

        try {
            UserDTO user = tokenInputPort.getUserFromToken(bearerToken);
            UserDTO userWithEmail = new UserDTO(user.userId(), user.userName(), email);
            uploadVideoInputPort.uploadVideo(videoFile, userWithEmail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid video format");
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while processing the video");
        }


        return ResponseEntity.ok("Video was sent to processing");

    }
}
