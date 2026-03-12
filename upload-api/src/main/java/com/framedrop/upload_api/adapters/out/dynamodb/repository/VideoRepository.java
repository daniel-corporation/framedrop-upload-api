package com.framedrop.upload_api.adapters.out.dynamodb.repository;

import com.framedrop.upload_api.adapters.out.dynamodb.entity.VideoEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class VideoRepository {

    private final DynamoDbTable<VideoEntity> videoEntityDynamoDbTable;


    public VideoRepository(DynamoDbEnhancedClient enhancedClient) {
        this.videoEntityDynamoDbTable = enhancedClient.table("Video", TableSchema.fromBean(VideoEntity.class));
    }


    public VideoEntity getVideoById(String videoId){
        return videoEntityDynamoDbTable.getItem(Key.builder().partitionValue(videoId).build());
    }

    public void save(VideoEntity videoEntity){
        videoEntityDynamoDbTable.putItem(videoEntity);
    }

    public List<VideoEntity> getAllUploadVideosByUser(String userId){
        DynamoDbIndex<VideoEntity> index = videoEntityDynamoDbTable.index("videoId-userid-index");

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(userId)
                        .build());

        return index.query(queryConditional)
                .iterator()
                .next()
                .items()
                .stream().
                toList();
    }

    public List<VideoEntity> listAll() {
        return StreamSupport.stream(videoEntityDynamoDbTable
                        .scan()
                        .items()
                        .spliterator(), false)
                .collect(Collectors.toList());
    }


}
