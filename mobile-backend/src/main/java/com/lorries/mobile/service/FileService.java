package com.lorries.mobile.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file, String type);
    
    /**
     * 批量上传文件
     */
    List<String> uploadFiles(MultipartFile[] files, String type);
    
    /**
     * 删除文件
     */
    void deleteFile(String url);
    
    /**
     * 获取文件访问URL
     */
    String getFileUrl(String objectName);
}
