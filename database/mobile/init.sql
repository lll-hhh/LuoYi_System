-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 移动端数据库
-- 全部11张表
-- =====================================================

-- =====================================================
-- 1. 用户表 (user)
-- =====================================================
CREATE TABLE "user" (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    real_name VARCHAR(50),
    id_card VARCHAR(18),
    avatar_url VARCHAR(500),
    gender VARCHAR(10),
    company_name VARCHAR(200),
    company_license VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    login_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".status IS '状态: ACTIVE/INACTIVE/BANNED';

CREATE INDEX idx_user_phone ON "user"(phone);
CREATE INDEX idx_user_status ON "user"(status);

-- =====================================================
-- 2. 角色表 (role)
-- =====================================================
CREATE TABLE role (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE role IS '角色表';

-- 初始化基本角色
INSERT INTO role (role_name, role_code, description, is_system) VALUES
('普通用户', 'USER', '普通注册用户', TRUE),
('司机', 'DRIVER', '运输车辆司机', TRUE),
('货主', 'CARGO_OWNER', '货物所有者', TRUE),
('VIP用户', 'VIP', 'VIP用户享有特权', TRUE);

-- =====================================================
-- 3. 用户角色关联表 (user_role)
-- =====================================================
CREATE TABLE user_role (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(user_id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES role(role_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id)
);

COMMENT ON TABLE user_role IS '用户角色关联表';

-- =====================================================
-- 4. 权限表 (permission)
-- =====================================================
CREATE TABLE permission (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE permission IS '权限表';

-- 初始化基本权限
INSERT INTO permission (permission_name, permission_code, description) VALUES
('查看路况', 'VIEW_TRAFFIC', '查看实时路况信息'),
('车辆申报', 'DECLARE_VEHICLE', '申报车辆信息'),
('货物申报', 'DECLARE_CARGO', '申报货物信息'),
('客流申报', 'DECLARE_PASSENGER', '申报乘客信息'),
('查看推荐路线', 'VIEW_ROUTE', '查看路线推荐'),
('接收通知', 'RECEIVE_NOTIFICATION', '接收系统通知'),
('查看历史记录', 'VIEW_HISTORY', '查看历史申报记录');

-- =====================================================
-- 5. 角色权限关联表 (role_permission)
-- =====================================================
CREATE TABLE role_permission (
    id SERIAL PRIMARY KEY,
    role_id INTEGER REFERENCES role(role_id) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES permission(permission_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);

COMMENT ON TABLE role_permission IS '角色权限关联表';

-- 初始化角色权限
INSERT INTO role_permission (role_id, permission_id) 
SELECT r.role_id, p.permission_id 
FROM role r, permission p 
WHERE r.role_code = 'USER';

INSERT INTO role_permission (role_id, permission_id) 
SELECT r.role_id, p.permission_id 
FROM role r, permission p 
WHERE r.role_code IN ('DRIVER', 'CARGO_OWNER', 'VIP');

-- =====================================================
-- 6. 车辆类型表 (vehicle_type)
-- =====================================================
CREATE TABLE vehicle_type (
    vehicle_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    type_code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE vehicle_type IS '车辆类型表';

INSERT INTO vehicle_type (type_name, type_code, description) VALUES
('小型客车', 'CAR', '载客9人以下'),
('大型客车', 'BUS', '载客9人以上'),
('小型货车', 'LIGHT_TRUCK', '载货2吨以下'),
('中型货车', 'MEDIUM_TRUCK', '载货2-8吨'),
('大型货车', 'HEAVY_TRUCK', '载货8吨以上'),
('危险品车', 'DANGEROUS_GOODS', '运输危险品车辆'),
('特种车辆', 'SPECIAL', '工程车、消防车等');

-- =====================================================
-- 7. 用户车辆表 (vehicle)
-- =====================================================
CREATE TABLE vehicle (
    vehicle_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(user_id),
    vehicle_type_id INTEGER REFERENCES vehicle_type(vehicle_type_id),
    plate_number VARCHAR(20) NOT NULL,
    plate_color VARCHAR(20),
    brand VARCHAR(50),
    model VARCHAR(50),
    color VARCHAR(20),
    vin VARCHAR(50),
    engine_number VARCHAR(50),
    register_date DATE,
    insurance_expire_date DATE,
    inspection_expire_date DATE,
    transport_license VARCHAR(50),
    transport_license_expire DATE,
    max_load_weight DECIMAL(10, 2),
    max_load_volume DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE vehicle IS '用户车辆表';
COMMENT ON COLUMN vehicle.plate_color IS '车牌颜色: 蓝/黄/绿/白';
COMMENT ON COLUMN vehicle.status IS '状态: ACTIVE/INACTIVE/EXPIRED';

CREATE INDEX idx_vehicle_user ON vehicle(user_id);
CREATE INDEX idx_vehicle_plate ON vehicle(plate_number);

-- =====================================================
-- 8. 交通枢纽类型表 (transportation_hub_type)
-- =====================================================
CREATE TABLE transportation_hub_type (
    hub_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    type_code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE transportation_hub_type IS '交通枢纽类型表';

INSERT INTO transportation_hub_type (type_name, type_code, description) VALUES
('火车站', 'RAILWAY', '铁路客运站'),
('汽车站', 'BUS_STATION', '长途汽车客运站'),
('机场', 'AIRPORT', '民用航空机场'),
('港口', 'PORT', '水运港口码头'),
('物流园区', 'LOGISTICS_PARK', '物流集散中心'),
('综合交通枢纽', 'COMPREHENSIVE', '多种交通方式换乘');

-- =====================================================
-- 9. 交通枢纽表 (transportation_hub)
-- =====================================================
CREATE TABLE transportation_hub (
    hub_id SERIAL PRIMARY KEY,
    hub_type_id INTEGER REFERENCES transportation_hub_type(hub_type_id),
    hub_name VARCHAR(100) NOT NULL,
    hub_code VARCHAR(50),
    address VARCHAR(300),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    contact_phone VARCHAR(20),
    business_hours VARCHAR(100),
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE transportation_hub IS '交通枢纽表';

CREATE INDEX idx_hub_type ON transportation_hub(hub_type_id);
CREATE INDEX idx_hub_location ON transportation_hub(latitude, longitude);

-- =====================================================
-- 10. 货物申报类型表 (cargo_declaration_type)
-- =====================================================
CREATE TABLE cargo_declaration_type (
    type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    type_code VARCHAR(20) NOT NULL UNIQUE,
    requires_special_permit BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE cargo_declaration_type IS '货物申报类型表';

INSERT INTO cargo_declaration_type (type_name, type_code, requires_special_permit, description) VALUES
('普通货物', 'NORMAL', FALSE, '一般商品货物'),
('生鲜食品', 'FRESH', FALSE, '需冷链运输'),
('危险化学品', 'HAZARDOUS', TRUE, '需危化品运输许可'),
('大件货物', 'OVERSIZE', TRUE, '超限超重货物'),
('贵重物品', 'VALUABLE', FALSE, '高价值货物'),
('活体动物', 'LIVESTOCK', TRUE, '活禽活畜运输');

-- =====================================================
-- 11. 货物申报表 (cargo_declaration)
-- =====================================================
CREATE TABLE cargo_declaration (
    declaration_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(user_id),
    vehicle_id INTEGER REFERENCES vehicle(vehicle_id),
    declaration_type_id INTEGER REFERENCES cargo_declaration_type(type_id),
    declaration_no VARCHAR(50) NOT NULL UNIQUE,
    cargo_name VARCHAR(200) NOT NULL,
    cargo_quantity INTEGER,
    cargo_weight DECIMAL(10, 2),
    cargo_volume DECIMAL(10, 2),
    cargo_value DECIMAL(15, 2),
    origin_hub_id INTEGER REFERENCES transportation_hub(hub_id),
    origin_address VARCHAR(300),
    destination_hub_id INTEGER REFERENCES transportation_hub(hub_id),
    destination_address VARCHAR(300),
    expected_departure TIMESTAMP,
    expected_arrival TIMESTAMP,
    actual_departure TIMESTAMP,
    actual_arrival TIMESTAMP,
    route_recommendation_id INTEGER,
    special_requirements TEXT,
    attached_documents JSONB,
    status VARCHAR(30) DEFAULT 'PENDING',
    review_result VARCHAR(20),
    review_comment TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE cargo_declaration IS '货物申报表';
COMMENT ON COLUMN cargo_declaration.status IS '状态: PENDING/APPROVED/REJECTED/IN_TRANSIT/COMPLETED/CANCELLED';
COMMENT ON COLUMN cargo_declaration.attached_documents IS '附件文档(JSON格式存储文件URL)';

CREATE INDEX idx_cargo_decl_user ON cargo_declaration(user_id);
CREATE INDEX idx_cargo_decl_vehicle ON cargo_declaration(vehicle_id);
CREATE INDEX idx_cargo_decl_status ON cargo_declaration(status);
CREATE INDEX idx_cargo_decl_time ON cargo_declaration(created_at);

-- =====================================================
-- 12. 特殊乘客类型表 (special_passenger_type)
-- =====================================================
CREATE TABLE special_passenger_type (
    type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    type_code VARCHAR(20) NOT NULL UNIQUE,
    requires_assistance BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE special_passenger_type IS '特殊乘客类型表';

INSERT INTO special_passenger_type (type_name, type_code, requires_assistance, description) VALUES
('普通乘客', 'NORMAL', FALSE, '无特殊需求'),
('老年人', 'ELDERLY', TRUE, '60岁以上老年人'),
('残障人士', 'DISABLED', TRUE, '肢体或感官障碍'),
('孕妇', 'PREGNANT', TRUE, '怀孕妇女'),
('儿童', 'CHILD', TRUE, '12岁以下儿童'),
('携宠乘客', 'WITH_PET', FALSE, '携带宠物的乘客');

-- =====================================================
-- 13. 乘客申报表 (passenger_declaration)
-- =====================================================
CREATE TABLE passenger_declaration (
    declaration_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(user_id),
    declaration_no VARCHAR(50) NOT NULL UNIQUE,
    passenger_count INTEGER DEFAULT 1,
    special_type_id INTEGER REFERENCES special_passenger_type(type_id),
    special_count INTEGER DEFAULT 0,
    origin_hub_id INTEGER REFERENCES transportation_hub(hub_id),
    origin_address VARCHAR(300),
    destination_hub_id INTEGER REFERENCES transportation_hub(hub_id),
    destination_address VARCHAR(300),
    travel_date DATE NOT NULL,
    travel_time TIME,
    transport_mode VARCHAR(50),
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    special_requirements TEXT,
    status VARCHAR(30) DEFAULT 'PENDING',
    review_result VARCHAR(20),
    review_comment TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE passenger_declaration IS '乘客申报表';
COMMENT ON COLUMN passenger_declaration.transport_mode IS '交通方式: BUS/TRAIN/FLIGHT/SHIP';
COMMENT ON COLUMN passenger_declaration.status IS '状态: PENDING/APPROVED/REJECTED/COMPLETED/CANCELLED';

CREATE INDEX idx_pass_decl_user ON passenger_declaration(user_id);
CREATE INDEX idx_pass_decl_date ON passenger_declaration(travel_date);
CREATE INDEX idx_pass_decl_status ON passenger_declaration(status);

-- =====================================================
-- 14. 用户通知表 (user_notification)
-- =====================================================
CREATE TABLE user_notification (
    notification_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(user_id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    notification_type VARCHAR(50),
    related_entity_type VARCHAR(50),
    related_entity_id INTEGER,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_notification IS '用户通知表';
COMMENT ON COLUMN user_notification.notification_type IS '通知类型: SYSTEM/DECLARATION/ROUTE/ALERT';
COMMENT ON COLUMN user_notification.related_entity_type IS '关联实体: CARGO_DECLARATION/PASSENGER_DECLARATION/ROUTE';

CREATE INDEX idx_user_notif_user ON user_notification(user_id);
CREATE INDEX idx_user_notif_read ON user_notification(is_read);
CREATE INDEX idx_user_notif_time ON user_notification(created_at);

-- =====================================================
-- 15. 用户反馈表 (user_feedback)
-- =====================================================
CREATE TABLE user_feedback (
    feedback_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(user_id),
    feedback_type VARCHAR(50) NOT NULL,
    title VARCHAR(200),
    content TEXT NOT NULL,
    attached_images JSONB,
    contact_info VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    reply_content TEXT,
    replied_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_feedback IS '用户反馈表';
COMMENT ON COLUMN user_feedback.feedback_type IS '反馈类型: BUG/SUGGESTION/COMPLAINT/OTHER';
COMMENT ON COLUMN user_feedback.status IS '状态: PENDING/PROCESSING/RESOLVED/CLOSED';

CREATE INDEX idx_feedback_user ON user_feedback(user_id);
CREATE INDEX idx_feedback_status ON user_feedback(status);

-- =====================================================
-- 创建用户和权限
-- =====================================================

-- 创建只读用户用于报表查询
CREATE USER readonly_user WITH PASSWORD 'readonly_password';
GRANT CONNECT ON DATABASE lorries_mobile TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly_user;

-- 创建应用用户
CREATE USER app_user WITH PASSWORD 'app_password';
GRANT CONNECT ON DATABASE lorries_mobile TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO app_user;

-- 输出初始化完成信息
DO $$
BEGIN
    RAISE NOTICE '络绎(Lorries)移动端数据库初始化完成!';
    RAISE NOTICE '共创建15张表';
END $$;
