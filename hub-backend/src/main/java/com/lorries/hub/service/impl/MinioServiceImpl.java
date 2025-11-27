package com.lorries.hub.service.impl;

import com.lorries.hub.config.MinioConfig;
import com.lorries.hub.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * MinIO对象存储服务实现
 * 提供文件上传、下载、删除等功能的具体实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 初始化时检查并创建存储桶
     */
    @PostConstruct
    @Override
    public void initBucket() {
        try {
            if (!bucketExists(minioConfig.getBucketName())) {
                createBucket(minioConfig.getBucketName());
                log.info("创建MinIO存储桶: {}", minioConfig.getBucketName());
            } else {
                log.info("MinIO存储桶已存在: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.warn("MinIO初始化失败，服务可能未启动: {}", e.getMessage());
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查存储桶是否存在失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            log.info("成功创建存储桶: {}", bucketName);
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", e.getMessage());
            throw new RuntimeException("创建存储桶失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            String finalObjectName = generateObjectName(objectName, getFileExtension(file.getOriginalFilename()));
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(finalObjectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            log.info("文件上传成功: {}", finalObjectName);
            return getFileUrl(finalObjectName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadStream(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            
            log.info("流上传成功: {}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("流上传失败: {}", e.getMessage());
            throw new RuntimeException("流上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadSnapshot(byte[] imageData, String cameraId, long timestamp) {
        String objectName = String.format("snapshots/%s/%s/%s_%d.jpg",
                LocalDateTime.now().format(DATE_FORMATTER),
                cameraId,
                cameraId,
                timestamp);
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            return uploadStream(bis, objectName, "image/jpeg", imageData.length);
        } catch (Exception e) {
            log.error("上传截图失败: {}", e.getMessage());
            throw new RuntimeException("上传截图失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadPlateImage(byte[] imageData, String plateNumber, long timestamp) {
        // 处理车牌号中的特殊字符
        String safePlateNumber = plateNumber.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        String objectName = String.format("plates/%s/%s_%d.jpg",
                LocalDateTime.now().format(DATE_FORMATTER),
                safePlateNumber,
                timestamp);
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            return uploadStream(bis, objectName, "image/jpeg", imageData.length);
        } catch (Exception e) {
            log.error("上传车牌图片失败: {}", e.getMessage());
            throw new RuntimeException("上传车牌图片失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadAnomalySnapshot(byte[] imageData, String eventId, String eventType) {
        String objectName = String.format("anomalies/%s/%s/%s_%s.jpg",
                LocalDateTime.now().format(DATE_FORMATTER),
                eventType.toLowerCase(),
                eventId,
                LocalDateTime.now().format(DATETIME_FORMATTER));
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            return uploadStream(bis, objectName, "image/jpeg", imageData.length);
        } catch (Exception e) {
            log.error("上传异常截图失败: {}", e.getMessage());
            throw new RuntimeException("上传异常截图失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}", e.getMessage());
            throw new RuntimeException("获取预签名URL失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return String.format("%s/%s/%s",
                minioConfig.getEndpoint(),
                minioConfig.getBucketName(),
                objectName);
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFiles(List<String> objectNames) {
        try {
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());
            
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .objects(objects)
                            .build());
            
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("删除文件失败: {} - {}", error.objectName(), error.message());
            }
            
            log.info("批量删除完成，共{}个文件", objectNames.size());
        } catch (Exception e) {
            log.error("批量删除失败: {}", e.getMessage());
            throw new RuntimeException("批量删除失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            
            Map<String, Object> info = new HashMap<>();
            info.put("objectName", objectName);
            info.put("size", stat.size());
            info.put("contentType", stat.contentType());
            info.put("lastModified", stat.lastModified().toString());
            info.put("etag", stat.etag());
            info.put("userMetadata", stat.userMetadata());
            
            return info;
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            throw new RuntimeException("获取文件信息失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> listFiles(String prefix, boolean recursive) {
        List<Map<String, Object>> files = new ArrayList<>();
        
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .prefix(prefix)
                            .recursive(recursive)
                            .build());
            
            for (Result<Item> result : results) {
                Item item = result.get();
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("objectName", item.objectName());
                fileInfo.put("size", item.size());
                fileInfo.put("lastModified", item.lastModified() != null ? item.lastModified().toString() : null);
                fileInfo.put("isDir", item.isDir());
                fileInfo.put("etag", item.etag());
                files.add(fileInfo);
            }
        } catch (Exception e) {
            log.error("列出文件失败: {}", e.getMessage());
            throw new RuntimeException("列出文件失败: " + e.getMessage());
        }
        
        return files;
    }

    @Override
    public void copyFile(String sourceObject, String targetObject) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(targetObject)
                    .source(CopySource.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(sourceObject)
                            .build())
                    .build());
            log.info("文件复制成功: {} -> {}", sourceObject, targetObject);
        } catch (Exception e) {
            log.error("文件复制失败: {}", e.getMessage());
            throw new RuntimeException("文件复制失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getBucketStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalSize = 0;
            int fileCount = 0;
            Map<String, Integer> typeCount = new HashMap<>();
            
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .recursive(true)
                            .build());
            
            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    totalSize += item.size();
                    fileCount++;
                    
                    String extension = getFileExtension(item.objectName());
                    typeCount.merge(extension, 1, Integer::sum);
                }
            }
            
            stats.put("bucketName", minioConfig.getBucketName());
            stats.put("totalSize", totalSize);
            stats.put("totalSizeFormatted", formatFileSize(totalSize));
            stats.put("fileCount", fileCount);
            stats.put("typeDistribution", typeCount);
            
        } catch (Exception e) {
            log.error("获取存储桶统计信息失败: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 生成对象名称
     */
    private String generateObjectName(String baseName, String extension) {
        String datePath = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        if (baseName.contains("/")) {
            // 如果已包含路径，只替换文件名部分
            int lastSlash = baseName.lastIndexOf('/');
            String path = baseName.substring(0, lastSlash + 1);
            return path + datePath + "/" + uuid + "." + extension;
        }
        
        return datePath + "/" + uuid + "." + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
