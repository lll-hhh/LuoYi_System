package com.lorries.hub.controller;

import com.lorries.hub.common.ApiResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RTMP视频流回调控制器
 * 处理Nginx RTMP模块的推流事件回调
 */
@Slf4j
@RestController
@RequestMapping("/api/rtmp")
public class RtmpCallbackController {

    // 存储当前活跃的流信息
    private final Map<String, StreamInfo> activeStreams = new ConcurrentHashMap<>();

    /**
     * 推流开始回调
     */
    @PostMapping("/on_publish")
    public ResponseEntity<String> onPublish(
            @RequestParam("app") String app,
            @RequestParam("name") String name,
            @RequestParam(value = "addr", required = false) String addr,
            @RequestParam(value = "clientid", required = false) String clientId) {
        
        log.info("RTMP推流开始 - app: {}, name: {}, addr: {}", app, name, addr);
        
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setApp(app);
        streamInfo.setName(name);
        streamInfo.setClientAddr(addr);
        streamInfo.setClientId(clientId);
        streamInfo.setStartTime(LocalDateTime.now());
        streamInfo.setStatus("ACTIVE");
        
        activeStreams.put(name, streamInfo);
        
        // 返回空响应表示允许推流，返回非2xx状态码可以拒绝推流
        return ResponseEntity.ok("");
    }

    /**
     * 推流结束回调
     */
    @PostMapping("/on_publish_done")
    public ResponseEntity<String> onPublishDone(
            @RequestParam("app") String app,
            @RequestParam("name") String name) {
        
        log.info("RTMP推流结束 - app: {}, name: {}", app, name);
        
        StreamInfo streamInfo = activeStreams.remove(name);
        if (streamInfo != null) {
            streamInfo.setEndTime(LocalDateTime.now());
            streamInfo.setStatus("STOPPED");
            // 可以在这里保存推流记录到数据库
        }
        
        return ResponseEntity.ok("");
    }

    /**
     * 播放开始回调
     */
    @PostMapping("/on_play")
    public ResponseEntity<String> onPlay(
            @RequestParam("app") String app,
            @RequestParam("name") String name,
            @RequestParam(value = "addr", required = false) String addr) {
        
        log.info("RTMP播放开始 - app: {}, name: {}, addr: {}", app, name, addr);
        
        StreamInfo streamInfo = activeStreams.get(name);
        if (streamInfo != null) {
            streamInfo.incrementViewers();
        }
        
        return ResponseEntity.ok("");
    }

    /**
     * 播放结束回调
     */
    @PostMapping("/on_play_done")
    public ResponseEntity<String> onPlayDone(
            @RequestParam("app") String app,
            @RequestParam("name") String name) {
        
        log.info("RTMP播放结束 - app: {}, name: {}", app, name);
        
        StreamInfo streamInfo = activeStreams.get(name);
        if (streamInfo != null) {
            streamInfo.decrementViewers();
        }
        
        return ResponseEntity.ok("");
    }

    /**
     * 录制完成回调
     */
    @PostMapping("/on_record_done")
    public ResponseEntity<String> onRecordDone(
            @RequestParam("app") String app,
            @RequestParam("name") String name,
            @RequestParam("path") String path) {
        
        log.info("RTMP录制完成 - app: {}, name: {}, path: {}", app, name, path);
        
        // 可以在这里触发视频处理任务，如上传到MinIO、调用算法分析等
        
        return ResponseEntity.ok("");
    }

    /**
     * 获取所有活跃流
     */
    @GetMapping("/streams")
    public ResponseEntity<ApiResponse<Map<String, StreamInfo>>> getActiveStreams() {
        return ResponseEntity.ok(ApiResponse.success(activeStreams));
    }

    /**
     * 获取指定流信息
     */
    @GetMapping("/streams/{name}")
    public ResponseEntity<ApiResponse<StreamInfo>> getStream(@PathVariable String name) {
        StreamInfo streamInfo = activeStreams.get(name);
        if (streamInfo != null) {
            return ResponseEntity.ok(ApiResponse.success(streamInfo));
        }
        return ResponseEntity.ok(ApiResponse.error(404, "Stream not found"));
    }

    /**
     * 获取HLS播放地址
     */
    @GetMapping("/hls/{streamName}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getHlsUrl(@PathVariable String streamName) {
        String baseUrl = "http://localhost:8088/hls";
        Map<String, String> urls = new HashMap<>();
        urls.put("master", baseUrl + "/" + streamName + "/index.m3u8");
        urls.put("720p", baseUrl + "/" + streamName + "_720p/index.m3u8");
        urls.put("360p", baseUrl + "/" + streamName + "_360p/index.m3u8");
        return ResponseEntity.ok(ApiResponse.success(urls));
    }

    /**
     * 流信息数据类
     */
    @Data
    public static class StreamInfo {
        private String app;
        private String name;
        private String clientAddr;
        private String clientId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private int viewers = 0;

        public synchronized void incrementViewers() {
            viewers++;
        }

        public synchronized void decrementViewers() {
            if (viewers > 0) viewers--;
        }
    }
}
