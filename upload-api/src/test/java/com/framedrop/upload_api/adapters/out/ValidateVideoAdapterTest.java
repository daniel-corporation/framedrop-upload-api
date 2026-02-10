package com.framedrop.upload_api.adapters.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateVideoAdapterTest {

    @Mock
    private MultipartFile videoFile;

    private ValidateVideoAdapter validateVideoAdapter;

    @BeforeEach
    void setUp() {
        validateVideoAdapter = new ValidateVideoAdapter();
    }

    @Test
    void shouldReturnTrueForValidMp4Video() throws IOException {
        byte[] mp4Header = new byte[]{0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70};
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(mp4Header));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertTrue(result);
        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldReturnTrueForValidAviVideo() throws IOException {
        byte[] aviHeader = new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x41, 0x56, 0x49, 0x20};
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(aviHeader));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertTrue(result);
        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldReturnTrueForValidWebmVideo() throws IOException {
        byte[] webmHeader = new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3};
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(webmHeader));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertTrue(result);
        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldReturnTrueForValidMkvVideo() throws IOException {
        byte[] mkvHeader = new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3};
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(mkvHeader));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertTrue(result);
        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldReturnFalseForInvalidFileFormat() throws IOException {
        byte[] textHeader = new byte[]{0x54, 0x65, 0x78, 0x74}; // "Text"
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(textHeader));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertFalse(result);
        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldReturnFalseForImageFile() throws IOException {
        byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(jpegHeader));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertFalse(result);
        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldReturnFalseForPdfFile() throws IOException {
        byte[] pdfHeader = new byte[]{0x25, 0x50, 0x44, 0x46}; // "%PDF"
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfHeader));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertFalse(result);
        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldThrowIOExceptionWhenInputStreamFails() throws IOException {
        when(videoFile.getInputStream()).thenThrow(new IOException("Stream error"));

        assertThrows(IOException.class, () ->
                validateVideoAdapter.isValidFormatVideo(videoFile)
        );

        verify(videoFile, times(1)).getInputStream();
    }

    @Test
    void shouldReturnFalseForEmptyFile() throws IOException {
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        boolean result = validateVideoAdapter.isValidFormatVideo(videoFile);

        assertFalse(result);
        verify(videoFile, times(1)).getInputStream();
    }
}
