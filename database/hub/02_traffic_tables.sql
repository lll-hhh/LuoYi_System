-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 交通管理表
-- =====================================================

-- =====================================================
-- 9. 道路表 (road)
-- =====================================================
CREATE TABLE road (
    road_id SERIAL PRIMARY KEY,
    road_no VARCHAR(50) NOT NULL UNIQUE,
    road_name VARCHAR(100) NOT NULL,
    road_type VARCHAR(50),
    length_meters DECIMAL(10, 2),
    lane_count INTEGER DEFAULT 2,
    speed_limit INTEGER DEFAULT 60,
    direction VARCHAR(20),
    start_point VARCHAR(200),
    end_point VARCHAR(200),
    geometry JSONB,
    status VARCHAR(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE road IS '道路表';
COMMENT ON COLUMN road.road_type IS '道路类型: MAIN/SECONDARY/BRANCH';
COMMENT ON COLUMN road.direction IS '方向: ONE_WAY/TWO_WAY';
COMMENT ON COLUMN road.status IS '状态: NORMAL/CONGESTED/CLOSED/CONSTRUCTION';
COMMENT ON COLUMN road.geometry IS '道路几何信息(GeoJSON格式)';

CREATE INDEX idx_road_status ON road(status);
CREATE INDEX idx_road_type ON road(road_type);

-- =====================================================
-- 10. 路口表 (junction)
-- =====================================================
CREATE TABLE junction (
    junction_id SERIAL PRIMARY KEY,
    junction_no VARCHAR(50) NOT NULL UNIQUE,
    junction_name VARCHAR(100) NOT NULL,
    junction_type VARCHAR(50),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    signal_config JSONB,
    status VARCHAR(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE junction IS '路口表';
COMMENT ON COLUMN junction.junction_type IS '路口类型: CROSS/T_JUNCTION/ROUNDABOUT';
COMMENT ON COLUMN junction.signal_config IS '信号灯配置(JSON格式)';

-- =====================================================
-- 11. 路口-道路关联表 (junction_road)
-- =====================================================
CREATE TABLE junction_road (
    id SERIAL PRIMARY KEY,
    junction_id INTEGER NOT NULL REFERENCES junction(junction_id),
    road_id INTEGER NOT NULL REFERENCES road(road_id),
    direction VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(junction_id, road_id)
);

COMMENT ON TABLE junction_road IS '路口-道路关联表';

-- =====================================================
-- 12. 出入口表 (entries) - 入口
-- =====================================================
CREATE TABLE entries (
    entry_id SERIAL PRIMARY KEY,
    entry_no VARCHAR(50) NOT NULL UNIQUE,
    entry_name VARCHAR(100) NOT NULL,
    entry_type VARCHAR(50),
    connected_road_id INTEGER REFERENCES road(road_id),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    capacity INTEGER,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE entries IS '入口表';
COMMENT ON COLUMN entries.entry_type IS '入口类型: MAIN/SECONDARY/EMERGENCY';
COMMENT ON COLUMN entries.status IS '状态: OPEN/CLOSED/LIMITED';

-- =====================================================
-- 13. 出口表 (exits)
-- =====================================================
CREATE TABLE exits (
    exit_id SERIAL PRIMARY KEY,
    exit_no VARCHAR(50) NOT NULL UNIQUE,
    exit_name VARCHAR(100) NOT NULL,
    exit_type VARCHAR(50),
    connected_road_id INTEGER REFERENCES road(road_id),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    capacity INTEGER,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE exits IS '出口表';

-- =====================================================
-- 14. 路线类型表 (route_type)
-- =====================================================
CREATE TABLE route_type (
    route_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE route_type IS '路线类型表';

INSERT INTO route_type (type_name, type_code, description, priority) VALUES
('货运路线', 'CARGO', '货物运输专用路线', 1),
('客运路线', 'PASSENGER', '客运车辆路线', 2),
('特种车辆路线', 'SPECIAL', '危化品等特种车辆路线', 3),
('普通路线', 'NORMAL', '普通车辆路线', 0);

-- =====================================================
-- 15. 路线表 (route)
-- =====================================================
CREATE TABLE route (
    route_id SERIAL PRIMARY KEY,
    route_no VARCHAR(50) NOT NULL UNIQUE,
    route_name VARCHAR(100) NOT NULL,
    route_type_id INTEGER REFERENCES route_type(route_type_id),
    start_point VARCHAR(200),
    end_point VARCHAR(200),
    distance_meters DECIMAL(10, 2),
    estimated_time INTEGER,
    waypoints JSONB,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE route IS '路线表';
COMMENT ON COLUMN route.estimated_time IS '预计通行时间(秒)';
COMMENT ON COLUMN route.waypoints IS '途经点列表(JSON格式)';

-- =====================================================
-- 16. 路线-道路关联表 (route_road)
-- =====================================================
CREATE TABLE route_road (
    id SERIAL PRIMARY KEY,
    route_id INTEGER NOT NULL REFERENCES route(route_id),
    road_id INTEGER NOT NULL REFERENCES road(road_id),
    sequence_order INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(route_id, road_id)
);

COMMENT ON TABLE route_road IS '路线-道路关联表';
COMMENT ON COLUMN route_road.sequence_order IS '道路在路线中的顺序';

-- =====================================================
-- 17. 节点-摄像头连接表 (node_camera_connect)
-- =====================================================
CREATE TABLE node_camera_connect (
    id SERIAL PRIMARY KEY,
    camera_id INTEGER NOT NULL REFERENCES camera(camera_id),
    node_type VARCHAR(20) NOT NULL,
    node_id INTEGER NOT NULL,
    coverage_area JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE node_camera_connect IS '节点-摄像头连接表';
COMMENT ON COLUMN node_camera_connect.node_type IS '节点类型: JUNCTION/ENTRY/EXIT/ROAD';

CREATE INDEX idx_node_camera_type ON node_camera_connect(node_type, node_id);

-- =====================================================
-- 18. 车辆类型表 (vehicle_type)
-- =====================================================
CREATE TABLE vehicle_type (
    vehicle_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50),
    description TEXT,
    icon_url VARCHAR(500),
    is_dangerous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE vehicle_type IS '车辆类型表';
COMMENT ON COLUMN vehicle_type.category IS '分类: CAR/BUS/TRUCK/SPECIAL';
COMMENT ON COLUMN vehicle_type.is_dangerous IS '是否为危险车辆类型';

INSERT INTO vehicle_type (type_name, type_code, category, is_dangerous, description) VALUES
('小型轿车', 'CAR', 'CAR', FALSE, '私家车、出租车等'),
('中型客车', 'MID_BUS', 'BUS', FALSE, '中型客运车辆'),
('大型客车', 'LARGE_BUS', 'BUS', FALSE, '大型客运车辆'),
('小型货车', 'LIGHT_TRUCK', 'TRUCK', FALSE, '轻型货运车辆'),
('重型货车', 'HEAVY_TRUCK', 'TRUCK', FALSE, '重型货运车辆'),
('油罐车', 'TANKER', 'SPECIAL', TRUE, '危险品运输车辆'),
('危化品车', 'HAZMAT', 'SPECIAL', TRUE, '危险化学品运输车'),
('摩托车', 'MOTORCYCLE', 'CAR', FALSE, '两轮机动车'),
('自行车', 'BICYCLE', 'CAR', FALSE, '非机动车');

-- =====================================================
-- 19. 已报备车辆表 (reported_vehicle)
-- =====================================================
CREATE TABLE reported_vehicle (
    reported_vehicle_id SERIAL PRIMARY KEY,
    plate_no VARCHAR(20) NOT NULL,
    vehicle_type_id INTEGER REFERENCES vehicle_type(vehicle_type_id),
    owner_name VARCHAR(100),
    owner_phone VARCHAR(20),
    company_name VARCHAR(200),
    route_id INTEGER REFERENCES route(route_id),
    purpose VARCHAR(200),
    expected_entry_time TIMESTAMP,
    expected_exit_time TIMESTAMP,
    actual_entry_time TIMESTAMP,
    actual_exit_time TIMESTAMP,
    entry_id INTEGER REFERENCES entries(entry_id),
    exit_id INTEGER REFERENCES exits(exit_id),
    status VARCHAR(20) DEFAULT 'PENDING',
    cargo_info JSONB,
    passenger_info JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reported_vehicle IS '已报备车辆表';
COMMENT ON COLUMN reported_vehicle.status IS '状态: PENDING/APPROVED/IN_TRANSIT/COMPLETED/CANCELLED';

CREATE INDEX idx_reported_vehicle_plate ON reported_vehicle(plate_no);
CREATE INDEX idx_reported_vehicle_status ON reported_vehicle(status);
CREATE INDEX idx_reported_vehicle_entry_time ON reported_vehicle(expected_entry_time);

-- =====================================================
-- 20. 未报备车辆表 (unreported_vehicle)
-- =====================================================
CREATE TABLE unreported_vehicle (
    unreported_vehicle_id SERIAL PRIMARY KEY,
    plate_no VARCHAR(20),
    vehicle_type_id INTEGER REFERENCES vehicle_type(vehicle_type_id),
    first_detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_detected_at TIMESTAMP,
    detection_count INTEGER DEFAULT 1,
    camera_id INTEGER REFERENCES camera(camera_id),
    snapshot_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'DETECTED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE unreported_vehicle IS '未报备车辆表';
COMMENT ON COLUMN unreported_vehicle.status IS '状态: DETECTED/TRACKING/HANDLED';

CREATE INDEX idx_unreported_vehicle_plate ON unreported_vehicle(plate_no);
CREATE INDEX idx_unreported_vehicle_status ON unreported_vehicle(status);
