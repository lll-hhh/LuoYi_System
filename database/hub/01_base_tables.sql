-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 基础信息表
-- =====================================================

-- 启用UUID扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- 1. 部门表 (department)
-- =====================================================
CREATE TABLE department (
    department_id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id INTEGER REFERENCES department(department_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE department IS '部门表';
COMMENT ON COLUMN department.department_id IS '部门ID';
COMMENT ON COLUMN department.department_name IS '部门名称';
COMMENT ON COLUMN department.description IS '部门描述';
COMMENT ON COLUMN department.parent_id IS '上级部门ID';

-- =====================================================
-- 2. 角色表 (role)
-- =====================================================
CREATE TABLE role (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    department_id INTEGER REFERENCES department(department_id),
    description TEXT,
    permissions JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE role IS '角色表';
COMMENT ON COLUMN role.role_id IS '角色ID';
COMMENT ON COLUMN role.role_name IS '角色名称';
COMMENT ON COLUMN role.role_code IS '角色编码';
COMMENT ON COLUMN role.department_id IS '所属部门ID';
COMMENT ON COLUMN role.permissions IS '权限列表(JSON格式)';

-- =====================================================
-- 3. 员工表 (employee)
-- =====================================================
CREATE TABLE employee (
    employee_id SERIAL PRIMARY KEY,
    employee_no VARCHAR(50) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar_url VARCHAR(500),
    role_id INTEGER REFERENCES role(role_id),
    department_id INTEGER REFERENCES department(department_id),
    position VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE employee IS '员工表';
COMMENT ON COLUMN employee.employee_id IS '员工ID';
COMMENT ON COLUMN employee.employee_no IS '工号';
COMMENT ON COLUMN employee.username IS '用户名';
COMMENT ON COLUMN employee.password_hash IS '密码哈希';
COMMENT ON COLUMN employee.real_name IS '真实姓名';
COMMENT ON COLUMN employee.status IS '状态: ACTIVE/INACTIVE/LOCKED';

CREATE INDEX idx_employee_role ON employee(role_id);
CREATE INDEX idx_employee_department ON employee(department_id);
CREATE INDEX idx_employee_status ON employee(status);

-- =====================================================
-- 4. 实名认证表 (verification)
-- =====================================================
CREATE TABLE verification (
    verification_id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(employee_id),
    id_card_no VARCHAR(20),
    id_card_front_url VARCHAR(500),
    id_card_back_url VARCHAR(500),
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    verified_at TIMESTAMP,
    verifier_id INTEGER REFERENCES employee(employee_id),
    reject_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE verification IS '实名认证表';
COMMENT ON COLUMN verification.verification_status IS '认证状态: PENDING/APPROVED/REJECTED';

-- =====================================================
-- 5. IP地址表 (ip_address)
-- =====================================================
CREATE TABLE ip_address (
    ip_id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(employee_id),
    ip_address VARCHAR(50) NOT NULL,
    device_type VARCHAR(50),
    device_name VARCHAR(100),
    is_allowed BOOLEAN DEFAULT TRUE,
    last_access_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ip_address IS 'IP地址绑定表';

-- =====================================================
-- 6. 设备类型表 (device_type)
-- =====================================================
CREATE TABLE device_type (
    device_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE device_type IS '设备类型表';
COMMENT ON COLUMN device_type.category IS '设备分类: CAMERA/SENSOR/OTHER';

-- 插入默认设备类型
INSERT INTO device_type (type_name, type_code, category, description) VALUES
('高清摄像头', 'HD_CAMERA', 'CAMERA', '1080P高清监控摄像头'),
('4K摄像头', '4K_CAMERA', 'CAMERA', '4K超高清摄像头'),
('红外摄像头', 'IR_CAMERA', 'CAMERA', '红外夜视摄像头'),
('车流传感器', 'TRAFFIC_SENSOR', 'SENSOR', '车流量检测传感器'),
('地磁传感器', 'MAGNETIC_SENSOR', 'SENSOR', '车位检测地磁传感器');

-- =====================================================
-- 7. 摄像头信息表 (camera)
-- =====================================================
CREATE TABLE camera (
    camera_id SERIAL PRIMARY KEY,
    camera_no VARCHAR(50) NOT NULL UNIQUE,
    camera_name VARCHAR(100) NOT NULL,
    device_type_id INTEGER REFERENCES device_type(device_type_id),
    location VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    rtmp_url VARCHAR(500),
    hls_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ONLINE',
    resolution VARCHAR(20),
    fps INTEGER DEFAULT 25,
    road_id INTEGER,
    junction_id INTEGER,
    installed_at TIMESTAMP,
    last_maintenance_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE camera IS '摄像头信息表';
COMMENT ON COLUMN camera.status IS '状态: ONLINE/OFFLINE/MAINTENANCE';
COMMENT ON COLUMN camera.rtmp_url IS 'RTMP推流地址';
COMMENT ON COLUMN camera.hls_url IS 'HLS播放地址';

CREATE INDEX idx_camera_status ON camera(status);
CREATE INDEX idx_camera_road ON camera(road_id);

-- =====================================================
-- 8. 其他设备表 (other_device)
-- =====================================================
CREATE TABLE other_device (
    device_id SERIAL PRIMARY KEY,
    device_no VARCHAR(50) NOT NULL UNIQUE,
    device_name VARCHAR(100) NOT NULL,
    device_type_id INTEGER REFERENCES device_type(device_type_id),
    location VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    status VARCHAR(20) DEFAULT 'ONLINE',
    config JSONB,
    installed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE other_device IS '其他设备表';
