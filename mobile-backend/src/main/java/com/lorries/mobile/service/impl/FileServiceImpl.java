package com.lorries.mobile.service.impl;

import com.lorries.mobile.exception.BusinessException;
import com.lorries.mobile.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务实现（MinIO）
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name:lorries}")
    private String bucketName;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Override
    public String uploadFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        try {
            // 确保bucket存在
            ensureBucketExists();

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = generateObjectName(type, extension);

            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return getFileUrl(objectName);
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String type) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file, type));
        }
        return urls;
    }

    @Override
    public void deleteFile(String url) {
        try {
            String objectName = extractObjectName(url);
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            return endpoint + "/" + bucketName + "/" + objectName;
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    private String generateObjectName(String type, String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return type + "/" + datePath + "/" + uuid + extension;
    }

    private String extractObjectName(String url) {
        int bucketIndex = url.indexOf(bucketName);
        if (bucketIndex == -1) {
            throw new BusinessException("无效的文件URL");
        }
        return url.substring(bucketIndex + bucketName.length() + 1);
    }
}
