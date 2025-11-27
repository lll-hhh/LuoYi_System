-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 监控与异常表
-- =====================================================

-- =====================================================
-- 30. 已报备车辆通行日志表 (reported_vehicle_passage_log)
-- =====================================================
CREATE TABLE reported_vehicle_passage_log (
    log_id SERIAL PRIMARY KEY,
    reported_vehicle_id INTEGER NOT NULL REFERENCES reported_vehicle(reported_vehicle_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    passage_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    direction VARCHAR(20),
    speed_kmh DECIMAL(6, 2),
    snapshot_url VARCHAR(500),
    confidence DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reported_vehicle_passage_log IS '已报备车辆通行日志表';

CREATE INDEX idx_reported_passage_vehicle ON reported_vehicle_passage_log(reported_vehicle_id);
CREATE INDEX idx_reported_passage_time ON reported_vehicle_passage_log(passage_time);
CREATE INDEX idx_reported_passage_camera ON reported_vehicle_passage_log(camera_id);

-- =====================================================
-- 31. 未报备车辆进出日志表 (unreported_vehicle_io_log)
-- =====================================================
CREATE TABLE unreported_vehicle_io_log (
    log_id SERIAL PRIMARY KEY,
    unreported_vehicle_id INTEGER NOT NULL REFERENCES unreported_vehicle(unreported_vehicle_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    log_type VARCHAR(20) NOT NULL,
    log_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(200),
    direction VARCHAR(20),
    snapshot_url VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE unreported_vehicle_io_log IS '未报备车辆进出日志表';
COMMENT ON COLUMN unreported_vehicle_io_log.log_type IS '日志类型: ENTRY/EXIT/PASS';

CREATE INDEX idx_unreported_io_vehicle ON unreported_vehicle_io_log(unreported_vehicle_id);
CREATE INDEX idx_unreported_io_time ON unreported_vehicle_io_log(log_time);

-- =====================================================
-- 32. 交通异常类型表 (traffic_anomaly_type)
-- =====================================================
CREATE TABLE traffic_anomaly_type (
    anomaly_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    severity VARCHAR(20),
    category VARCHAR(50),
    description TEXT,
    handling_guide TEXT,
    auto_notify BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE traffic_anomaly_type IS '交通异常类型表';
COMMENT ON COLUMN traffic_anomaly_type.severity IS '严重程度: LOW/MEDIUM/HIGH/CRITICAL';
COMMENT ON COLUMN traffic_anomaly_type.category IS '分类: VEHICLE/ROAD/EQUIPMENT/OTHER';

INSERT INTO traffic_anomaly_type (type_name, type_code, severity, category, description) VALUES
('车辆超速', 'OVER_SPEED', 'MEDIUM', 'VEHICLE', '车辆速度超过限速'),
('车辆停留过长', 'LONG_STAY', 'LOW', 'VEHICLE', '车辆在某区域停留时间过长'),
('路径偏离', 'ROUTE_DEVIATION', 'HIGH', 'VEHICLE', '车辆偏离报备路线'),
('非法进入', 'ILLEGAL_ENTRY', 'HIGH', 'VEHICLE', '未报备车辆进入管控区域'),
('交通拥堵', 'CONGESTION', 'MEDIUM', 'ROAD', '道路出现拥堵'),
('交通事故', 'ACCIDENT', 'CRITICAL', 'ROAD', '发生交通事故'),
('设备故障', 'EQUIPMENT_FAILURE', 'MEDIUM', 'EQUIPMENT', '监控设备故障'),
('信号灯故障', 'SIGNAL_FAILURE', 'HIGH', 'EQUIPMENT', '交通信号灯故障'),
('道路损坏', 'ROAD_DAMAGE', 'HIGH', 'ROAD', '道路设施损坏'),
('危险品泄漏', 'HAZMAT_LEAK', 'CRITICAL', 'VEHICLE', '危险品运输车辆泄漏');

-- =====================================================
-- 33. 已报备车辆异常表 (reported_vehicle_anomaly)
-- =====================================================
CREATE TABLE reported_vehicle_anomaly (
    anomaly_id SERIAL PRIMARY KEY,
    reported_vehicle_id INTEGER NOT NULL REFERENCES reported_vehicle(reported_vehicle_id),
    anomaly_type_id INTEGER NOT NULL REFERENCES traffic_anomaly_type(anomaly_type_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    description TEXT,
    evidence_urls JSONB,
    status VARCHAR(20) DEFAULT 'PENDING',
    handler_id INTEGER REFERENCES employee(employee_id),
    handled_at TIMESTAMP,
    handling_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reported_vehicle_anomaly IS '已报备车辆异常表';
COMMENT ON COLUMN reported_vehicle_anomaly.status IS '状态: PENDING/HANDLING/RESOLVED/IGNORED';

CREATE INDEX idx_reported_anomaly_vehicle ON reported_vehicle_anomaly(reported_vehicle_id);
CREATE INDEX idx_reported_anomaly_type ON reported_vehicle_anomaly(anomaly_type_id);
CREATE INDEX idx_reported_anomaly_status ON reported_vehicle_anomaly(status);
CREATE INDEX idx_reported_anomaly_time ON reported_vehicle_anomaly(detected_at);

-- =====================================================
-- 34. 未报备车辆异常表 (unreported_vehicle_anomaly)
-- =====================================================
CREATE TABLE unreported_vehicle_anomaly (
    anomaly_id SERIAL PRIMARY KEY,
    unreported_vehicle_id INTEGER NOT NULL REFERENCES unreported_vehicle(unreported_vehicle_id),
    anomaly_type_id INTEGER NOT NULL REFERENCES traffic_anomaly_type(anomaly_type_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    description TEXT,
    evidence_urls JSONB,
    status VARCHAR(20) DEFAULT 'PENDING',
    handler_id INTEGER REFERENCES employee(employee_id),
    handled_at TIMESTAMP,
    handling_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE unreported_vehicle_anomaly IS '未报备车辆异常表';

CREATE INDEX idx_unreported_anomaly_vehicle ON unreported_vehicle_anomaly(unreported_vehicle_id);
CREATE INDEX idx_unreported_anomaly_status ON unreported_vehicle_anomaly(status);

-- =====================================================
-- 35. 其他异常表 (other_anomaly)
-- =====================================================
CREATE TABLE other_anomaly (
    anomaly_id SERIAL PRIMARY KEY,
    anomaly_type_id INTEGER NOT NULL REFERENCES traffic_anomaly_type(anomaly_type_id),
    camera_id INTEGER REFERENCES camera(camera_id),
    road_id INTEGER REFERENCES road(road_id),
    junction_id INTEGER REFERENCES junction(junction_id),
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    description TEXT,
    evidence_urls JSONB,
    severity VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    reporter_id INTEGER REFERENCES employee(employee_id),
    handler_id INTEGER REFERENCES employee(employee_id),
    handled_at TIMESTAMP,
    handling_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE other_anomaly IS '其他异常表(非车辆相关)';

CREATE INDEX idx_other_anomaly_type ON other_anomaly(anomaly_type_id);
CREATE INDEX idx_other_anomaly_status ON other_anomaly(status);
CREATE INDEX idx_other_anomaly_time ON other_anomaly(detected_at);
