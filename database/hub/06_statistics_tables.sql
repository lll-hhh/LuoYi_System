-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 数据统计与分析表
-- =====================================================

-- =====================================================
-- 40. 数据类型表 (data_type)
-- =====================================================
CREATE TABLE data_type (
    data_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    unit VARCHAR(20),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE data_type IS '数据类型表';

INSERT INTO data_type (type_name, type_code, unit, description) VALUES
('车流量', 'TRAFFIC_FLOW', '辆', '单位时间内通过的车辆数'),
('平均车速', 'AVG_SPEED', 'km/h', '平均行驶速度'),
('拥堵指数', 'CONGESTION_INDEX', '', '拥堵程度指标'),
('货物吞吐量', 'CARGO_THROUGHPUT', '吨', '货物进出总量'),
('客流量', 'PASSENGER_FLOW', '人', '乘客进出总量'),
('停车场使用率', 'PARKING_USAGE', '%', '停车位使用率'),
('异常事件数', 'ANOMALY_COUNT', '次', '异常事件发生次数'),
('设备在线率', 'DEVICE_ONLINE_RATE', '%', '设备正常运行比例');

-- =====================================================
-- 41. 道路小时汇总表 (road_hour_sumlog)
-- =====================================================
CREATE TABLE road_hour_sumlog (
    id SERIAL PRIMARY KEY,
    road_id INTEGER REFERENCES road(road_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    stat_hour TIMESTAMP NOT NULL,
    total_vehicles INTEGER DEFAULT 0,
    car_count INTEGER DEFAULT 0,
    bus_count INTEGER DEFAULT 0,
    truck_count INTEGER DEFAULT 0,
    motorcycle_count INTEGER DEFAULT 0,
    avg_speed DECIMAL(6, 2),
    max_speed DECIMAL(6, 2),
    min_speed DECIMAL(6, 2),
    congestion_index DECIMAL(4, 2),
    congestion_duration INTEGER DEFAULT 0,
    anomaly_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(road_id, camera_id, stat_hour)
);

COMMENT ON TABLE road_hour_sumlog IS '道路小时汇总表';
COMMENT ON COLUMN road_hour_sumlog.stat_hour IS '统计小时(整点)';
COMMENT ON COLUMN road_hour_sumlog.congestion_duration IS '拥堵持续时间(分钟)';

CREATE INDEX idx_road_hour_road ON road_hour_sumlog(road_id);
CREATE INDEX idx_road_hour_time ON road_hour_sumlog(stat_hour);

-- =====================================================
-- 42. 道路日汇总表 (road_day_sumlog)
-- =====================================================
CREATE TABLE road_day_sumlog (
    id SERIAL PRIMARY KEY,
    road_id INTEGER REFERENCES road(road_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    stat_date DATE NOT NULL,
    total_vehicles INTEGER DEFAULT 0,
    car_count INTEGER DEFAULT 0,
    bus_count INTEGER DEFAULT 0,
    truck_count INTEGER DEFAULT 0,
    peak_hour_morning TIME,
    peak_vehicles_morning INTEGER,
    peak_hour_evening TIME,
    peak_vehicles_evening INTEGER,
    avg_speed DECIMAL(6, 2),
    congestion_index_avg DECIMAL(4, 2),
    congestion_total_minutes INTEGER DEFAULT 0,
    anomaly_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(road_id, camera_id, stat_date)
);

COMMENT ON TABLE road_day_sumlog IS '道路日汇总表';

CREATE INDEX idx_road_day_road ON road_day_sumlog(road_id);
CREATE INDEX idx_road_day_date ON road_day_sumlog(stat_date);

-- =====================================================
-- 43. 道路周汇总表 (road_week_sumlog)
-- =====================================================
CREATE TABLE road_week_sumlog (
    id SERIAL PRIMARY KEY,
    road_id INTEGER REFERENCES road(road_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    stat_year INTEGER NOT NULL,
    stat_week INTEGER NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    total_vehicles INTEGER DEFAULT 0,
    daily_avg_vehicles INTEGER,
    peak_day VARCHAR(20),
    peak_day_vehicles INTEGER,
    avg_speed DECIMAL(6, 2),
    congestion_index_avg DECIMAL(4, 2),
    anomaly_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(road_id, camera_id, stat_year, stat_week)
);

COMMENT ON TABLE road_week_sumlog IS '道路周汇总表';

CREATE INDEX idx_road_week_road ON road_week_sumlog(road_id);
CREATE INDEX idx_road_week_year ON road_week_sumlog(stat_year, stat_week);

-- =====================================================
-- 44. 道路月汇总表 (road_month_sumlog)
-- =====================================================
CREATE TABLE road_month_sumlog (
    id SERIAL PRIMARY KEY,
    road_id INTEGER REFERENCES road(road_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    stat_year INTEGER NOT NULL,
    stat_month INTEGER NOT NULL,
    total_vehicles INTEGER DEFAULT 0,
    daily_avg_vehicles INTEGER,
    peak_date DATE,
    peak_date_vehicles INTEGER,
    avg_speed DECIMAL(6, 2),
    congestion_index_avg DECIMAL(4, 2),
    yoy_growth_rate DECIMAL(6, 2),
    mom_growth_rate DECIMAL(6, 2),
    anomaly_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(road_id, camera_id, stat_year, stat_month)
);

COMMENT ON TABLE road_month_sumlog IS '道路月汇总表';
COMMENT ON COLUMN road_month_sumlog.yoy_growth_rate IS '同比增长率';
COMMENT ON COLUMN road_month_sumlog.mom_growth_rate IS '环比增长率';

CREATE INDEX idx_road_month_road ON road_month_sumlog(road_id);
CREATE INDEX idx_road_month_year ON road_month_sumlog(stat_year, stat_month);

-- =====================================================
-- 45. 道路年汇总表 (road_year_sumlog)
-- =====================================================
CREATE TABLE road_year_sumlog (
    id SERIAL PRIMARY KEY,
    road_id INTEGER REFERENCES road(road_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    stat_year INTEGER NOT NULL,
    total_vehicles BIGINT DEFAULT 0,
    monthly_avg_vehicles INTEGER,
    peak_month INTEGER,
    peak_month_vehicles INTEGER,
    avg_speed DECIMAL(6, 2),
    congestion_index_avg DECIMAL(4, 2),
    yoy_growth_rate DECIMAL(6, 2),
    anomaly_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(road_id, camera_id, stat_year)
);

COMMENT ON TABLE road_year_sumlog IS '道路年汇总表';

CREATE INDEX idx_road_year_road ON road_year_sumlog(road_id);
CREATE INDEX idx_road_year_year ON road_year_sumlog(stat_year);

-- =====================================================
-- 46. 日数据存储表 (day_data_store)
-- =====================================================
CREATE TABLE day_data_store (
    id SERIAL PRIMARY KEY,
    data_type_id INTEGER REFERENCES data_type(data_type_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    stat_date DATE NOT NULL,
    value DECIMAL(15, 4),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(data_type_id, entity_type, entity_id, stat_date)
);

COMMENT ON TABLE day_data_store IS '日数据存储表';
COMMENT ON COLUMN day_data_store.entity_type IS '实体类型: ROAD/JUNCTION/PARKING/WAREHOUSE';

CREATE INDEX idx_day_data_type ON day_data_store(data_type_id);
CREATE INDEX idx_day_data_date ON day_data_store(stat_date);
CREATE INDEX idx_day_data_entity ON day_data_store(entity_type, entity_id);

-- =====================================================
-- 47. 周数据存储表 (week_data_store)
-- =====================================================
CREATE TABLE week_data_store (
    id SERIAL PRIMARY KEY,
    data_type_id INTEGER REFERENCES data_type(data_type_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    stat_year INTEGER NOT NULL,
    stat_week INTEGER NOT NULL,
    value DECIMAL(15, 4),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(data_type_id, entity_type, entity_id, stat_year, stat_week)
);

COMMENT ON TABLE week_data_store IS '周数据存储表';

CREATE INDEX idx_week_data_type ON week_data_store(data_type_id);
CREATE INDEX idx_week_data_year ON week_data_store(stat_year, stat_week);

-- =====================================================
-- 48. 月数据存储表 (month_data_store)
-- =====================================================
CREATE TABLE month_data_store (
    id SERIAL PRIMARY KEY,
    data_type_id INTEGER REFERENCES data_type(data_type_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    stat_year INTEGER NOT NULL,
    stat_month INTEGER NOT NULL,
    value DECIMAL(15, 4),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(data_type_id, entity_type, entity_id, stat_year, stat_month)
);

COMMENT ON TABLE month_data_store IS '月数据存储表';

CREATE INDEX idx_month_data_type ON month_data_store(data_type_id);
CREATE INDEX idx_month_data_year ON month_data_store(stat_year, stat_month);

-- =====================================================
-- 49. 年数据存储表 (year_data_store)
-- =====================================================
CREATE TABLE year_data_store (
    id SERIAL PRIMARY KEY,
    data_type_id INTEGER REFERENCES data_type(data_type_id),
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    stat_year INTEGER NOT NULL,
    value DECIMAL(15, 4),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(data_type_id, entity_type, entity_id, stat_year)
);

COMMENT ON TABLE year_data_store IS '年数据存储表';

CREATE INDEX idx_year_data_type ON year_data_store(data_type_id);
CREATE INDEX idx_year_data_year ON year_data_store(stat_year);

-- =====================================================
-- 50. 自主记录汇总表 (self_record_sumlog)
-- =====================================================
CREATE TABLE self_record_sumlog (
    id SERIAL PRIMARY KEY,
    camera_id INTEGER REFERENCES camera(camera_id),
    record_time TIMESTAMP NOT NULL,
    snapshot_url VARCHAR(500),
    video_url VARCHAR(500),
    duration_seconds INTEGER,
    file_size_mb DECIMAL(10, 2),
    detected_objects JSONB,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE self_record_sumlog IS '自主记录汇总表';
COMMENT ON COLUMN self_record_sumlog.detected_objects IS '检测到的目标(JSON格式)';

CREATE INDEX idx_self_record_camera ON self_record_sumlog(camera_id);
CREATE INDEX idx_self_record_time ON self_record_sumlog(record_time);

-- =====================================================
-- 51. 第三方数据表 (third_part_data)
-- =====================================================
CREATE TABLE third_part_data (
    third_part_data_id SERIAL PRIMARY KEY,
    data_type_id INTEGER REFERENCES data_type(data_type_id),
    source_name VARCHAR(100) NOT NULL,
    source_type VARCHAR(50),
    data_content JSONB NOT NULL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    status VARCHAR(20) DEFAULT 'VALID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE third_part_data IS '第三方数据表';
COMMENT ON COLUMN third_part_data.source_type IS '来源类型: WEATHER/TRAFFIC/EVENT/GOV';

CREATE INDEX idx_third_data_type ON third_part_data(data_type_id);
CREATE INDEX idx_third_data_source ON third_part_data(source_name);
CREATE INDEX idx_third_data_time ON third_part_data(received_at);

-- =====================================================
-- 52. 第三方数据汇总表 (third_part_data_sumlog)
-- =====================================================
CREATE TABLE third_part_data_sumlog (
    id SERIAL PRIMARY KEY,
    third_part_data_id INTEGER REFERENCES third_part_data(third_part_data_id),
    stat_date DATE NOT NULL,
    source_name VARCHAR(100),
    record_count INTEGER DEFAULT 0,
    summary JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE third_part_data_sumlog IS '第三方数据汇总表';

CREATE INDEX idx_third_sumlog_date ON third_part_data_sumlog(stat_date);
