package com.lorries.hub.controller;

import com.lorries.hub.common.ApiResponse;
import com.lorries.hub.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件存储控制器
 * 提供文件上传、下载、删除等RESTful接口
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "文件存储", description = "MinIO对象存储管理接口")
public class FileStorageController {

    private final MinioService minioService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件到对象存储")
    public ApiResponse<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储路径前缀") @RequestParam(required = false) String prefix) {
        
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        
        String objectName = prefix != null ? prefix + "/" + file.getOriginalFilename() : file.getOriginalFilename();
        String url = minioService.uploadFile(file, objectName);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("objectName", objectName);
        result.put("originalName", file.getOriginalFilename());
        result.put("size", String.valueOf(file.getSize()));
        result.put("contentType", file.getContentType());
        
        return ApiResponse.success(result);
    }

    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    public ApiResponse<List<Map<String, String>>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "存储路径前缀") @RequestParam(required = false) String prefix) {
        
        List<Map<String, String>> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String objectName = prefix != null ? prefix + "/" + file.getOriginalFilename() : file.getOriginalFilename();
                String url = minioService.uploadFile(file, objectName);
                Map<String, String> fileResult = new HashMap<>();
                fileResult.put("url", url);
                fileResult.put("objectName", objectName);
                fileResult.put("originalName", file.getOriginalFilename());
                results.add(fileResult);
            }
        }
        
        return ApiResponse.success(results);
    }

    @PostMapping("/upload/snapshot")
    @Operation(summary = "上传视频截图", description = "上传摄像头视频截图")
    public ApiResponse<String> uploadSnapshot(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "摄像头ID") @RequestParam String cameraId) {
        
        try {
            String url = minioService.uploadSnapshot(
                    file.getBytes(),
                    cameraId,
                    System.currentTimeMillis());
            return ApiResponse.success(url);
        } catch (Exception e) {
            log.error("上传截图失败", e);
            return ApiResponse.error(500, "上传截图失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/plate")
    @Operation(summary = "上传车牌图片", description = "上传车牌识别图片")
    public ApiResponse<String> uploadPlateImage(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "车牌号") @RequestParam String plateNumber) {
        
        try {
            String url = minioService.uploadPlateImage(
                    file.getBytes(),
                    plateNumber,
                    System.currentTimeMillis());
            return ApiResponse.success(url);
        } catch (Exception e) {
            log.error("上传车牌图片失败", e);
            return ApiResponse.error(500, "上传车牌图片失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/anomaly")
    @Operation(summary = "上传异常截图", description = "上传异常事件截图")
    public ApiResponse<String> uploadAnomalySnapshot(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "事件ID") @RequestParam String eventId,
            @Parameter(description = "事件类型") @RequestParam String eventType) {
        
        try {
            String url = minioService.uploadAnomalySnapshot(
                    file.getBytes(),
                    eventId,
                    eventType);
            return ApiResponse.success(url);
        } catch (Exception e) {
            log.error("上传异常截图失败", e);
            return ApiResponse.error(500, "上传异常截图失败: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    @Operation(summary = "下载文件", description = "下载指定文件")
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "对象名称") @RequestParam String objectName) {
        
        try {
            InputStream inputStream = minioService.downloadFile(objectName);
            Map<String, Object> fileInfo = minioService.getFileInfo(objectName);
            
            String filename = objectName.contains("/") 
                    ? objectName.substring(objectName.lastIndexOf("/") + 1) 
                    : objectName;
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename*=UTF-8''" + encodedFilename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength((Long) fileInfo.get("size"))
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/url")
    @Operation(summary = "获取文件访问URL", description = "获取文件的临时访问URL")
    public ApiResponse<String> getFileUrl(
            @Parameter(description = "对象名称") @RequestParam String objectName,
            @Parameter(description = "过期时间(秒)") @RequestParam(defaultValue = "3600") int expiry) {
        
        String url = minioService.getPresignedUrl(objectName, expiry);
        return ApiResponse.success(url);
    }

    @GetMapping("/info")
    @Operation(summary = "获取文件信息", description = "获取文件的元数据信息")
    public ApiResponse<Map<String, Object>> getFileInfo(
            @Parameter(description = "对象名称") @RequestParam String objectName) {
        
        Map<String, Object> info = minioService.getFileInfo(objectName);
        return ApiResponse.success(info);
    }

    @GetMapping("/list")
    @Operation(summary = "列出文件", description = "列出指定目录下的所有文件")
    public ApiResponse<List<Map<String, Object>>> listFiles(
            @Parameter(description = "路径前缀") @RequestParam(required = false, defaultValue = "") String prefix,
            @Parameter(description = "是否递归") @RequestParam(defaultValue = "false") boolean recursive) {
        
        List<Map<String, Object>> files = minioService.listFiles(prefix, recursive);
        return ApiResponse.success(files);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "删除指定文件")
    public ApiResponse<Void> deleteFile(
            @Parameter(description = "对象名称") @RequestParam String objectName) {
        
        minioService.deleteFile(objectName);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/delete/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    public ApiResponse<Void> deleteFiles(@RequestBody List<String> objectNames) {
        minioService.deleteFiles(objectNames);
        return ApiResponse.success(null);
    }

    @PostMapping("/copy")
    @Operation(summary = "复制文件", description = "复制文件到新位置")
    public ApiResponse<Void> copyFile(
            @Parameter(description = "源对象名称") @RequestParam String sourceObject,
            @Parameter(description = "目标对象名称") @RequestParam String targetObject) {
        
        minioService.copyFile(sourceObject, targetObject);
        return ApiResponse.success(null);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取存储统计", description = "获取存储桶的统计信息")
    public ApiResponse<Map<String, Object>> getBucketStats() {
        Map<String, Object> stats = minioService.getBucketStats();
        return ApiResponse.success(stats);
    }
}
