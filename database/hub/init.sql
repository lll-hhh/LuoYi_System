-- =====================================================
-- 络绎(Lorries)智慧交通管理系统 - 枢纽端数据库
-- 初始化脚本
-- =====================================================

-- 按顺序执行所有建表脚本
\i /docker-entrypoint-initdb.d/01_base_tables.sql
\i /docker-entrypoint-initdb.d/02_traffic_tables.sql
\i /docker-entrypoint-initdb.d/03_warehouse_tables.sql
\i /docker-entrypoint-initdb.d/04_monitoring_tables.sql
\i /docker-entrypoint-initdb.d/05_task_tables.sql
\i /docker-entrypoint-initdb.d/06_statistics_tables.sql
\i /docker-entrypoint-initdb.d/07_prediction_tables.sql

-- 创建只读用户用于报表查询
CREATE USER readonly_user WITH PASSWORD 'readonly_password';
GRANT CONNECT ON DATABASE lorries_hub TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly_user;

-- 创建应用用户
CREATE USER app_user WITH PASSWORD 'app_password';
GRANT CONNECT ON DATABASE lorries_hub TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO app_user;

-- 输出初始化完成信息
DO $$
BEGIN
    RAISE NOTICE '络绎(Lorries)枢纽端数据库初始化完成!';
    RAISE NOTICE '共创建56张表';
END $$;
