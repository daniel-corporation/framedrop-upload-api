package com.framedrop.upload_api.adapters.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadS3AdapterTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile videoFile;

    private UploadS3Adapter uploadS3Adapter;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String VIDEO_PATH = "videos/user123/video.mp4";
    private static final String CONTENT_TYPE = "video/mp4";
    private static final long FILE_SIZE = 1024L;

    @BeforeEach
    void setUp() {
        uploadS3Adapter = new UploadS3Adapter(s3Client);
        ReflectionTestUtils.setField(uploadS3Adapter, "bucketName", BUCKET_NAME);
    }

    @Test
    void shouldUploadVideoSuccessfully() throws IOException {
        byte[] fileContent = "video content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(videoFile.getInputStream()).thenReturn(inputStream);
        when(videoFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(videoFile.getSize()).thenReturn(FILE_SIZE);

        uploadS3Adapter.uploadVideoToStorage(VIDEO_PATH, videoFile);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(BUCKET_NAME, capturedRequest.bucket());
        assertEquals(VIDEO_PATH, capturedRequest.key());
        assertEquals(CONTENT_TYPE, capturedRequest.contentType());
    }

    @Test
    void shouldCallS3ClientWithCorrectParameters() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());

        when(videoFile.getInputStream()).thenReturn(inputStream);
        when(videoFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(videoFile.getSize()).thenReturn(FILE_SIZE);

        uploadS3Adapter.uploadVideoToStorage(VIDEO_PATH, videoFile);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(videoFile, times(1)).getInputStream();
        verify(videoFile, times(1)).getContentType();
        verify(videoFile, times(1)).getSize();
    }

    @Test
    void shouldThrowIOExceptionWhenGetInputStreamFails() throws IOException {
        when(videoFile.getInputStream()).thenThrow(new IOException("Failed to read file"));

        assertThrows(IOException.class, () ->
                uploadS3Adapter.uploadVideoToStorage(VIDEO_PATH, videoFile)
        );

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldThrowExceptionWhenS3ClientFails() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());

        when(videoFile.getInputStream()).thenReturn(inputStream);
        when(videoFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(videoFile.getSize()).thenReturn(FILE_SIZE);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        assertThrows(S3Exception.class, () ->
                uploadS3Adapter.uploadVideoToStorage(VIDEO_PATH, videoFile)
        );
    }

    @Test
    void shouldHandleDifferentContentTypes() throws IOException {
        String aviContentType = "video/avi";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());

        when(videoFile.getInputStream()).thenReturn(inputStream);
        when(videoFile.getContentType()).thenReturn(aviContentType);
        when(videoFile.getSize()).thenReturn(FILE_SIZE);

        uploadS3Adapter.uploadVideoToStorage(VIDEO_PATH, videoFile);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        assertEquals(aviContentType, requestCaptor.getValue().contentType());
    }

    @Test
    void shouldHandleDifferentFileSizes() throws IOException {
        long largeFileSize = 1024L * 1024L * 100L; // 100MB
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());

        when(videoFile.getInputStream()).thenReturn(inputStream);
        when(videoFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(videoFile.getSize()).thenReturn(largeFileSize);

        uploadS3Adapter.uploadVideoToStorage(VIDEO_PATH, videoFile);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(videoFile).getSize();
    }

    @Test
    void shouldHandleDifferentVideoPaths() throws IOException {
        String customPath = "custom/path/video123.mp4";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());

        when(videoFile.getInputStream()).thenReturn(inputStream);
        when(videoFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(videoFile.getSize()).thenReturn(FILE_SIZE);

        uploadS3Adapter.uploadVideoToStorage(customPath, videoFile);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        assertEquals(customPath, requestCaptor.getValue().key());
    }
}
