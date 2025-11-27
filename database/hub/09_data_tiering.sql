-- ================================================
-- 络绎智慧交通管理系统 - 数据分层与归档策略
-- 实现冷热数据分离和自动归档
-- ================================================

-- ================================================
-- 1. 创建数据分层相关表
-- ================================================

-- 1.1 数据分层配置表
CREATE TABLE IF NOT EXISTS data_tiering_config (
    id SERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL UNIQUE,
    hot_days INT DEFAULT 7,          -- 热数据保留天数
    warm_days INT DEFAULT 30,        -- 温数据保留天数
    cold_days INT DEFAULT 365,       -- 冷数据保留天数(归档前)
    archive_enabled BOOLEAN DEFAULT true,
    compression_enabled BOOLEAN DEFAULT true,
    partition_column VARCHAR(50) DEFAULT 'created_at',
    last_archive_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 1.2 归档任务日志表
CREATE TABLE IF NOT EXISTS archive_job_logs (
    id SERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    rows_archived BIGINT DEFAULT 0,
    rows_deleted BIGINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'running',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 1.3 归档数据统计表
CREATE TABLE IF NOT EXISTS archive_statistics (
    id SERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    archive_date DATE NOT NULL,
    total_rows BIGINT,
    data_size_bytes BIGINT,
    compressed_size_bytes BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(table_name, archive_date)
);

-- ================================================
-- 2. 初始化分层配置
-- ================================================

INSERT INTO data_tiering_config (table_name, hot_days, warm_days, cold_days, partition_column)
VALUES 
    ('traffic_flow_records', 7, 30, 365, 'record_time'),
    ('plate_recognition_records', 7, 30, 180, 'capture_time'),
    ('vehicle_locations', 3, 14, 90, 'recorded_at'),
    ('anomaly_events', 30, 90, 365, 'created_at'),
    ('system_logs', 7, 30, 90, 'log_time'),
    ('operation_logs', 14, 60, 180, 'operation_time')
ON CONFLICT (table_name) DO NOTHING;

-- ================================================
-- 3. 创建归档表
-- ================================================

-- 3.1 归档交通流量表 (按月分区)
CREATE TABLE IF NOT EXISTS archived_traffic_flow (
    id BIGINT,
    road_id BIGINT,
    record_time TIMESTAMP,
    vehicle_count INT,
    avg_speed DECIMAL(10, 2),
    congestion_index DECIMAL(5, 2),
    travel_time_index DECIMAL(5, 2),
    archive_date DATE NOT NULL,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (archive_date);

-- 创建月度分区 (示例：最近12个月)
DO $$
DECLARE
    start_date DATE := date_trunc('month', CURRENT_DATE - INTERVAL '12 months');
    end_date DATE := date_trunc('month', CURRENT_DATE + INTERVAL '1 month');
    partition_date DATE := start_date;
    partition_name TEXT;
BEGIN
    WHILE partition_date < end_date LOOP
        partition_name := 'archived_traffic_flow_' || to_char(partition_date, 'YYYY_MM');
        
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS %I PARTITION OF archived_traffic_flow 
             FOR VALUES FROM (%L) TO (%L)',
            partition_name,
            partition_date,
            partition_date + INTERVAL '1 month'
        );
        
        partition_date := partition_date + INTERVAL '1 month';
    END LOOP;
END $$;

-- 3.2 归档车牌识别表
CREATE TABLE IF NOT EXISTS archived_plate_records (
    id BIGINT,
    camera_id BIGINT,
    plate_number VARCHAR(20),
    capture_time TIMESTAMP,
    vehicle_type VARCHAR(20),
    confidence DECIMAL(5, 2),
    image_url VARCHAR(255),
    archive_date DATE NOT NULL,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (archive_date);

-- 3.3 归档位置记录表
CREATE TABLE IF NOT EXISTS archived_vehicle_locations (
    id BIGINT,
    vehicle_id BIGINT,
    driver_id BIGINT,
    task_id BIGINT,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    speed DECIMAL(10, 2),
    heading DECIMAL(5, 2),
    recorded_at TIMESTAMP,
    archive_date DATE NOT NULL,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (archive_date);

-- ================================================
-- 4. 核心归档函数
-- ================================================

-- 4.1 通用数据归档函数
CREATE OR REPLACE FUNCTION archive_table_data(
    p_source_table TEXT,
    p_archive_table TEXT,
    p_date_column TEXT,
    p_days_to_keep INT
)
RETURNS TABLE(archived_rows BIGINT, deleted_rows BIGINT) AS $$
DECLARE
    v_cutoff_date TIMESTAMP;
    v_archived BIGINT := 0;
    v_deleted BIGINT := 0;
    v_batch_size INT := 10000;
    v_job_id INT;
BEGIN
    v_cutoff_date := CURRENT_TIMESTAMP - (p_days_to_keep || ' days')::INTERVAL;
    
    -- 记录任务开始
    INSERT INTO archive_job_logs (job_name, table_name, start_time, status)
    VALUES ('archive_' || p_source_table, p_source_table, CURRENT_TIMESTAMP, 'running')
    RETURNING id INTO v_job_id;
    
    BEGIN
        -- 分批归档数据
        LOOP
            EXECUTE format(
                'WITH moved_rows AS (
                    DELETE FROM %I
                    WHERE %I < $1
                    AND ctid IN (
                        SELECT ctid FROM %I
                        WHERE %I < $1
                        LIMIT $2
                    )
                    RETURNING *
                )
                INSERT INTO %I 
                SELECT *, CURRENT_DATE as archive_date
                FROM moved_rows',
                p_source_table,
                p_date_column,
                p_source_table,
                p_date_column,
                p_archive_table
            ) USING v_cutoff_date, v_batch_size;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            v_archived := v_archived + v_deleted;
            
            EXIT WHEN v_deleted < v_batch_size;
            
            -- 提交当前批次，避免长事务
            COMMIT;
        END LOOP;
        
        -- 更新配置表
        UPDATE data_tiering_config 
        SET last_archive_time = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE table_name = p_source_table;
        
        -- 更新任务日志
        UPDATE archive_job_logs
        SET end_time = CURRENT_TIMESTAMP,
            rows_archived = v_archived,
            rows_deleted = v_archived,
            status = 'completed'
        WHERE id = v_job_id;
        
        -- 记录统计信息
        INSERT INTO archive_statistics (table_name, archive_date, total_rows)
        VALUES (p_source_table, CURRENT_DATE, v_archived)
        ON CONFLICT (table_name, archive_date) 
        DO UPDATE SET total_rows = archive_statistics.total_rows + EXCLUDED.total_rows;
        
    EXCEPTION WHEN OTHERS THEN
        UPDATE archive_job_logs
        SET end_time = CURRENT_TIMESTAMP,
            status = 'failed',
            error_message = SQLERRM
        WHERE id = v_job_id;
        RAISE;
    END;
    
    archived_rows := v_archived;
    deleted_rows := v_archived;
    RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

-- 4.2 执行所有表的归档
CREATE OR REPLACE FUNCTION run_all_archives()
RETURNS TABLE(
    table_name TEXT,
    archived_rows BIGINT,
    status TEXT
) AS $$
DECLARE
    r RECORD;
    v_result RECORD;
BEGIN
    FOR r IN 
        SELECT * FROM data_tiering_config 
        WHERE archive_enabled = true
    LOOP
        BEGIN
            SELECT * INTO v_result FROM archive_table_data(
                r.table_name,
                'archived_' || r.table_name,
                r.partition_column,
                r.cold_days
            );
            
            RETURN QUERY SELECT r.table_name, v_result.archived_rows, 'success'::TEXT;
        EXCEPTION WHEN OTHERS THEN
            RETURN QUERY SELECT r.table_name, 0::BIGINT, ('error: ' || SQLERRM)::TEXT;
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ================================================
-- 5. 分区管理函数
-- ================================================

-- 5.1 自动创建新分区
CREATE OR REPLACE FUNCTION create_future_partitions(
    p_table_name TEXT,
    p_months_ahead INT DEFAULT 3
)
RETURNS void AS $$
DECLARE
    v_start_date DATE;
    v_end_date DATE;
    v_partition_date DATE;
    v_partition_name TEXT;
BEGIN
    v_start_date := date_trunc('month', CURRENT_DATE);
    v_end_date := date_trunc('month', CURRENT_DATE + (p_months_ahead || ' months')::INTERVAL);
    v_partition_date := v_start_date;
    
    WHILE v_partition_date < v_end_date LOOP
        v_partition_name := p_table_name || '_' || to_char(v_partition_date, 'YYYY_MM');
        
        IF NOT EXISTS (
            SELECT 1 FROM pg_class WHERE relname = v_partition_name
        ) THEN
            EXECUTE format(
                'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I
                 FOR VALUES FROM (%L) TO (%L)',
                v_partition_name,
                p_table_name,
                v_partition_date,
                v_partition_date + INTERVAL '1 month'
            );
            RAISE NOTICE 'Created partition: %', v_partition_name;
        END IF;
        
        v_partition_date := v_partition_date + INTERVAL '1 month';
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 5.2 删除过期分区
CREATE OR REPLACE FUNCTION drop_old_partitions(
    p_table_name TEXT,
    p_months_to_keep INT DEFAULT 24
)
RETURNS void AS $$
DECLARE
    v_cutoff_date DATE;
    v_partition RECORD;
BEGIN
    v_cutoff_date := date_trunc('month', CURRENT_DATE - (p_months_to_keep || ' months')::INTERVAL);
    
    FOR v_partition IN
        SELECT c.relname as partition_name
        FROM pg_inherits i
        JOIN pg_class c ON c.oid = i.inhrelid
        JOIN pg_class p ON p.oid = i.inhparent
        WHERE p.relname = p_table_name
    LOOP
        -- 检查分区日期是否在截止日期之前
        IF v_partition.partition_name ~ '_\d{4}_\d{2}$' THEN
            DECLARE
                v_partition_date DATE;
            BEGIN
                v_partition_date := to_date(
                    substring(v_partition.partition_name from '_(\d{4}_\d{2})$'),
                    'YYYY_MM'
                );
                
                IF v_partition_date < v_cutoff_date THEN
                    EXECUTE format('DROP TABLE IF EXISTS %I', v_partition.partition_name);
                    RAISE NOTICE 'Dropped partition: %', v_partition.partition_name;
                END IF;
            EXCEPTION WHEN OTHERS THEN
                RAISE NOTICE 'Could not process partition %: %', v_partition.partition_name, SQLERRM;
            END;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ================================================
-- 6. 数据压缩函数
-- ================================================

-- 6.1 压缩归档分区
CREATE OR REPLACE FUNCTION compress_archive_partitions()
RETURNS void AS $$
DECLARE
    v_partition RECORD;
BEGIN
    -- 对已归档的分区进行压缩
    FOR v_partition IN
        SELECT c.relname as partition_name
        FROM pg_inherits i
        JOIN pg_class c ON c.oid = i.inhrelid
        JOIN pg_class p ON p.oid = i.inhparent
        WHERE p.relname LIKE 'archived_%'
    LOOP
        BEGIN
            -- 使用CLUSTER重新组织数据以获得更好的压缩
            EXECUTE format('CLUSTER %I', v_partition.partition_name);
            EXECUTE format('VACUUM FULL %I', v_partition.partition_name);
            RAISE NOTICE 'Compressed partition: %', v_partition.partition_name;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not compress %: %', v_partition.partition_name, SQLERRM;
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ================================================
-- 7. 定时任务配置 (需要pg_cron扩展)
-- ================================================

-- 注意：以下SQL需要在安装pg_cron扩展后执行
-- CREATE EXTENSION IF NOT EXISTS pg_cron;

-- 每天凌晨2点执行归档
-- SELECT cron.schedule('daily-archive', '0 2 * * *', $$SELECT run_all_archives()$$);

-- 每月1号创建新分区
-- SELECT cron.schedule('monthly-partition', '0 0 1 * *', $$SELECT create_future_partitions('archived_traffic_flow', 3)$$);

-- 每周日凌晨3点压缩归档数据
-- SELECT cron.schedule('weekly-compress', '0 3 * * 0', $$SELECT compress_archive_partitions()$$);

-- ================================================
-- 8. 监控和报告函数
-- ================================================

-- 8.1 获取数据分层状态报告
CREATE OR REPLACE FUNCTION get_tiering_status()
RETURNS TABLE(
    table_name TEXT,
    hot_data_size TEXT,
    warm_data_size TEXT,
    cold_data_size TEXT,
    archive_size TEXT,
    total_rows BIGINT,
    last_archive_time TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.table_name::TEXT,
        pg_size_pretty(pg_table_size(c.table_name::regclass))::TEXT as hot_data_size,
        'N/A'::TEXT as warm_data_size,
        'N/A'::TEXT as cold_data_size,
        COALESCE(
            pg_size_pretty(pg_table_size(('archived_' || c.table_name)::regclass)),
            'N/A'
        )::TEXT as archive_size,
        (SELECT COUNT(*) FROM pg_stat_user_tables WHERE relname = c.table_name)::BIGINT as total_rows,
        c.last_archive_time
    FROM data_tiering_config c;
END;
$$ LANGUAGE plpgsql;

-- 8.2 获取归档任务历史
CREATE OR REPLACE FUNCTION get_archive_history(p_days INT DEFAULT 7)
RETURNS TABLE(
    job_name TEXT,
    table_name TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration INTERVAL,
    rows_archived BIGINT,
    status TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        l.job_name::TEXT,
        l.table_name::TEXT,
        l.start_time,
        l.end_time,
        l.end_time - l.start_time as duration,
        l.rows_archived,
        l.status::TEXT
    FROM archive_job_logs l
    WHERE l.created_at >= CURRENT_TIMESTAMP - (p_days || ' days')::INTERVAL
    ORDER BY l.start_time DESC;
END;
$$ LANGUAGE plpgsql;

-- ================================================
-- 9. 索引优化
-- ================================================

CREATE INDEX IF NOT EXISTS idx_archive_traffic_flow_date ON archived_traffic_flow (archive_date);
CREATE INDEX IF NOT EXISTS idx_archive_traffic_flow_road ON archived_traffic_flow (road_id, record_time);
CREATE INDEX IF NOT EXISTS idx_archive_plate_records_date ON archived_plate_records (archive_date);
CREATE INDEX IF NOT EXISTS idx_archive_plate_records_plate ON archived_plate_records (plate_number, capture_time);
CREATE INDEX IF NOT EXISTS idx_archive_locations_date ON archived_vehicle_locations (archive_date);
CREATE INDEX IF NOT EXISTS idx_archive_locations_vehicle ON archived_vehicle_locations (vehicle_id, recorded_at);

COMMENT ON TABLE data_tiering_config IS '数据分层配置表，定义各表的热温冷数据策略';
COMMENT ON TABLE archive_job_logs IS '归档任务执行日志';
COMMENT ON TABLE archive_statistics IS '归档数据统计信息';
