-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 仓储物流表
-- =====================================================

-- =====================================================
-- 21. 仓库表 (warehouse)
-- =====================================================
CREATE TABLE warehouse (
    warehouse_id SERIAL PRIMARY KEY,
    warehouse_no VARCHAR(50) NOT NULL UNIQUE,
    warehouse_name VARCHAR(100) NOT NULL,
    warehouse_type VARCHAR(50),
    address VARCHAR(200),
    road_id INTEGER REFERENCES road(road_id),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    total_capacity DECIMAL(12, 2),
    used_capacity DECIMAL(12, 2) DEFAULT 0,
    manager_id INTEGER REFERENCES employee(employee_id),
    contact_phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE warehouse IS '仓库表';
COMMENT ON COLUMN warehouse.warehouse_type IS '仓库类型: NORMAL/COLD_CHAIN/DANGEROUS';
COMMENT ON COLUMN warehouse.total_capacity IS '总容量(立方米)';

CREATE INDEX idx_warehouse_status ON warehouse(status);
CREATE INDEX idx_warehouse_road ON warehouse(road_id);

-- =====================================================
-- 22. 货物类型表 (cargo_type)
-- =====================================================
CREATE TABLE cargo_type (
    cargo_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50),
    is_dangerous BOOLEAN DEFAULT FALSE,
    storage_requirement TEXT,
    handling_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE cargo_type IS '货物类型表';
COMMENT ON COLUMN cargo_type.category IS '分类: NORMAL/COLD_CHAIN/DANGEROUS/FRAGILE';

INSERT INTO cargo_type (type_name, type_code, category, is_dangerous, storage_requirement) VALUES
('普通货物', 'NORMAL', 'NORMAL', FALSE, '常温存储'),
('冷链货物', 'COLD_CHAIN', 'COLD_CHAIN', FALSE, '需冷藏存储，温度0-4℃'),
('危险品', 'DANGEROUS', 'DANGEROUS', TRUE, '需隔离存储，专人管理'),
('易碎品', 'FRAGILE', 'FRAGILE', FALSE, '轻拿轻放，防震存储'),
('贵重物品', 'VALUABLE', 'NORMAL', FALSE, '安全存储，需监控');

-- =====================================================
-- 23. 货物存储表 (cargo_store)
-- =====================================================
CREATE TABLE cargo_store (
    cargo_store_id SERIAL PRIMARY KEY,
    cargo_no VARCHAR(50) NOT NULL UNIQUE,
    cargo_name VARCHAR(200) NOT NULL,
    cargo_type_id INTEGER REFERENCES cargo_type(cargo_type_id),
    warehouse_id INTEGER REFERENCES warehouse(warehouse_id),
    location_code VARCHAR(50),
    quantity DECIMAL(12, 2) NOT NULL,
    unit VARCHAR(20),
    weight_kg DECIMAL(10, 2),
    volume_m3 DECIMAL(10, 2),
    batch_no VARCHAR(50),
    production_date DATE,
    expiry_date DATE,
    owner_company VARCHAR(200),
    status VARCHAR(20) DEFAULT 'IN_STOCK',
    in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    out_time TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE cargo_store IS '货物存储表';
COMMENT ON COLUMN cargo_store.status IS '状态: IN_STOCK/OUT_STOCK/RESERVED/DAMAGED';

CREATE INDEX idx_cargo_store_warehouse ON cargo_store(warehouse_id);
CREATE INDEX idx_cargo_store_type ON cargo_store(cargo_type_id);
CREATE INDEX idx_cargo_store_status ON cargo_store(status);

-- =====================================================
-- 24. 货物运输表 (cargo_trans)
-- =====================================================
CREATE TABLE cargo_trans (
    cargo_trans_id SERIAL PRIMARY KEY,
    trans_no VARCHAR(50) NOT NULL UNIQUE,
    cargo_type_id INTEGER REFERENCES cargo_type(cargo_type_id),
    reported_vehicle_id INTEGER REFERENCES reported_vehicle(reported_vehicle_id),
    warehouse_id INTEGER REFERENCES warehouse(warehouse_id),
    trans_type VARCHAR(20) NOT NULL,
    origin VARCHAR(200),
    destination VARCHAR(200),
    cargo_description TEXT,
    quantity DECIMAL(12, 2),
    weight_kg DECIMAL(10, 2),
    scheduled_time TIMESTAMP,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    current_location VARCHAR(200),
    status VARCHAR(20) DEFAULT 'PENDING',
    driver_name VARCHAR(100),
    driver_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE cargo_trans IS '货物运输表';
COMMENT ON COLUMN cargo_trans.trans_type IS '运输类型: INBOUND/OUTBOUND/TRANSFER';
COMMENT ON COLUMN cargo_trans.status IS '状态: PENDING/IN_TRANSIT/ARRIVED/COMPLETED/CANCELLED';

CREATE INDEX idx_cargo_trans_vehicle ON cargo_trans(reported_vehicle_id);
CREATE INDEX idx_cargo_trans_status ON cargo_trans(status);
CREATE INDEX idx_cargo_trans_time ON cargo_trans(scheduled_time);

-- =====================================================
-- 25. 乘客入口表 (passenger_entrance)
-- =====================================================
CREATE TABLE passenger_entrance (
    passenger_entrance_id SERIAL PRIMARY KEY,
    entrance_no VARCHAR(50) NOT NULL UNIQUE,
    entrance_name VARCHAR(100) NOT NULL,
    entrance_type VARCHAR(50),
    road_id INTEGER REFERENCES road(road_id),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    capacity INTEGER,
    facilities JSONB,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE passenger_entrance IS '乘客入口表(上下车点)';
COMMENT ON COLUMN passenger_entrance.entrance_type IS '类型: BUS_STOP/TAXI_STAND/DROP_OFF';

-- =====================================================
-- 26. 乘客运输表 (passenger_trans)
-- =====================================================
CREATE TABLE passenger_trans (
    passenger_trans_id SERIAL PRIMARY KEY,
    trans_no VARCHAR(50) NOT NULL UNIQUE,
    reported_vehicle_id INTEGER REFERENCES reported_vehicle(reported_vehicle_id),
    passenger_entrance_id INTEGER REFERENCES passenger_entrance(passenger_entrance_id),
    passenger_count INTEGER,
    special_passenger_count INTEGER DEFAULT 0,
    special_passenger_types JSONB,
    origin VARCHAR(200),
    destination VARCHAR(200),
    scheduled_time TIMESTAMP,
    actual_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE passenger_trans IS '乘客运输表';
COMMENT ON COLUMN passenger_trans.special_passenger_types IS '特殊乘客类型(JSON): 老人/儿童/残障人士等';

CREATE INDEX idx_passenger_trans_vehicle ON passenger_trans(reported_vehicle_id);
CREATE INDEX idx_passenger_trans_status ON passenger_trans(status);

-- =====================================================
-- 27. 停车场表 (parking)
-- =====================================================
CREATE TABLE parking (
    parking_id SERIAL PRIMARY KEY,
    parking_no VARCHAR(50) NOT NULL UNIQUE,
    parking_name VARCHAR(100) NOT NULL,
    parking_type VARCHAR(50),
    road_id INTEGER REFERENCES road(road_id),
    address VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    total_spaces INTEGER NOT NULL,
    available_spaces INTEGER,
    hourly_rate DECIMAL(8, 2),
    daily_max_rate DECIMAL(8, 2),
    opening_hours VARCHAR(50),
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE parking IS '停车场表';
COMMENT ON COLUMN parking.parking_type IS '类型: UNDERGROUND/SURFACE/MULTI_LEVEL';

CREATE INDEX idx_parking_status ON parking(status);

-- =====================================================
-- 28. 停车位设置表 (parking_set)
-- =====================================================
CREATE TABLE parking_set (
    parking_set_id SERIAL PRIMARY KEY,
    parking_id INTEGER NOT NULL REFERENCES parking(parking_id),
    space_no VARCHAR(20) NOT NULL,
    space_type VARCHAR(20),
    floor_level INTEGER,
    zone VARCHAR(20),
    is_reserved BOOLEAN DEFAULT FALSE,
    reserved_for VARCHAR(100),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(parking_id, space_no)
);

COMMENT ON TABLE parking_set IS '停车位设置表';
COMMENT ON COLUMN parking_set.space_type IS '车位类型: STANDARD/LARGE/DISABLED/CHARGING';
COMMENT ON COLUMN parking_set.status IS '状态: AVAILABLE/OCCUPIED/RESERVED/MAINTENANCE';

CREATE INDEX idx_parking_set_status ON parking_set(status);

-- =====================================================
-- 29. 停车记录表 (parking_log)
-- =====================================================
CREATE TABLE parking_log (
    parking_log_id SERIAL PRIMARY KEY,
    parking_id INTEGER NOT NULL REFERENCES parking(parking_id),
    parking_set_id INTEGER REFERENCES parking_set(parking_set_id),
    plate_no VARCHAR(20),
    vehicle_type VARCHAR(50),
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    duration_minutes INTEGER,
    fee DECIMAL(10, 2),
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    payment_method VARCHAR(20),
    entry_camera_id INTEGER REFERENCES camera(camera_id),
    exit_camera_id INTEGER REFERENCES camera(camera_id),
    entry_snapshot_url VARCHAR(500),
    exit_snapshot_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE parking_log IS '停车记录表';
COMMENT ON COLUMN parking_log.payment_status IS '支付状态: UNPAID/PAID/FREE';

CREATE INDEX idx_parking_log_plate ON parking_log(plate_no);
CREATE INDEX idx_parking_log_time ON parking_log(entry_time);
CREATE INDEX idx_parking_log_parking ON parking_log(parking_id);
