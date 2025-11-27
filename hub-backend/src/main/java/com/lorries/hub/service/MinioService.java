package com.lorries.hub.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * MinIO对象存储服务接口
 * 提供文件上传、下载、删除等功能
 */
public interface MinioService {

    /**
     * 初始化存储桶
     * 如果存储桶不存在则创建
     */
    void initBucket();

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    void createBucket(String bucketName);

    /**
     * 上传文件
     *
     * @param file       文件
     * @param objectName 对象名称（存储路径）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String objectName);

    /**
     * 上传文件流
     *
     * @param inputStream 输入流
     * @param objectName  对象名称
     * @param contentType 内容类型
     * @param size        文件大小
     * @return 文件访问URL
     */
    String uploadStream(InputStream inputStream, String objectName, String contentType, long size);

    /**
     * 上传视频截图
     *
     * @param imageData   图片数据
     * @param cameraId    摄像头ID
     * @param timestamp   时间戳
     * @return 文件访问URL
     */
    String uploadSnapshot(byte[] imageData, String cameraId, long timestamp);

    /**
     * 上传车牌识别图片
     *
     * @param imageData   图片数据
     * @param plateNumber 车牌号
     * @param timestamp   时间戳
     * @return 文件访问URL
     */
    String uploadPlateImage(byte[] imageData, String plateNumber, long timestamp);

    /**
     * 上传异常事件截图
     *
     * @param imageData   图片数据
     * @param eventId     事件ID
     * @param eventType   事件类型
     * @return 文件访问URL
     */
    String uploadAnomalySnapshot(byte[] imageData, String eventId, String eventType);

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String objectName);

    /**
     * 获取文件临时访问URL
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 临时访问URL
     */
    String getPresignedUrl(String objectName, int expiry);

    /**
     * 获取文件永久访问URL
     *
     * @param objectName 对象名称
     * @return 永久访问URL
     */
    String getFileUrl(String objectName);

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    void deleteFile(String objectName);

    /**
     * 批量删除文件
     *
     * @param objectNames 对象名称列表
     */
    void deleteFiles(List<String> objectNames);

    /**
     * 获取文件信息
     *
     * @param objectName 对象名称
     * @return 文件元信息
     */
    Map<String, Object> getFileInfo(String objectName);

    /**
     * 列出目录下的所有文件
     *
     * @param prefix    前缀（目录路径）
     * @param recursive 是否递归
     * @return 文件列表
     */
    List<Map<String, Object>> listFiles(String prefix, boolean recursive);

    /**
     * 复制文件
     *
     * @param sourceObject 源对象名称
     * @param targetObject 目标对象名称
     */
    void copyFile(String sourceObject, String targetObject);

    /**
     * 获取存储桶统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getBucketStats();
}
