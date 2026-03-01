package com.framedrop.upload_api.adapters.in.controller;

import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;
import com.framedrop.upload_api.core.domain.ports.in.TokenInputPort;
import com.framedrop.upload_api.core.domain.ports.in.UploadVideoInputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    @Mock
    private UploadVideoInputPort uploadVideoInputPort;

    @Mock
    private TokenInputPort tokenInputPort;

    @Mock
    private MultipartFile multipartFile;

    private UploadController uploadController;

    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String EMAIL = "john@example.com";
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        uploadController = new UploadController(uploadVideoInputPort, tokenInputPort);
        userDTO = new UserDTO("user123", "John Doe", null);
    }

    @Test
    void shouldUploadVideoSuccessfully() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(tokenInputPort.getUserFromToken(BEARER_TOKEN)).thenReturn(userDTO);

        ResponseEntity<?> response = uploadController.uploadVideo(BEARER_TOKEN, multipartFile, EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Video was sent to processing", response.getBody());
        verify(tokenInputPort, times(1)).getUserFromToken(BEARER_TOKEN);
        verify(uploadVideoInputPort, times(1)).uploadVideo(eq(multipartFile), any(UserDTO.class));
    }

    @Test
    void shouldReturnBadRequestWhenFileIsNull() {
        ResponseEntity<?> response = uploadController.uploadVideo(BEARER_TOKEN, null, EMAIL);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No file provided", response.getBody());
        verify(tokenInputPort, never()).getUserFromToken(anyString());
        verify(uploadVideoInputPort, never()).uploadVideo(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenFileIsEmpty() {
        when(multipartFile.isEmpty()).thenReturn(true);

        ResponseEntity<?> response = uploadController.uploadVideo(BEARER_TOKEN, multipartFile, EMAIL);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No file provided", response.getBody());
        verify(tokenInputPort, never()).getUserFromToken(anyString());
        verify(uploadVideoInputPort, never()).uploadVideo(any(), any());
    }

    @Test
    void shouldCallTokenInputPortWithCorrectToken() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(tokenInputPort.getUserFromToken(BEARER_TOKEN)).thenReturn(userDTO);

        uploadController.uploadVideo(BEARER_TOKEN, multipartFile, EMAIL);

        verify(tokenInputPort).getUserFromToken(BEARER_TOKEN);
    }

    @Test
    void shouldCallUploadVideoWithEmailFromRequest() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(tokenInputPort.getUserFromToken(BEARER_TOKEN)).thenReturn(userDTO);

        uploadController.uploadVideo(BEARER_TOKEN, multipartFile, EMAIL);

        ArgumentCaptor<UserDTO> userCaptor = ArgumentCaptor.forClass(UserDTO.class);
        verify(uploadVideoInputPort).uploadVideo(eq(multipartFile), userCaptor.capture());

        UserDTO captured = userCaptor.getValue();
        assertEquals("user123", captured.userId());
        assertEquals("John Doe", captured.userName());
        assertEquals(EMAIL, captured.email());
    }

    @Test
    void shouldHandleDifferentBearerTokens() {
        String differentToken = "Bearer different.token.here";
        UserDTO differentUser = new UserDTO("user456", "Jane Doe", null);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(tokenInputPort.getUserFromToken(differentToken)).thenReturn(differentUser);

        uploadController.uploadVideo(differentToken, multipartFile, "jane@example.com");

        verify(tokenInputPort).getUserFromToken(differentToken);
        ArgumentCaptor<UserDTO> userCaptor = ArgumentCaptor.forClass(UserDTO.class);
        verify(uploadVideoInputPort).uploadVideo(eq(multipartFile), userCaptor.capture());
        assertEquals("jane@example.com", userCaptor.getValue().email());
    }
}
