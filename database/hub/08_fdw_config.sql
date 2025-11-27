-- ================================================
-- 络绎智慧交通管理系统 - PostgreSQL 外部数据包装器(FDW)配置
-- 实现多数据库跨库查询和数据联邦
-- ================================================

-- ================================================
-- 1. 安装必要的扩展
-- ================================================

-- postgres_fdw: 用于连接其他PostgreSQL数据库
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- file_fdw: 用于访问服务器上的CSV/文本文件
CREATE EXTENSION IF NOT EXISTS file_fdw;

-- ================================================
-- 2. 创建外部服务器连接
-- ================================================

-- 2.1 Hub数据库连接 (枢纽端)
CREATE SERVER IF NOT EXISTS hub_server
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (
        host 'hub-db',
        port '5432',
        dbname 'lorries_hub'
    );

-- 2.2 Mobile数据库连接 (移动端)
CREATE SERVER IF NOT EXISTS mobile_server
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (
        host 'mobile-db',
        port '5432',
        dbname 'lorries_mobile'
    );

-- 2.3 历史数据归档服务器
CREATE SERVER IF NOT EXISTS archive_server
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (
        host 'archive-db',
        port '5432',
        dbname 'lorries_archive'
    );

-- 2.4 文件服务器 (用于导入CSV数据)
CREATE SERVER IF NOT EXISTS file_server
    FOREIGN DATA WRAPPER file_fdw;

-- ================================================
-- 3. 创建用户映射
-- ================================================

-- 3.1 Hub服务器用户映射
CREATE USER MAPPING IF NOT EXISTS FOR CURRENT_USER
    SERVER hub_server
    OPTIONS (
        user 'postgres',
        password 'postgres'
    );

-- 3.2 Mobile服务器用户映射
CREATE USER MAPPING IF NOT EXISTS FOR CURRENT_USER
    SERVER mobile_server
    OPTIONS (
        user 'postgres',
        password 'postgres'
    );

-- 3.3 Archive服务器用户映射
CREATE USER MAPPING IF NOT EXISTS FOR CURRENT_USER
    SERVER archive_server
    OPTIONS (
        user 'postgres',
        password 'postgres'
    );

-- ================================================
-- 4. 创建外部表 - Hub数据库
-- ================================================

-- 4.1 车辆信息外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_hub_vehicles (
    id BIGSERIAL,
    plate_number VARCHAR(20),
    vehicle_type VARCHAR(20),
    brand VARCHAR(50),
    model VARCHAR(50),
    color VARCHAR(20),
    owner_id BIGINT,
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)
SERVER hub_server
OPTIONS (
    schema_name 'public',
    table_name 'vehicles'
);

-- 4.2 道路信息外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_hub_roads (
    id BIGSERIAL,
    road_name VARCHAR(100),
    road_type VARCHAR(20),
    start_point_lat DECIMAL(10, 7),
    start_point_lng DECIMAL(10, 7),
    end_point_lat DECIMAL(10, 7),
    end_point_lng DECIMAL(10, 7),
    length_km DECIMAL(10, 3),
    lanes INT,
    speed_limit INT,
    status VARCHAR(20),
    created_at TIMESTAMP
)
SERVER hub_server
OPTIONS (
    schema_name 'public',
    table_name 'roads'
);

-- 4.3 交通流量外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_hub_traffic_flow (
    id BIGSERIAL,
    road_id BIGINT,
    record_time TIMESTAMP,
    vehicle_count INT,
    avg_speed DECIMAL(10, 2),
    congestion_index DECIMAL(5, 2),
    travel_time_index DECIMAL(5, 2)
)
SERVER hub_server
OPTIONS (
    schema_name 'public',
    table_name 'traffic_flow_records'
);

-- 4.4 摄像头信息外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_hub_cameras (
    id BIGSERIAL,
    camera_name VARCHAR(100),
    camera_type VARCHAR(50),
    road_id BIGINT,
    location_lat DECIMAL(10, 7),
    location_lng DECIMAL(10, 7),
    rtmp_url VARCHAR(255),
    hls_url VARCHAR(255),
    status VARCHAR(20),
    created_at TIMESTAMP
)
SERVER hub_server
OPTIONS (
    schema_name 'public',
    table_name 'cameras'
);

-- ================================================
-- 5. 创建外部表 - Mobile数据库
-- ================================================

-- 5.1 司机信息外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_mobile_drivers (
    id BIGSERIAL,
    user_id BIGINT,
    driver_name VARCHAR(50),
    phone VARCHAR(20),
    license_number VARCHAR(30),
    license_type VARCHAR(10),
    vehicle_id BIGINT,
    status VARCHAR(20),
    created_at TIMESTAMP
)
SERVER mobile_server
OPTIONS (
    schema_name 'public',
    table_name 'drivers'
);

-- 5.2 运输任务外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_mobile_tasks (
    id BIGSERIAL,
    task_number VARCHAR(50),
    driver_id BIGINT,
    vehicle_id BIGINT,
    cargo_id BIGINT,
    start_location VARCHAR(255),
    end_location VARCHAR(255),
    planned_start_time TIMESTAMP,
    actual_start_time TIMESTAMP,
    planned_end_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    status VARCHAR(20),
    created_at TIMESTAMP
)
SERVER mobile_server
OPTIONS (
    schema_name 'public',
    table_name 'transport_tasks'
);

-- 5.3 位置记录外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_mobile_locations (
    id BIGSERIAL,
    task_id BIGINT,
    driver_id BIGINT,
    vehicle_id BIGINT,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    speed DECIMAL(10, 2),
    heading DECIMAL(5, 2),
    recorded_at TIMESTAMP
)
SERVER mobile_server
OPTIONS (
    schema_name 'public',
    table_name 'location_records'
);

-- ================================================
-- 6. 创建外部表 - Archive数据库 (历史归档)
-- ================================================

-- 6.1 历史交通流量外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_archive_traffic_flow (
    id BIGSERIAL,
    road_id BIGINT,
    record_time TIMESTAMP,
    vehicle_count INT,
    avg_speed DECIMAL(10, 2),
    congestion_index DECIMAL(5, 2),
    archive_date DATE
)
SERVER archive_server
OPTIONS (
    schema_name 'public',
    table_name 'archived_traffic_flow'
);

-- 6.2 历史车牌识别记录外部表
CREATE FOREIGN TABLE IF NOT EXISTS fdw_archive_plate_records (
    id BIGSERIAL,
    camera_id BIGINT,
    plate_number VARCHAR(20),
    capture_time TIMESTAMP,
    vehicle_type VARCHAR(20),
    confidence DECIMAL(5, 2),
    archive_date DATE
)
SERVER archive_server
OPTIONS (
    schema_name 'public',
    table_name 'archived_plate_records'
);

-- ================================================
-- 7. 创建联邦视图 (跨库查询)
-- ================================================

-- 7.1 实时+历史交通流量联合视图
CREATE OR REPLACE VIEW v_all_traffic_flow AS
SELECT 
    id,
    road_id,
    record_time,
    vehicle_count,
    avg_speed,
    congestion_index,
    'current' as data_source
FROM fdw_hub_traffic_flow
WHERE record_time >= CURRENT_DATE - INTERVAL '30 days'
UNION ALL
SELECT 
    id,
    road_id,
    record_time,
    vehicle_count,
    avg_speed,
    congestion_index,
    'archive' as data_source
FROM fdw_archive_traffic_flow
WHERE archive_date >= CURRENT_DATE - INTERVAL '1 year';

-- 7.2 车辆运行状态联合视图
CREATE OR REPLACE VIEW v_vehicle_status AS
SELECT 
    v.id,
    v.plate_number,
    v.vehicle_type,
    v.brand,
    v.status as vehicle_status,
    d.driver_name,
    d.phone as driver_phone,
    t.task_number,
    t.status as task_status,
    l.latitude,
    l.longitude,
    l.speed,
    l.recorded_at as last_location_time
FROM fdw_hub_vehicles v
LEFT JOIN fdw_mobile_drivers d ON d.vehicle_id = v.id
LEFT JOIN fdw_mobile_tasks t ON t.vehicle_id = v.id AND t.status = 'in_progress'
LEFT JOIN LATERAL (
    SELECT * FROM fdw_mobile_locations 
    WHERE vehicle_id = v.id 
    ORDER BY recorded_at DESC 
    LIMIT 1
) l ON true;

-- 7.3 道路摄像头监控视图
CREATE OR REPLACE VIEW v_road_cameras AS
SELECT 
    r.id as road_id,
    r.road_name,
    r.road_type,
    c.id as camera_id,
    c.camera_name,
    c.camera_type,
    c.rtmp_url,
    c.hls_url,
    c.status as camera_status,
    c.location_lat,
    c.location_lng
FROM fdw_hub_roads r
LEFT JOIN fdw_hub_cameras c ON c.road_id = r.id
WHERE r.status = 'active';

-- ================================================
-- 8. 创建CSV文件导入外部表
-- ================================================

-- 8.1 批量导入车辆数据
CREATE FOREIGN TABLE IF NOT EXISTS csv_import_vehicles (
    plate_number VARCHAR(20),
    vehicle_type VARCHAR(20),
    brand VARCHAR(50),
    model VARCHAR(50),
    color VARCHAR(20),
    owner_name VARCHAR(50),
    owner_phone VARCHAR(20)
)
SERVER file_server
OPTIONS (
    filename '/data/import/vehicles.csv',
    format 'csv',
    header 'true',
    delimiter ','
);

-- 8.2 批量导入道路数据
CREATE FOREIGN TABLE IF NOT EXISTS csv_import_roads (
    road_name VARCHAR(100),
    road_type VARCHAR(20),
    start_lat DECIMAL(10, 7),
    start_lng DECIMAL(10, 7),
    end_lat DECIMAL(10, 7),
    end_lng DECIMAL(10, 7),
    length_km DECIMAL(10, 3),
    lanes INT,
    speed_limit INT
)
SERVER file_server
OPTIONS (
    filename '/data/import/roads.csv',
    format 'csv',
    header 'true',
    delimiter ','
);

-- ================================================
-- 9. 辅助函数
-- ================================================

-- 9.1 刷新外部表统计信息
CREATE OR REPLACE FUNCTION refresh_fdw_stats()
RETURNS void AS $$
BEGIN
    ANALYZE fdw_hub_vehicles;
    ANALYZE fdw_hub_roads;
    ANALYZE fdw_hub_traffic_flow;
    ANALYZE fdw_hub_cameras;
    ANALYZE fdw_mobile_drivers;
    ANALYZE fdw_mobile_tasks;
    ANALYZE fdw_mobile_locations;
    RAISE NOTICE 'FDW statistics refreshed successfully';
END;
$$ LANGUAGE plpgsql;

-- 9.2 测试FDW连接
CREATE OR REPLACE FUNCTION test_fdw_connection(server_name TEXT)
RETURNS TABLE(status TEXT, message TEXT) AS $$
DECLARE
    test_query TEXT;
BEGIN
    CASE server_name
        WHEN 'hub_server' THEN
            PERFORM 1 FROM fdw_hub_vehicles LIMIT 1;
            RETURN QUERY SELECT 'SUCCESS'::TEXT, 'Hub server connection OK'::TEXT;
        WHEN 'mobile_server' THEN
            PERFORM 1 FROM fdw_mobile_drivers LIMIT 1;
            RETURN QUERY SELECT 'SUCCESS'::TEXT, 'Mobile server connection OK'::TEXT;
        WHEN 'archive_server' THEN
            PERFORM 1 FROM fdw_archive_traffic_flow LIMIT 1;
            RETURN QUERY SELECT 'SUCCESS'::TEXT, 'Archive server connection OK'::TEXT;
        ELSE
            RETURN QUERY SELECT 'ERROR'::TEXT, 'Unknown server: ' || server_name;
    END CASE;
EXCEPTION WHEN OTHERS THEN
    RETURN QUERY SELECT 'ERROR'::TEXT, SQLERRM;
END;
$$ LANGUAGE plpgsql;

-- ================================================
-- 10. 授权配置
-- ================================================

-- 授予应用用户外部表查询权限
-- GRANT USAGE ON FOREIGN SERVER hub_server TO app_user;
-- GRANT USAGE ON FOREIGN SERVER mobile_server TO app_user;
-- GRANT USAGE ON FOREIGN SERVER archive_server TO app_user;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_user;

COMMENT ON EXTENSION postgres_fdw IS '络绎系统跨数据库联邦查询支持';
