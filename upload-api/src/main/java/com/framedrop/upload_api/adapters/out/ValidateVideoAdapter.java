package com.framedrop.upload_api.adapters.out;

import com.framedrop.upload_api.core.domain.ports.out.ValidateVideoOutputPort;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Component
public class ValidateVideoAdapter implements ValidateVideoOutputPort {

    private static final Tika tika = new Tika();

    private static final Set<String> ALLOWED_VIDEO_MIME_TYPES = Set.of(
            "video/mp4",
            "video/x-msvideo", //avi
            "video/quicktime", //.mov
            "video/webm",
            "video/x-matroska",//mkv
            "application/x-matroska"//mkv
    );
    @Override
    public boolean isValidFormatVideo(MultipartFile file) throws IOException {

        String dectectedMimeType = tika.detect(file.getInputStream());

        return ALLOWED_VIDEO_MIME_TYPES.contains(dectectedMimeType);
    }
}
