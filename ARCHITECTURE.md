# 络绎智慧交通管理系统 - 架构文档

## 系统概述

络绎(Lorries)是一个综合性的智慧交通管理系统，包含以下核心模块：

- **道路监控管理**: 实时路况监控、异常事件检测
- **仓储管理**: 货物进出库、库存盘点
- **停车场管理**: 车位管理、计费结算
- **移动端应用**: 货运申报、路线规划

## 技术栈

### 后端技术
| 组件 | 技术选型 | 版本 |
|------|---------|------|
| 运行时 | Java | 17 |
| 框架 | Spring Boot | 3.2.x |
| ORM | MyBatis-Plus | 3.5.x |
| 安全 | Spring Security + JWT | - |
| 文档 | SpringDoc OpenAPI | 2.x |

### 前端技术
| 组件 | 技术选型 | 版本 |
|------|---------|------|
| Web框架 | Vue.js | 3.x |
| UI组件 | Element Plus | 2.x |
| 构建工具 | Vite | 5.x |
| 状态管理 | Pinia | 2.x |
| 图表 | ECharts | 5.x |

### 移动端技术
| 组件 | 技术选型 | 版本 |
|------|---------|------|
| 框架 | Flutter | 3.x |
| 语言 | Dart | 3.x |
| 状态管理 | Provider | 6.x |
| 路由 | GoRouter | 14.x |
| HTTP | Dio | 5.x |

### 算法服务
| 组件 | 技术选型 | 版本 |
|------|---------|------|
| 框架 | FastAPI | 0.110.x |
| 语言 | Python | 3.11 |
| 图像处理 | OpenCV | 4.x |
| 机器学习 | PyTorch | 2.x |

### 基础设施
| 组件 | 技术选型 | 端口 |
|------|---------|------|
| 数据库(枢纽) | PostgreSQL 16 | 5432 |
| 数据库(移动) | PostgreSQL 16 | 5433 |
| 缓存 | Redis 7 | 6379 |
| 消息队列 | RabbitMQ 3 | 5672 |
| 对象存储 | MinIO | 9000 |
| 反向代理 | Nginx | 80/443 |

## 系统架构

```
                                    ┌─────────────┐
                                    │   Nginx     │
                                    │  (80/443)   │
                                    └──────┬──────┘
                                           │
              ┌────────────────────────────┼────────────────────────────┐
              │                            │                            │
              ▼                            ▼                            ▼
    ┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
    │  Web Frontend   │         │  Hub Backend    │         │ Mobile Backend  │
    │  (Vue3:3000)    │         │ (Spring:8081)   │         │ (Spring:8082)   │
    └─────────────────┘         └────────┬────────┘         └────────┬────────┘
                                         │                           │
                        ┌────────────────┴────────────────┐          │
                        │                                 │          │
                        ▼                                 ▼          ▼
              ┌─────────────────┐               ┌─────────────────────────────┐
              │ Algorithm Svc   │               │      PostgreSQL Cluster     │
              │ (Python:8090)   │               │  Hub(5432) + Mobile(5433)   │
              └─────────────────┘               └─────────────────────────────┘
                                                          │
                                    ┌─────────────────────┴─────────────────────┐
                                    │                                           │
                                    ▼                                           ▼
                          ┌─────────────────┐                         ┌─────────────────┐
                          │     Redis       │                         │   RabbitMQ      │
                          │    (6379)       │                         │   (5672)        │
                          └─────────────────┘                         └─────────────────┘
                                    │
                                    ▼
                          ┌─────────────────┐
                          │     MinIO       │
                          │  (9000/9001)    │
                          └─────────────────┘
```

## 数据库设计

### 枢纽端数据库 (67张表)

#### 系统管理模块 (6张表)
- `sys_user` - 系统用户
- `sys_role` - 角色
- `sys_permission` - 权限
- `sys_user_role` - 用户角色关联
- `sys_role_permission` - 角色权限关联
- `sys_operation_log` - 操作日志

#### 道路监控模块 (8张表)
- `road_info` - 道路信息
- `road_section` - 路段
- `junction` - 路口
- `camera` - 摄像头
- `camera_stream` - 视频流
- `traffic_flow` - 交通流量
- `traffic_status` - 路况状态
- `traffic_event` - 交通事件

#### 仓储管理模块 (11张表)
- `warehouse` - 仓库
- `storage_area` - 库区
- `storage_location` - 库位
- `goods_category` - 货物类别
- `goods_info` - 货物信息
- `inventory` - 库存
- `inventory_log` - 库存日志
- `inbound_order` - 入库单
- `inbound_order_item` - 入库明细
- `outbound_order` - 出库单
- `outbound_order_item` - 出库明细

#### 停车场管理模块 (9张表)
- `parking_lot` - 停车场
- `parking_area` - 停车区域
- `parking_space` - 车位
- `parking_record` - 停车记录
- `parking_fee_rule` - 收费规则
- `parking_payment` - 支付记录
- `monthly_pass` - 月卡
- `blacklist` - 黑名单
- `parking_statistics` - 统计数据

#### 员工管理模块 (6张表)
- `department` - 部门
- `employee` - 员工
- `work_schedule` - 排班
- `attendance` - 考勤
- `salary_record` - 工资记录
- `training_record` - 培训记录

#### 统计分析模块 (5张表)
- `traffic_daily_stats` - 每日交通统计
- `warehouse_daily_stats` - 每日仓储统计
- `parking_daily_stats` - 每日停车统计
- `system_alert` - 系统告警
- `report_template` - 报表模板

#### 智能算法模块 (11张表)
- `algorithm_model` - 算法模型
- `algorithm_task` - 算法任务
- `vehicle_detection` - 车辆检测
- `license_plate_recognition` - 车牌识别
- `traffic_violation` - 交通违规
- `anomaly_detection` - 异常检测
- `traffic_prediction` - 交通预测
- `congestion_analysis` - 拥堵分析
- `route_optimization` - 路线优化
- `model_training_log` - 模型训练日志
- `algorithm_config` - 算法配置

### 移动端数据库 (11张表)
- `app_user` - 移动端用户
- `user_vehicle` - 用户车辆
- `cargo_declaration` - 货运申报
- `cargo_item` - 货物明细
- `route_plan` - 路线规划
- `route_point` - 路线途经点
- `traffic_query_log` - 路况查询日志
- `user_feedback` - 用户反馈
- `app_notification` - 应用通知
- `user_settings` - 用户设置
- `operation_log` - 操作日志

## 目录结构

```
luoyi/
├── README.md                 # 项目说明
├── ARCHITECTURE.md           # 架构文档
├── .env                      # 环境变量
├── docker-compose.yml        # Docker编排
├── nginx/                    # Nginx配置
│   ├── nginx.conf
│   └── conf.d/
│       └── default.conf
│
├── database/                 # 数据库脚本
│   ├── hub/                  # 枢纽端数据库
│   │   ├── init.sql
│   │   ├── 01-system.sql
│   │   ├── 02-road.sql
│   │   ├── 03-warehouse.sql
│   │   ├── 04-parking.sql
│   │   ├── 05-employee.sql
│   │   ├── 06-statistics.sql
│   │   └── 07-algorithm.sql
│   └── mobile/               # 移动端数据库
│       └── init.sql
│
├── hub-backend/              # 枢纽端后端
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/luoyi/hub/
│       ├── HubApplication.java
│       ├── config/
│       ├── entity/
│       ├── mapper/
│       ├── service/
│       ├── controller/
│       └── algorithm/
│
├── mobile-backend/           # 移动端后端
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/luoyi/mobile/
│       ├── MobileApplication.java
│       ├── config/
│       ├── entity/
│       ├── dto/
│       ├── mapper/
│       ├── service/
│       └── controller/
│
├── algorithm-service/        # 算法服务
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── main.py
│   └── app/
│       ├── api/
│       ├── models/
│       └── services/
│
├── web-frontend/             # Web前端
│   ├── Dockerfile
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── views/
│       ├── components/
│       ├── api/
│       ├── stores/
│       └── router/
│
└── mobile-app/               # 移动端App
    ├── Dockerfile
    ├── pubspec.yaml
    └── lib/
        ├── main.dart
        ├── config/
        ├── models/
        ├── providers/
        ├── services/
        └── screens/
```

## API接口设计

### 枢纽端后端 API (端口: 8081)

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 登录、登出、刷新Token |
| 用户 | `/api/users` | 用户管理 |
| 角色 | `/api/roles` | 角色权限管理 |
| 道路 | `/api/roads` | 道路信息管理 |
| 路口 | `/api/junctions` | 路口管理 |
| 摄像头 | `/api/cameras` | 摄像头管理 |
| 交通流量 | `/api/traffic` | 交通流量数据 |
| 仓库 | `/api/warehouses` | 仓库管理 |
| 货物 | `/api/goods` | 货物管理 |
| 库存 | `/api/inventory` | 库存管理 |
| 停车场 | `/api/parking-lots` | 停车场管理 |
| 车位 | `/api/parking-spaces` | 车位管理 |
| 员工 | `/api/employees` | 员工管理 |
| 统计 | `/api/statistics` | 统计分析 |
| 算法 | `/api/algorithm` | 算法调用 |

### 移动端后端 API (端口: 8082)

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 注册、登录、验证码 |
| 用户 | `/api/user` | 用户信息、设置 |
| 车辆 | `/api/vehicles` | 车辆管理 |
| 申报 | `/api/declarations` | 货运申报 |
| 路线 | `/api/routes` | 路线规划 |
| 路况 | `/api/traffic` | 路况查询 |
| 消息 | `/api/messages` | 通知消息 |

### 算法服务 API (端口: 8090)

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/detect/vehicle` | POST | 车辆检测 |
| `/api/detect/plate` | POST | 车牌识别 |
| `/api/detect/anomaly` | POST | 异常检测 |
| `/api/traffic/analyze` | POST | 交通分析 |
| `/api/traffic/predict` | POST | 流量预测 |
| `/api/route/optimize` | POST | 路线优化 |

## 启动说明

### 前置要求
- Docker 24.0+
- Docker Compose 2.0+
- 至少 8GB 内存

### 启动步骤

```bash
# 1. 克隆项目
cd /home/lh/my/project/luoyi

# 2. 配置环境变量 (可选修改 .env 文件)
cp .env.example .env

# 3. 启动所有服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| Web管理后台 | http://localhost | 管理员登录 |
| Hub API文档 | http://localhost:8081/swagger-ui.html | 枢纽端API |
| Mobile API文档 | http://localhost:8082/swagger-ui.html | 移动端API |
| Algorithm API文档 | http://localhost:8090/docs | 算法服务API |
| RabbitMQ管理 | http://localhost:15672 | guest/guest |
| MinIO控制台 | http://localhost:9001 | admin/luoyi123456 |

### 默认账号

| 系统 | 用户名 | 密码 |
|------|--------|------|
| Web管理后台 | admin | admin123 |
| RabbitMQ | guest | guest |
| MinIO | admin | luoyi123456 |
| PostgreSQL Hub | luoyi | luoyi123456 |
| PostgreSQL Mobile | luoyi | luoyi123456 |

## 开发指南

### 后端开发
```bash
# 进入项目目录
cd hub-backend  # 或 mobile-backend

# 本地开发 (需要Java 17)
./mvnw spring-boot:run
```

### 前端开发
```bash
# 进入项目目录
cd web-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 移动端开发
```bash
# 进入项目目录
cd mobile-app

# 获取依赖
flutter pub get

# 生成代码 (JSON序列化)
flutter pub run build_runner build

# 运行应用
flutter run
```

### 算法服务开发
```bash
# 进入项目目录
cd algorithm-service

# 创建虚拟环境
python -m venv venv
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt

# 启动服务
uvicorn main:app --reload --port 8090
```

## 部署清单

- [ ] 修改 `.env` 中的数据库密码
- [ ] 配置 SSL 证书 (生产环境)
- [ ] 设置防火墙规则
- [ ] 配置日志收集
- [ ] 设置监控告警
- [ ] 配置数据备份策略

## 维护联系

- 技术支持: support@luoyi.com
- 文档地址: https://docs.luoyi.com
