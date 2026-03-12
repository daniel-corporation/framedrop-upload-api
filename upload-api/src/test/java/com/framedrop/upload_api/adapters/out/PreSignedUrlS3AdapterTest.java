package com.framedrop.upload_api.adapters.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreSignedUrlS3AdapterTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    private PreSignedUrlS3Adapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PreSignedUrlS3Adapter(s3Presigner, "framedrop-upload");
    }

    @Test
    void shouldGeneratePreSignedUrlSuccessfully() throws Exception {
        String videoPath = "videos/user123/video.mp4";
        URL expectedUrl = new URL("https://framedrop-upload.s3.amazonaws.com/videos/user123/video.mp4?presigned=true");

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGetObjectRequest);
        when(presignedGetObjectRequest.url()).thenReturn(expectedUrl);

        String result = adapter.generatePreSignedUrl(videoPath);

        assertNotNull(result);
        assertEquals(expectedUrl.toString(), result);
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenPresignerFails() {
        String videoPath = "videos/user123/video.mp4";

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("S3 error"));

        assertThrows(RuntimeException.class, () -> adapter.generatePreSignedUrl(videoPath));
    }
}
