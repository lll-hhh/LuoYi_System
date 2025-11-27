package com.lorries.hub.service.impl;

import com.lorries.hub.service.DataTieringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据分层转储服务实现
 * 实现冷热数据分离和自动归档
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataTieringServiceImpl implements DataTieringService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public ArchiveResult archiveTable(String tableName) {
        ArchiveResult result = new ArchiveResult();
        result.setTableName(tableName);
        result.setStartTime(LocalDateTime.now());

        try {
            // 获取分层配置
            TieringConfig config = getTieringConfig(tableName);
            if (config == null) {
                result.setSuccess(false);
                result.setErrorMessage("未找到表 " + tableName + " 的分层配置");
                return result;
            }

            if (!config.isArchiveEnabled()) {
                result.setSuccess(false);
                result.setErrorMessage("表 " + tableName + " 的归档功能未启用");
                return result;
            }

            // 调用PostgreSQL存储过程执行归档
            String sql = "SELECT * FROM archive_table_data(?, ?, ?, ?)";
            Map<String, Object> archiveResult = jdbcTemplate.queryForMap(sql,
                    tableName,
                    "archived_" + tableName,
                    config.getPartitionColumn(),
                    config.getColdDays());

            long archivedRows = ((Number) archiveResult.get("archived_rows")).longValue();
            long deletedRows = ((Number) archiveResult.get("deleted_rows")).longValue();

            result.setArchivedRows(archivedRows);
            result.setDeletedRows(deletedRows);
            result.setSuccess(true);
            result.setEndTime(LocalDateTime.now());

            log.info("表 {} 归档完成，归档 {} 行，删除 {} 行", tableName, archivedRows, deletedRows);

        } catch (Exception e) {
            log.error("表 {} 归档失败", tableName, e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
        }

        return result;
    }

    @Override
    @Transactional
    public List<ArchiveResult> archiveAllTables() {
        List<ArchiveResult> results = new ArrayList<>();

        List<TieringConfig> configs = getAllTieringConfigs();
        for (TieringConfig config : configs) {
            if (config.isArchiveEnabled()) {
                ArchiveResult result = archiveTable(config.getTableName());
                results.add(result);
            }
        }

        return results;
    }

    @Override
    public TieringConfig getTieringConfig(String tableName) {
        String sql = "SELECT * FROM data_tiering_config WHERE table_name = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                TieringConfig config = new TieringConfig();
                config.setId(rs.getLong("id"));
                config.setTableName(rs.getString("table_name"));
                config.setHotDays(rs.getInt("hot_days"));
                config.setWarmDays(rs.getInt("warm_days"));
                config.setColdDays(rs.getInt("cold_days"));
                config.setArchiveEnabled(rs.getBoolean("archive_enabled"));
                config.setCompressionEnabled(rs.getBoolean("compression_enabled"));
                config.setPartitionColumn(rs.getString("partition_column"));
                Timestamp lastArchive = rs.getTimestamp("last_archive_time");
                if (lastArchive != null) {
                    config.setLastArchiveTime(lastArchive.toLocalDateTime());
                }
                config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return config;
            }, tableName);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void updateTieringConfig(TieringConfig config) {
        String sql = "UPDATE data_tiering_config SET " +
                "hot_days = ?, warm_days = ?, cold_days = ?, " +
                "archive_enabled = ?, compression_enabled = ?, " +
                "partition_column = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE table_name = ?";

        jdbcTemplate.update(sql,
                config.getHotDays(),
                config.getWarmDays(),
                config.getColdDays(),
                config.isArchiveEnabled(),
                config.isCompressionEnabled(),
                config.getPartitionColumn(),
                config.getTableName());

        log.info("更新表 {} 的分层配置", config.getTableName());
    }

    @Override
    public List<TieringConfig> getAllTieringConfigs() {
        String sql = "SELECT * FROM data_tiering_config ORDER BY table_name";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TieringConfig config = new TieringConfig();
            config.setId(rs.getLong("id"));
            config.setTableName(rs.getString("table_name"));
            config.setHotDays(rs.getInt("hot_days"));
            config.setWarmDays(rs.getInt("warm_days"));
            config.setColdDays(rs.getInt("cold_days"));
            config.setArchiveEnabled(rs.getBoolean("archive_enabled"));
            config.setCompressionEnabled(rs.getBoolean("compression_enabled"));
            config.setPartitionColumn(rs.getString("partition_column"));
            Timestamp lastArchive = rs.getTimestamp("last_archive_time");
            if (lastArchive != null) {
                config.setLastArchiveTime(lastArchive.toLocalDateTime());
            }
            config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return config;
        });
    }

    @Override
    @Transactional
    public void createFuturePartitions(String tableName, int monthsAhead) {
        jdbcTemplate.execute(String.format("SELECT create_future_partitions('%s', %d)", tableName, monthsAhead));
        log.info("为表 {} 创建了未来 {} 个月的分区", tableName, monthsAhead);
    }

    @Override
    @Transactional
    public void dropOldPartitions(String tableName, int monthsToKeep) {
        jdbcTemplate.execute(String.format("SELECT drop_old_partitions('%s', %d)", tableName, monthsToKeep));
        log.info("清理表 {} 超过 {} 个月的分区", tableName, monthsToKeep);
    }

    @Override
    @Transactional
    public void compressArchiveData(String tableName) {
        try {
            String sql = "VACUUM FULL archived_" + tableName;
            jdbcTemplate.execute(sql);
            log.info("压缩归档表 archived_{}", tableName);
        } catch (Exception e) {
            log.error("压缩归档表失败", e);
        }
    }

    @Override
    public List<TieringStatus> getTieringStatus() {
        String sql = "SELECT * FROM get_tiering_status()";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TieringStatus status = new TieringStatus();
            status.setTableName(rs.getString("table_name"));
            status.setHotDataSize(rs.getString("hot_data_size"));
            status.setWarmDataSize(rs.getString("warm_data_size"));
            status.setColdDataSize(rs.getString("cold_data_size"));
            status.setArchiveSize(rs.getString("archive_size"));
            status.setTotalRows(rs.getLong("total_rows"));
            Timestamp lastArchive = rs.getTimestamp("last_archive_time");
            if (lastArchive != null) {
                status.setLastArchiveTime(lastArchive.toLocalDateTime());
            }
            return status;
        });
    }

    @Override
    public List<ArchiveJobLog> getArchiveHistory(int days) {
        String sql = "SELECT * FROM get_archive_history(?)";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ArchiveJobLog log = new ArchiveJobLog();
            log.setJobName(rs.getString("job_name"));
            log.setTableName(rs.getString("table_name"));
            log.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            Timestamp endTime = rs.getTimestamp("end_time");
            if (endTime != null) {
                log.setEndTime(endTime.toLocalDateTime());
            }
            log.setRowsArchived(rs.getLong("rows_archived"));
            log.setStatus(rs.getString("status"));
            return log;
        }, days);
    }

    @Override
    public List<ArchiveStatistics> getArchiveStatistics(String tableName, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM archive_statistics " +
                "WHERE table_name = ? AND archive_date BETWEEN ? AND ? " +
                "ORDER BY archive_date DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ArchiveStatistics stats = new ArchiveStatistics();
            stats.setId(rs.getLong("id"));
            stats.setTableName(rs.getString("table_name"));
            stats.setArchiveDate(rs.getDate("archive_date").toLocalDate());
            stats.setTotalRows(rs.getLong("total_rows"));
            stats.setDataSizeBytes(rs.getLong("data_size_bytes"));
            stats.setCompressedSizeBytes(rs.getLong("compressed_size_bytes"));
            stats.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return stats;
        }, tableName, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
    }

    @Override
    public Map<String, Object> queryArchivedData(String tableName, LocalDate startDate, LocalDate endDate, int pageNum, int pageSize) {
        Map<String, Object> result = new HashMap<>();

        String archiveTable = "archived_" + tableName;
        int offset = (pageNum - 1) * pageSize;

        // 查询总数
        String countSql = String.format(
                "SELECT COUNT(*) FROM %s WHERE archive_date BETWEEN ? AND ?",
                archiveTable);
        Long total = jdbcTemplate.queryForObject(countSql, Long.class,
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));

        // 查询数据
        String dataSql = String.format(
                "SELECT * FROM %s WHERE archive_date BETWEEN ? AND ? ORDER BY archive_date DESC LIMIT ? OFFSET ?",
                archiveTable);
        List<Map<String, Object>> data = jdbcTemplate.queryForList(dataSql,
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate), pageSize, offset);

        result.put("total", total);
        result.put("data", data);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("pages", (total + pageSize - 1) / pageSize);

        return result;
    }

    @Override
    @Transactional
    public long restoreArchivedData(String tableName, LocalDate startDate, LocalDate endDate) {
        String archiveTable = "archived_" + tableName;

        // 先计算要恢复的行数
        String countSql = String.format(
                "SELECT COUNT(*) FROM %s WHERE archive_date BETWEEN ? AND ?",
                archiveTable);
        Long count = jdbcTemplate.queryForObject(countSql, Long.class,
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));

        if (count == null || count == 0) {
            return 0;
        }

        // 从归档表恢复数据到原表
        String restoreSql = String.format(
                "INSERT INTO %s SELECT * FROM %s WHERE archive_date BETWEEN ? AND ?",
                tableName, archiveTable);
        jdbcTemplate.update(restoreSql,
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));

        // 删除归档表中已恢复的数据
        String deleteSql = String.format(
                "DELETE FROM %s WHERE archive_date BETWEEN ? AND ?",
                archiveTable);
        jdbcTemplate.update(deleteSql,
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));

        log.info("从归档表 {} 恢复 {} 行数据", archiveTable, count);
        return count;
    }

    /**
     * 定时任务：每天凌晨2点执行数据归档
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledArchive() {
        log.info("开始执行定时归档任务");
        List<ArchiveResult> results = archiveAllTables();
        
        int successCount = 0;
        int failCount = 0;
        long totalArchived = 0;
        
        for (ArchiveResult result : results) {
            if (result.isSuccess()) {
                successCount++;
                totalArchived += result.getArchivedRows();
            } else {
                failCount++;
                log.warn("表 {} 归档失败: {}", result.getTableName(), result.getErrorMessage());
            }
        }
        
        log.info("定时归档任务完成，成功 {} 个表，失败 {} 个表，共归档 {} 行",
                successCount, failCount, totalArchived);
    }

    /**
     * 定时任务：每月1号凌晨1点创建新分区
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void scheduledCreatePartitions() {
        log.info("开始创建新分区");
        
        List<String> archiveTables = Arrays.asList(
                "archived_traffic_flow",
                "archived_plate_records",
                "archived_vehicle_locations"
        );
        
        for (String table : archiveTables) {
            try {
                createFuturePartitions(table, 3);
            } catch (Exception e) {
                log.error("创建分区失败: {}", table, e);
            }
        }
    }

    /**
     * 定时任务：每周日凌晨3点清理过期分区
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void scheduledCleanPartitions() {
        log.info("开始清理过期分区");
        
        List<String> archiveTables = Arrays.asList(
                "archived_traffic_flow",
                "archived_plate_records",
                "archived_vehicle_locations"
        );
        
        for (String table : archiveTables) {
            try {
                dropOldPartitions(table, 24); // 保留24个月
            } catch (Exception e) {
                log.error("清理分区失败: {}", table, e);
            }
        }
    }
}
