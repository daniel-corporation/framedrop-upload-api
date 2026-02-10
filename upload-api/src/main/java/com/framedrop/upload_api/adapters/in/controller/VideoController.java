package com.framedrop.upload_api.adapters.in.controller;

import com.framedrop.upload_api.adapters.in.controller.dto.VideoDTO;
import com.framedrop.upload_api.adapters.in.controller.dto.VideoStatusDTO;
import com.framedrop.upload_api.adapters.out.SqsVideoQueueAdapter;
import com.framedrop.upload_api.adapters.out.dto.VideoMetadata;
import com.framedrop.upload_api.core.domain.ports.in.VideoInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoInputPort videoInputPort;

    private final SqsVideoQueueAdapter adapter;

    @GetMapping("/users/{id}")
    public ResponseEntity<List<VideoDTO>> getVideos(@PathVariable("id") String id) {

        return ResponseEntity.ok(videoInputPort.getAllVideosByUserId(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateVideoStatus(@PathVariable("id") String videoId, @RequestBody VideoStatusDTO videoStatus) {


        videoInputPort.updateVideoStatus(videoId,videoStatus.status());
        return ResponseEntity.ok("Video status updated");
    }

}
