package com.framedrop.upload_api.adapters.out;

import com.framedrop.upload_api.core.domain.ports.out.UploadVideoOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;


@RequiredArgsConstructor
public class UploadS3Adapter implements UploadVideoOutputPort {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public void uploadVideoToStorage(String videoFilePath, MultipartFile videoFile) throws IOException {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(videoFilePath)
                .contentType(videoFile.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(videoFile.getInputStream(),
                        videoFile.getSize()));

    }
}
