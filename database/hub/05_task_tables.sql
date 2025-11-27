-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 任务与调度表
-- =====================================================

-- =====================================================
-- 36. 任务类型表 (task_type)
-- =====================================================
CREATE TABLE task_type (
    task_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50),
    priority INTEGER DEFAULT 0,
    estimated_duration INTEGER,
    required_skills JSONB,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE task_type IS '任务类型表';
COMMENT ON COLUMN task_type.category IS '分类: PATROL/MAINTENANCE/EMERGENCY/INSPECTION';
COMMENT ON COLUMN task_type.estimated_duration IS '预计时长(分钟)';

INSERT INTO task_type (type_name, type_code, category, priority, estimated_duration) VALUES
('日常巡检', 'DAILY_PATROL', 'PATROL', 1, 60),
('设备维护', 'EQUIPMENT_MAINTENANCE', 'MAINTENANCE', 2, 120),
('紧急响应', 'EMERGENCY_RESPONSE', 'EMERGENCY', 5, 30),
('事故处理', 'ACCIDENT_HANDLING', 'EMERGENCY', 5, 60),
('交通疏导', 'TRAFFIC_CONTROL', 'PATROL', 3, 45),
('设备检修', 'EQUIPMENT_INSPECTION', 'INSPECTION', 2, 90);

-- =====================================================
-- 37. 班次安排表 (shift_schedule)
-- =====================================================
CREATE TABLE shift_schedule (
    shift_schedule_id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(employee_id),
    shift_date DATE NOT NULL,
    shift_type VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    location VARCHAR(200),
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    notes TEXT,
    created_by INTEGER REFERENCES employee(employee_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, shift_date, shift_type)
);

COMMENT ON TABLE shift_schedule IS '班次安排表';
COMMENT ON COLUMN shift_schedule.shift_type IS '班次类型: MORNING/AFTERNOON/NIGHT/FULL_DAY';
COMMENT ON COLUMN shift_schedule.status IS '状态: SCHEDULED/ON_DUTY/COMPLETED/ABSENT';

CREATE INDEX idx_shift_employee ON shift_schedule(employee_id);
CREATE INDEX idx_shift_date ON shift_schedule(shift_date);
CREATE INDEX idx_shift_status ON shift_schedule(status);

-- =====================================================
-- 38. 任务表 (task)
-- =====================================================
CREATE TABLE task (
    task_id SERIAL PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE,
    task_type_id INTEGER NOT NULL REFERENCES task_type(task_type_id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority INTEGER DEFAULT 0,
    employee_id INTEGER REFERENCES employee(employee_id),
    shift_schedule_id INTEGER REFERENCES shift_schedule(shift_schedule_id),
    related_anomaly_id INTEGER,
    related_anomaly_type VARCHAR(50),
    location VARCHAR(200),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    scheduled_start TIMESTAMP,
    scheduled_end TIMESTAMP,
    actual_start TIMESTAMP,
    actual_end TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    result TEXT,
    attachments JSONB,
    created_by INTEGER REFERENCES employee(employee_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE task IS '任务表';
COMMENT ON COLUMN task.status IS '状态: PENDING/ASSIGNED/IN_PROGRESS/COMPLETED/CANCELLED';
COMMENT ON COLUMN task.related_anomaly_type IS '关联异常类型: REPORTED/UNREPORTED/OTHER';

CREATE INDEX idx_task_type ON task(task_type_id);
CREATE INDEX idx_task_employee ON task(employee_id);
CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_time ON task(scheduled_start);

-- =====================================================
-- 39. 通知表 (notification)
-- =====================================================
CREATE TABLE notification (
    notification_id SERIAL PRIMARY KEY,
    task_id INTEGER REFERENCES task(task_id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) DEFAULT 'INFO',
    sender_id INTEGER REFERENCES employee(employee_id),
    receiver_ids JSONB,
    receiver_roles JSONB,
    is_broadcast BOOLEAN DEFAULT FALSE,
    read_by JSONB DEFAULT '[]',
    status VARCHAR(20) DEFAULT 'SENT',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE notification IS '通知表';
COMMENT ON COLUMN notification.notification_type IS '类型: TASK/ALERT/ANNOUNCEMENT/SYSTEM';
COMMENT ON COLUMN notification.severity IS '级别: INFO/WARNING/ERROR/CRITICAL';

CREATE INDEX idx_notification_type ON notification(notification_type);
CREATE INDEX idx_notification_time ON notification(sent_at);
CREATE INDEX idx_notification_status ON notification(status);
