package com.lorries.mobile.controller;

import com.lorries.mobile.common.result.Result;
import com.lorries.mobile.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传下载接口")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件")
    public Result<String> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型") @RequestParam(defaultValue = "common") String type) {
        String url = fileService.uploadFile(file, type);
        return Result.success(url);
    }

    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    public Result<List<String>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "文件类型") @RequestParam(defaultValue = "common") String type) {
        List<String> urls = fileService.uploadFiles(files, type);
        return Result.success(urls);
    }

    @DeleteMapping
    @Operation(summary = "删除文件", description = "根据URL删除文件")
    public Result<Void> deleteFile(@Parameter(description = "文件URL") @RequestParam String url) {
        fileService.deleteFile(url);
        return Result.success();
    }
}
