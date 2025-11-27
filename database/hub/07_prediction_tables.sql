-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 预测与推荐表
-- =====================================================

-- =====================================================
-- 53. 拥堵预测表 (congestion_prediction)
-- =====================================================
CREATE TABLE congestion_prediction (
    prediction_id SERIAL PRIMARY KEY,
    road_id INTEGER REFERENCES road(road_id),
    junction_id INTEGER REFERENCES junction(junction_id),
    prediction_time TIMESTAMP NOT NULL,
    target_time TIMESTAMP NOT NULL,
    predicted_index DECIMAL(4, 2),
    predicted_level VARCHAR(20),
    confidence DECIMAL(5, 4),
    algorithm_version VARCHAR(50),
    actual_index DECIMAL(4, 2),
    accuracy DECIMAL(5, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE congestion_prediction IS '拥堵预测表';
COMMENT ON COLUMN congestion_prediction.prediction_time IS '预测发起时间';
COMMENT ON COLUMN congestion_prediction.target_time IS '预测目标时间';
COMMENT ON COLUMN congestion_prediction.predicted_level IS '预测拥堵等级: SMOOTH/SLOW/CONGESTED/SEVERE';
COMMENT ON COLUMN congestion_prediction.confidence IS '预测置信度';
COMMENT ON COLUMN congestion_prediction.actual_index IS '实际拥堵指数(用于评估)';
COMMENT ON COLUMN congestion_prediction.accuracy IS '预测准确率';

CREATE INDEX idx_congestion_pred_road ON congestion_prediction(road_id);
CREATE INDEX idx_congestion_pred_junction ON congestion_prediction(junction_id);
CREATE INDEX idx_congestion_pred_target ON congestion_prediction(target_time);

-- =====================================================
-- 54. 流量预测表 (traffic_flow_prediction)
-- =====================================================
CREATE TABLE traffic_flow_prediction (
    prediction_id SERIAL PRIMARY KEY,
    road_id INTEGER REFERENCES road(road_id),
    junction_id INTEGER REFERENCES junction(junction_id),
    prediction_time TIMESTAMP NOT NULL,
    target_start_time TIMESTAMP NOT NULL,
    target_end_time TIMESTAMP NOT NULL,
    predicted_flow INTEGER,
    predicted_speed DECIMAL(6, 2),
    confidence DECIMAL(5, 4),
    algorithm_version VARCHAR(50),
    actual_flow INTEGER,
    actual_speed DECIMAL(6, 2),
    flow_accuracy DECIMAL(5, 4),
    speed_accuracy DECIMAL(5, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE traffic_flow_prediction IS '流量预测表';

CREATE INDEX idx_flow_pred_road ON traffic_flow_prediction(road_id);
CREATE INDEX idx_flow_pred_target ON traffic_flow_prediction(target_start_time);

-- =====================================================
-- 55. 路线推荐表 (route_recommendation)
-- =====================================================
CREATE TABLE route_recommendation (
    recommendation_id SERIAL PRIMARY KEY,
    vehicle_plate VARCHAR(20),
    start_location VARCHAR(200),
    end_location VARCHAR(200),
    start_point_lat DECIMAL(10, 7),
    start_point_lng DECIMAL(10, 7),
    end_point_lat DECIMAL(10, 7),
    end_point_lng DECIMAL(10, 7),
    recommended_route_id INTEGER REFERENCES route(route_id),
    alternative_routes JSONB,
    estimated_time INTEGER,
    estimated_distance DECIMAL(10, 2),
    recommendation_reason TEXT,
    algorithm_version VARCHAR(50),
    is_accepted BOOLEAN,
    actual_route_id INTEGER REFERENCES route(route_id),
    actual_time INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE route_recommendation IS '路线推荐表';
COMMENT ON COLUMN route_recommendation.alternative_routes IS '备选路线(JSON数组)';
COMMENT ON COLUMN route_recommendation.estimated_time IS '预计时间(分钟)';
COMMENT ON COLUMN route_recommendation.estimated_distance IS '预计距离(公里)';

CREATE INDEX idx_route_rec_plate ON route_recommendation(vehicle_plate);
CREATE INDEX idx_route_rec_time ON route_recommendation(created_at);

-- =====================================================
-- 56. 调度建议表 (dispatch_suggestion)
-- =====================================================
CREATE TABLE dispatch_suggestion (
    suggestion_id SERIAL PRIMARY KEY,
    suggestion_type VARCHAR(50) NOT NULL,
    target_entity_type VARCHAR(50) NOT NULL,
    target_entity_id INTEGER NOT NULL,
    current_status JSONB,
    suggested_action TEXT NOT NULL,
    suggested_params JSONB,
    priority INTEGER DEFAULT 3,
    expected_improvement TEXT,
    algorithm_version VARCHAR(50),
    is_applied BOOLEAN DEFAULT FALSE,
    applied_at TIMESTAMP,
    applied_by INTEGER REFERENCES employee(employee_id),
    result_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE dispatch_suggestion IS '调度建议表';
COMMENT ON COLUMN dispatch_suggestion.suggestion_type IS '建议类型: SIGNAL_TIMING/ROUTE_DIVERSION/PARKING_GUIDE/RESOURCE_ALLOCATION';
COMMENT ON COLUMN dispatch_suggestion.target_entity_type IS '目标实体类型: JUNCTION/ROAD/PARKING/WAREHOUSE';
COMMENT ON COLUMN dispatch_suggestion.priority IS '优先级: 1-最高, 5-最低';

CREATE INDEX idx_dispatch_sug_type ON dispatch_suggestion(suggestion_type);
CREATE INDEX idx_dispatch_sug_entity ON dispatch_suggestion(target_entity_type, target_entity_id);
CREATE INDEX idx_dispatch_sug_time ON dispatch_suggestion(created_at);

-- =====================================================
-- 初始化视图
-- =====================================================

-- 实时道路状态视图
CREATE OR REPLACE VIEW v_road_realtime_status AS
SELECT 
    r.road_id,
    r.road_name,
    r.road_level,
    r.lane_count,
    h.total_vehicles AS hourly_vehicles,
    h.avg_speed,
    h.congestion_index,
    CASE 
        WHEN h.congestion_index < 2 THEN '畅通'
        WHEN h.congestion_index < 4 THEN '缓行'
        WHEN h.congestion_index < 6 THEN '拥堵'
        ELSE '严重拥堵'
    END AS traffic_status,
    h.stat_hour
FROM road r
LEFT JOIN road_hour_sumlog h ON r.road_id = h.road_id
    AND h.stat_hour = DATE_TRUNC('hour', CURRENT_TIMESTAMP);

-- 设备在线状态视图
CREATE OR REPLACE VIEW v_device_online_status AS
SELECT 
    'CAMERA' AS device_type,
    c.camera_id AS device_id,
    c.camera_name AS device_name,
    c.ip_address,
    c.online_status,
    c.last_heartbeat,
    r.road_name AS location
FROM camera c
LEFT JOIN road r ON c.road_id = r.road_id
UNION ALL
SELECT 
    dt.type_name AS device_type,
    od.other_device_id AS device_id,
    od.device_name,
    od.ip_address,
    od.online_status,
    od.last_heartbeat,
    od.location
FROM other_device od
JOIN device_type dt ON od.device_type_id = dt.device_type_id;

-- 今日异常统计视图
CREATE OR REPLACE VIEW v_today_anomaly_summary AS
SELECT 
    tat.type_name,
    COUNT(rva.anomaly_id) AS reported_count,
    (SELECT COUNT(*) FROM unreported_vehicle_anomaly uva 
     WHERE uva.anomaly_type_id = tat.anomaly_type_id 
     AND DATE(uva.occurred_at) = CURRENT_DATE) AS unreported_count,
    (SELECT COUNT(*) FROM other_anomaly oa 
     WHERE oa.anomaly_type_id = tat.anomaly_type_id 
     AND DATE(oa.occurred_at) = CURRENT_DATE) AS other_count
FROM traffic_anomaly_type tat
LEFT JOIN reported_vehicle_anomaly rva ON tat.anomaly_type_id = rva.anomaly_type_id
    AND DATE(rva.occurred_at) = CURRENT_DATE
GROUP BY tat.anomaly_type_id, tat.type_name;

-- 仓库库存概览视图
CREATE OR REPLACE VIEW v_warehouse_inventory_overview AS
SELECT 
    w.warehouse_id,
    w.warehouse_name,
    w.warehouse_type,
    COUNT(DISTINCT ct.cargo_type_id) AS cargo_type_count,
    SUM(cs.current_quantity) AS total_quantity,
    SUM(cs.current_quantity * cs.unit_weight) AS total_weight,
    w.capacity,
    ROUND(SUM(cs.current_quantity * cs.unit_weight) / NULLIF(w.capacity, 0) * 100, 2) AS usage_rate
FROM warehouse w
LEFT JOIN cargo_store cs ON w.warehouse_id = cs.warehouse_id
LEFT JOIN cargo_type ct ON cs.cargo_type_id = ct.cargo_type_id
GROUP BY w.warehouse_id, w.warehouse_name, w.warehouse_type, w.capacity;

-- 停车场实时状态视图
CREATE OR REPLACE VIEW v_parking_realtime_status AS
SELECT 
    p.parking_id,
    p.parking_name,
    p.parking_type,
    p.total_spaces,
    p.available_spaces,
    p.total_spaces - p.available_spaces AS occupied_spaces,
    ROUND((p.total_spaces - p.available_spaces)::DECIMAL / NULLIF(p.total_spaces, 0) * 100, 2) AS occupancy_rate,
    ps.hourly_rate,
    ps.daily_max_rate,
    p.status
FROM parking p
LEFT JOIN parking_set ps ON p.parking_id = ps.parking_id AND ps.is_active = TRUE;
