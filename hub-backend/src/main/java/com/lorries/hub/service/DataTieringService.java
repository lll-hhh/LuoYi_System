package com.lorries.hub.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据分层转储服务接口
 * 负责实现冷热数据分离和自动归档
 */
public interface DataTieringService {

    /**
     * 执行数据归档
     *
     * @param tableName 表名
     * @return 归档结果
     */
    ArchiveResult archiveTable(String tableName);

    /**
     * 执行所有表的归档
     *
     * @return 归档结果列表
     */
    List<ArchiveResult> archiveAllTables();

    /**
     * 获取分层配置
     *
     * @param tableName 表名
     * @return 分层配置
     */
    TieringConfig getTieringConfig(String tableName);

    /**
     * 更新分层配置
     *
     * @param config 配置信息
     */
    void updateTieringConfig(TieringConfig config);

    /**
     * 获取所有分层配置
     *
     * @return 配置列表
     */
    List<TieringConfig> getAllTieringConfigs();

    /**
     * 创建新分区
     *
     * @param tableName   表名
     * @param monthsAhead 提前创建几个月的分区
     */
    void createFuturePartitions(String tableName, int monthsAhead);

    /**
     * 删除过期分区
     *
     * @param tableName    表名
     * @param monthsToKeep 保留多少个月的分区
     */
    void dropOldPartitions(String tableName, int monthsToKeep);

    /**
     * 压缩归档数据
     *
     * @param tableName 表名
     */
    void compressArchiveData(String tableName);

    /**
     * 获取分层状态报告
     *
     * @return 状态报告
     */
    List<TieringStatus> getTieringStatus();

    /**
     * 获取归档历史记录
     *
     * @param days 查询最近多少天
     * @return 历史记录列表
     */
    List<ArchiveJobLog> getArchiveHistory(int days);

    /**
     * 获取归档统计信息
     *
     * @param tableName 表名
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    List<ArchiveStatistics> getArchiveStatistics(String tableName, LocalDate startDate, LocalDate endDate);

    /**
     * 查询归档数据
     *
     * @param tableName 原表名
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 查询结果
     */
    Map<String, Object> queryArchivedData(String tableName, LocalDate startDate, LocalDate endDate, int pageNum, int pageSize);

    /**
     * 恢复归档数据
     *
     * @param tableName 表名
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 恢复的行数
     */
    long restoreArchivedData(String tableName, LocalDate startDate, LocalDate endDate);

    // ========== 内部类定义 ==========

    /**
     * 归档结果
     */
    class ArchiveResult {
        private String tableName;
        private long archivedRows;
        private long deletedRows;
        private boolean success;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public ArchiveResult() {}

        public ArchiveResult(String tableName, long archivedRows, boolean success) {
            this.tableName = tableName;
            this.archivedRows = archivedRows;
            this.deletedRows = archivedRows;
            this.success = success;
        }

        // Getters and Setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public long getArchivedRows() { return archivedRows; }
        public void setArchivedRows(long archivedRows) { this.archivedRows = archivedRows; }
        public long getDeletedRows() { return deletedRows; }
        public void setDeletedRows(long deletedRows) { this.deletedRows = deletedRows; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }

    /**
     * 分层配置
     */
    class TieringConfig {
        private Long id;
        private String tableName;
        private int hotDays;
        private int warmDays;
        private int coldDays;
        private boolean archiveEnabled;
        private boolean compressionEnabled;
        private String partitionColumn;
        private LocalDateTime lastArchiveTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public int getHotDays() { return hotDays; }
        public void setHotDays(int hotDays) { this.hotDays = hotDays; }
        public int getWarmDays() { return warmDays; }
        public void setWarmDays(int warmDays) { this.warmDays = warmDays; }
        public int getColdDays() { return coldDays; }
        public void setColdDays(int coldDays) { this.coldDays = coldDays; }
        public boolean isArchiveEnabled() { return archiveEnabled; }
        public void setArchiveEnabled(boolean archiveEnabled) { this.archiveEnabled = archiveEnabled; }
        public boolean isCompressionEnabled() { return compressionEnabled; }
        public void setCompressionEnabled(boolean compressionEnabled) { this.compressionEnabled = compressionEnabled; }
        public String getPartitionColumn() { return partitionColumn; }
        public void setPartitionColumn(String partitionColumn) { this.partitionColumn = partitionColumn; }
        public LocalDateTime getLastArchiveTime() { return lastArchiveTime; }
        public void setLastArchiveTime(LocalDateTime lastArchiveTime) { this.lastArchiveTime = lastArchiveTime; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * 分层状态
     */
    class TieringStatus {
        private String tableName;
        private String hotDataSize;
        private String warmDataSize;
        private String coldDataSize;
        private String archiveSize;
        private long totalRows;
        private LocalDateTime lastArchiveTime;

        // Getters and Setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public String getHotDataSize() { return hotDataSize; }
        public void setHotDataSize(String hotDataSize) { this.hotDataSize = hotDataSize; }
        public String getWarmDataSize() { return warmDataSize; }
        public void setWarmDataSize(String warmDataSize) { this.warmDataSize = warmDataSize; }
        public String getColdDataSize() { return coldDataSize; }
        public void setColdDataSize(String coldDataSize) { this.coldDataSize = coldDataSize; }
        public String getArchiveSize() { return archiveSize; }
        public void setArchiveSize(String archiveSize) { this.archiveSize = archiveSize; }
        public long getTotalRows() { return totalRows; }
        public void setTotalRows(long totalRows) { this.totalRows = totalRows; }
        public LocalDateTime getLastArchiveTime() { return lastArchiveTime; }
        public void setLastArchiveTime(LocalDateTime lastArchiveTime) { this.lastArchiveTime = lastArchiveTime; }
    }

    /**
     * 归档任务日志
     */
    class ArchiveJobLog {
        private Long id;
        private String jobName;
        private String tableName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long rowsArchived;
        private long rowsDeleted;
        private String status;
        private String errorMessage;
        private LocalDateTime createdAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getJobName() { return jobName; }
        public void setJobName(String jobName) { this.jobName = jobName; }
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public long getRowsArchived() { return rowsArchived; }
        public void setRowsArchived(long rowsArchived) { this.rowsArchived = rowsArchived; }
        public long getRowsDeleted() { return rowsDeleted; }
        public void setRowsDeleted(long rowsDeleted) { this.rowsDeleted = rowsDeleted; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * 归档统计
     */
    class ArchiveStatistics {
        private Long id;
        private String tableName;
        private LocalDate archiveDate;
        private long totalRows;
        private long dataSizeBytes;
        private long compressedSizeBytes;
        private LocalDateTime createdAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public LocalDate getArchiveDate() { return archiveDate; }
        public void setArchiveDate(LocalDate archiveDate) { this.archiveDate = archiveDate; }
        public long getTotalRows() { return totalRows; }
        public void setTotalRows(long totalRows) { this.totalRows = totalRows; }
        public long getDataSizeBytes() { return dataSizeBytes; }
        public void setDataSizeBytes(long dataSizeBytes) { this.dataSizeBytes = dataSizeBytes; }
        public long getCompressedSizeBytes() { return compressedSizeBytes; }
        public void setCompressedSizeBytes(long compressedSizeBytes) { this.compressedSizeBytes = compressedSizeBytes; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
