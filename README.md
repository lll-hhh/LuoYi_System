<p align="center">
  <img src="https://img.shields.io/badge/络绎-Lorries-blue?style=for-the-badge&logo=truck&logoColor=white" alt="Lorries Logo"/>
</p>

<h1 align="center">🚛 络绎智慧交通管理系统</h1>

<p align="center">
  <strong>Lorries Intelligent Transportation Management System</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Vue-3.x-42b883?style=flat-square&logo=vue.js" alt="Vue"/>
  <img src="https://img.shields.io/badge/Flutter-3.x-02569B?style=flat-square&logo=flutter" alt="Flutter"/>
  <img src="https://img.shields.io/badge/Python-3.11-3776AB?style=flat-square&logo=python" alt="Python"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker" alt="Docker"/>
</p>

<p align="center">
  <a href="#-项目简介">项目简介</a> •
  <a href="#-功能特性">功能特性</a> •
  <a href="#-系统架构">系统架构</a> •
  <a href="#-技术栈">技术栈</a> •
  <a href="#-快速开始">快速开始</a> •
  <a href="#-项目结构">项目结构</a>
</p>

---

## 📖 项目简介

**络绎**（Lorries）是一套综合性的智慧交通管理系统，集成了道路监控、仓储管理、停车场管理、移动端货运申报等多个模块。系统采用微服务架构设计，支持高并发、可扩展，适用于城市交通管理、物流园区、货运中心等场景。

### 🎯 设计目标

- **智能化**: 集成 AI 算法，实现车辆识别、异常检测、交通预测
- **一体化**: 统一管理道路、仓储、停车等多业务场景
- **移动化**: 提供移动端 App，支持司机在线申报、路况查询
- **可视化**: 实时监控大屏，数据统计分析报表

---

## ✨ 功能特性

<table>
<tr>
<td width="50%">

cd algorithm-service
# 构建镜像（可增量）
docker build -t luoyi-algorithm:dev .

# 运行容器并映射端口
docker run --rm -it \
  -p 8090:8090 \
  --name luoyi-algorithm-dev \
  luoyi-algorithm:dev

# 或使用 docker-compose 热重载开发
# docker-compose up algorithm-service --build

### 📦 仓储管理
- 多仓库、多库区管理
- 货物入库/出库流程
- 库存盘点与预警
- 货物追踪与查询

### 🅿️ 停车场管理
- 车位状态实时监控
- 自动计费与支付
- 月卡管理
- 黑名单管理

</td>
<td width="50%">

### 👥 员工管理
- 部门组织架构
- 排班与考勤
- 工资核算
- 培训记录

### 📊 统计分析
- 交通流量趋势分析
- 仓储吞吐量统计
- 停车收入报表
- 自定义报表模板

### 📱 移动端应用
- 司机注册登录
- 车辆信息管理
- 货运在线申报
- 实时路况查询
- 智能路线规划

</td>
</tr>
</table>

---

## 🏗️ 系统架构

```
                                    ┌─────────────────┐
                                    │     Nginx       │
                                    │   (80 / 443)    │
                                    └────────┬────────┘
                                             │
           ┌─────────────────────────────────┼─────────────────────────────────┐
           │                                 │                                 │
           ▼                                 ▼                                 ▼
┌─────────────────────┐         ┌─────────────────────┐         ┌─────────────────────┐
│   Web Frontend      │         │    Hub Backend      │         │   Mobile Backend    │
│   Vue 3 + Vite      │         │   Spring Boot       │         │   Spring Boot       │
│      :3000          │         │      :8081          │         │      :8082          │
└─────────────────────┘         └──────────┬──────────┘         └──────────┬──────────┘
                                           │                               │
                          ┌────────────────┼───────────────────────────────┤
                          │                │                               │
                          ▼                ▼                               ▼
              ┌───────────────────┐ ┌─────────────────────────────────────────────────┐
              │  Algorithm Svc    │ │              PostgreSQL Cluster                 │
              │  FastAPI/Python   │ │     Hub DB (:5432)    Mobile DB (:5433)         │
              │     :8090         │ └─────────────────────────────────────────────────┘
              └───────────────────┘                        │
                                          ┌────────────────┴────────────────┐
                                          │                                 │
                                          ▼                                 ▼
                                ┌─────────────────┐               ┌─────────────────┐
                                │     Redis       │               │    RabbitMQ     │
                                │     :6379       │               │     :5672       │
                                └─────────────────┘               └─────────────────┘
                                          │
                                          ▼
                                ┌─────────────────┐
                                │      MinIO      │
                                │   :9000/:9001   │
                                └─────────────────┘
```

---

## 🛠️ 技术栈

<table>
<tr>
<th>层级</th>
<th>技术选型</th>
<th>说明</th>
</tr>
<tr>
<td><b>后端框架</b></td>
<td>
<img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" alt="Java"/>
<img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot" alt="Spring Boot"/>
</td>
<td>MyBatis-Plus + Spring Security + JWT</td>
</tr>
<tr>
<td><b>Web 前端</b></td>
<td>
<img src="https://img.shields.io/badge/Vue-3.x-42b883?logo=vue.js" alt="Vue"/>
<img src="https://img.shields.io/badge/Vite-5.x-646CFF?logo=vite" alt="Vite"/>
</td>
<td>Element Plus + Pinia + ECharts</td>
</tr>
<tr>
<td><b>移动端</b></td>
<td>
<img src="https://img.shields.io/badge/Flutter-3.x-02569B?logo=flutter" alt="Flutter"/>
<img src="https://img.shields.io/badge/Dart-3.x-0175C2?logo=dart" alt="Dart"/>
</td>
<td>Provider + GoRouter + Dio</td>
</tr>
<tr>
<td><b>算法服务</b></td>
<td>
<img src="https://img.shields.io/badge/Python-3.11-3776AB?logo=python" alt="Python"/>
<img src="https://img.shields.io/badge/FastAPI-0.110-009688?logo=fastapi" alt="FastAPI"/>
</td>
<td>OpenCV + PyTorch (接口预留)</td>
</tr>
<tr>
<td><b>数据库</b></td>
<td>
<img src="https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql" alt="PostgreSQL"/>
<img src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis" alt="Redis"/>
</td>
<td>双实例 + 缓存</td>
</tr>
<tr>
<td><b>中间件</b></td>
<td>
<img src="https://img.shields.io/badge/RabbitMQ-3-FF6600?logo=rabbitmq" alt="RabbitMQ"/>
<img src="https://img.shields.io/badge/MinIO-Latest-C72E49?logo=minio" alt="MinIO"/>
</td>
<td>消息队列 + 对象存储</td>
</tr>
<tr>
<td><b>容器化</b></td>
<td>
<img src="https://img.shields.io/badge/Docker-24+-2496ED?logo=docker" alt="Docker"/>
<img src="https://img.shields.io/badge/Nginx-Alpine-009639?logo=nginx" alt="Nginx"/>
</td>
<td>10 个微服务容器</td>
</tr>
</table>

---

## 🚀 快速开始

### 环境要求

- Docker 24.0+
- Docker Compose 2.0+
- 8GB+ 内存

### 一键启动

```bash
# 克隆项目
git clone https://github.com/lll-hhh/LuoYi_System.git
cd LuoYi_System

# 启动所有服务
docker-compose up -d

# 仅启动算法服务（Python 只能在容器中运行）
docker-compose up -d algorithm-service

# 查看服务状态
docker-compose ps
```

### 访问地址

| 服务 | 地址 | 说明 |
|:---:|:---:|:---:|
| 🌐 Web 管理后台 | http://localhost | 管理员登录 |
| 📚 Hub API 文档 | http://localhost:8081/swagger-ui.html | 枢纽端 API |
| 📚 Mobile API 文档 | http://localhost:8082/swagger-ui.html | 移动端 API |
| 📚 Algorithm API 文档 | http://localhost:8090/docs | 算法服务 API |
| 🐰 RabbitMQ 管理 | http://localhost:15672 | guest / guest |
| 📦 MinIO 控制台 | http://localhost:9001 | admin / luoyi123456 |

### 默认账号

| 系统 | 用户名 | 密码 |
|:---:|:---:|:---:|
| Web 管理后台 | admin | admin123 |
| PostgreSQL | luoyi | luoyi123456 |
| MinIO | admin | luoyi123456 |

---

## 📁 项目结构

```
LuoYi_System/
│
├── 📄 docker-compose.yml      # Docker 编排文件
├── 📄 .env                    # 环境变量配置
│
├── 📂 nginx/                  # Nginx 配置
│   └── conf.d/default.conf
│
├── 📂 database/               # 数据库脚本
│   ├── hub/                   # 枢纽端 (56 张表)
│   │   ├── 01-system.sql
│   │   ├── 02-road.sql
│   │   ├── 03-warehouse.sql
│   │   ├── 04-parking.sql
│   │   ├── 05-employee.sql
│   │   ├── 06-statistics.sql
│   │   └── 07-algorithm.sql
│   └── mobile/                # 移动端 (11 张表)
│       └── init.sql
│
├── 📂 hub-backend/            # 枢纽端后端 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/lorries/hub/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── mapper/
│   │       ├── entity/
│   │       └── algorithm/
│   └── pom.xml
│
├── 📂 mobile-backend/         # 移动端后端 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/lorries/mobile/
│   └── pom.xml
│
├── 📂 algorithm-service/      # 算法服务 (FastAPI)
│   ├── main.py
│   └── requirements.txt
│
├── 📂 web-frontend/           # Web 前端 (Vue 3)
│   ├── src/
│   │   ├── views/
│   │   ├── components/
│   │   ├── api/
│   │   └── stores/
│   └── package.json
│
└── 📂 mobile-app/             # 移动端 App (Flutter)
    ├── lib/
    │   ├── screens/
    │   ├── providers/
    │   ├── services/
    │   └── models/
    └── pubspec.yaml
```

---

## 📊 项目统计

<table>
<tr>
<td align="center"><b>📄 代码文件</b></td>
<td align="center"><b>📝 代码行数</b></td>
<td align="center"><b>🗄️ 数据库表</b></td>
<td align="center"><b>🐳 Docker 服务</b></td>
</tr>
<tr>
<td align="center">150+</td>
<td align="center">15,000+</td>
<p align="center">
  <img src="https://img.shields.io/badge/%E7%BB%9C%E7%BB%8E-Lorries-blue?style=for-the-badge&logo=truck&logoColor=white" alt="Lorries Logo" />
</p>

<h1 align="center">🚛 络绎 · 智慧交通与物流中枢</h1>

<p align="center">“一套代码，串起城市道路、仓储枢纽、停车资产与移动司机。”</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.x-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/FastAPI-0.110-05998B?style=flat-square&logo=fastapi" alt="FastAPI"/>
  <img src="https://img.shields.io/badge/Vue-3.x-42b883?style=flat-square&logo=vue.js" alt="Vue"/>
  <img src="https://img.shields.io/badge/Flutter-3.x-02569B?style=flat-square&logo=flutter" alt="Flutter"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker%20Compose-Full%20Stack-2496ED?style=flat-square&logo=docker" alt="Docker"/>
</p>

<p align="center">
  <a href="#-项目综述">项目综述</a> ·
  <a href="#-亮点速览">亮点速览</a> ·
  <a href="#-功能矩阵">功能矩阵</a> ·
  <a href="#-系统架构">系统架构</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="#-开发与测试">开发与测试</a>
</p>

---

## 📖 项目综述

**络绎（Lorries）** 是一套端到端的智慧交通管理平台，面向城市道路治理、物流园区调度与多仓一体化运营。平台由 Web 管控台 + 移动端应用 + 算法服务 + IoT 接入组成，具备高并发能力与良好的横向扩展性，可覆盖道路监控、仓储履约、停车资产、司机服务等典型场景。

> 🧭 愿景：让交通参与者「可感知、可协同、可预测」。

---

## ✨ 亮点速览

<table>
<tr>
  <td><b>🛰️ 全域感知</b><br/>道路、仓库、停车、司机四大域统一建模，实时采集并入库。</td>
  <td><b>🧠 AI 算法中心</b><br/>FastAPI 算法服务封装路线规划、异常检测、调度优化、需求预测。</td>
</tr>
<tr>
  <td><b>📱 移动优先</b><br/>Flutter App 支持司机注册、车辆管理、货运申报与智能路线。</td>
  <td><b>📊 可观测性</b><br/>Prometheus 指标、WebSocket 实时推送、分布式日志确保可追溯。</td>
</tr>
</table>

---

## 🧩 功能矩阵

| 模块 | 能力 | 亮点 |
| --- | --- | --- |
| 📦 仓储管理 | 多仓多库区、入出库流转、容量预警、轨迹追踪 | 动态容量计算 + 可视化大屏 |
| 🛣️ 道路监控 | 车流监控、异常上报、IoT 摄像头接入 | WebSocket 实时态势、事件复盘 |
| 🅿️ 停车资产 | 车位监测、计费策略、黑名单管控 | 计费引擎与电子票据接口 |
| 👥 员工与司机 | 组织结构、排班考勤、司机档案、车辆绑定 | 司机端定位上报 + 任务提醒 |
| � 货运移动端 | 在线申报、路线规划、路况订阅 | Flutter + WebSocket 低延迟交互 |
| 🧮 算法服务 | 路线多目标优化、调度评分、异常检测、需求预测 | 纯 Docker 运行，配套 pytest 与指标 |

---

## 🏗️ 系统架构

```
                                    ┌─────────────────┐
                                    │     Nginx       │
                                    │   (80 / 443)    │
                                    └────────┬────────┘
                                             │
           ┌─────────────────────────────────┼─────────────────────────────────┐
           │                                 │                                 │
           ▼                                 ▼                                 ▼
┌─────────────────────┐         ┌─────────────────────┐         ┌─────────────────────┐
│   Web Frontend      │         │    Hub Backend      │         │   Mobile Backend    │
│   Vue 3 + Vite      │         │   Spring Boot       │         │   Spring Boot       │
│      :3000          │         │      :8081          │         │      :8082          │
└─────────────────────┘         └──────────┬──────────┘         └──────────┬──────────┘
                                           │                               │
                          ┌────────────────┼───────────────────────────────┤
                          │                │                               │
                          ▼                ▼                               ▼
              ┌───────────────────┐ ┌─────────────────────────────────────────────────┐
              │  Algorithm Svc    │ │              PostgreSQL Cluster                 │
              │  FastAPI/Python   │ │     Hub DB (:5432)    Mobile DB (:5433)         │
              │     :8090         │ └─────────────────────────────────────────────────┘
              └───────────────────┘                        │
                                          ┌────────────────┴────────────────┐
                                          │                                 │
                                          ▼                                 ▼
                                ┌─────────────────┐               ┌─────────────────┐
                                │     Redis       │               │    RabbitMQ     │
                                │     :6379       │               │     :5672       │
                                └─────────────────┘               └─────────────────┘
                                          │
                                          ▼
                                ┌─────────────────┐
                                │      MinIO      │
                                │   :9000/:9001   │
                                └─────────────────┘
```

---

## 🛠️ 技术栈

| 层级 | 技术选型 | 说明 |
| --- | --- | --- |
| 后端 | Java 17 · Spring Boot 2.7 · MyBatis-Plus · Spring Security · JWT | 枢纽与移动双后台，提供 REST & WebSocket |
| 算法服务 | Python 3.11 · FastAPI 0.110 · Uvicorn · Prometheus client | 纯 Docker 运行，提供路线/调度/异常/预测 API |
| Web 前端 | Vue 3 · Vite 5 · Element Plus · Pinia · ECharts | 管理驾驶舱与数据大屏 |
| 移动端 | Flutter 3 · Dart 3 · GoRouter · Dio | 司机端申报、轨迹、消息提醒 |
| 数据与中间件 | PostgreSQL 16 · Redis 7 · RabbitMQ 3 · MinIO latest | 主备数据库、缓存、消息、对象存储 |
| 基础设施 | Docker Compose · Nginx · GitHub Actions (可扩展) | 一键编排与 CI/CD 预留 |

---

## 🚀 快速开始

### 0. 环境准备

- Docker ≥ 24.0
- Docker Compose ≥ 2.0
- 8 GB 以上可用内存

### 1. 克隆仓库

```bash
git clone https://github.com/lll-hhh/LuoYi_System.git
cd LuoYi_System
```

### 2. 启动全栈（推荐）

```bash
docker-compose up -d
docker-compose ps
```

### 3. 定向运行

```bash
# 仅启动算法服务（⚠️ Python 只能在容器中运行）
docker-compose up -d algorithm-service --build

# 仅启动移动端后台
docker-compose up -d mobile-backend
```

### 4. 常用入口

| 服务 | 地址 | 说明 |
| --- | --- | --- |
| 🌐 管理后台 | http://localhost | Web 端入口 |
| 📚 Hub API 文档 | http://localhost:8081/swagger-ui.html | 枢纽 REST API |
| 📚 Mobile API 文档 | http://localhost:8082/swagger-ui.html | 司机/货运 API |
| 🤖 Algorithm Docs | http://localhost:8090/docs | FastAPI 文档 |
| 🐰 RabbitMQ Console | http://localhost:15672 | guest / guest |
| 📦 MinIO Console | http://localhost:9001 | admin / luoyi123456 |

### 5. 默认账号

| 系统 | 账号 | 密码 |
| --- | --- | --- |
| Web 管理后台 | admin | admin123 |
| PostgreSQL | luoyi | luoyi123456 |
| MinIO | admin | luoyi123456 |

---

## 📂 项目结构

```
LuoYi_System/
├── docker-compose.yml          # 一键编排入口
├── .env                        # 环境变量示例
├── database/                   # DDL & 初始化数据
│   ├── hub/                    # 枢纽端（56 张表）
│   └── mobile/                 # 移动端（11 张表）
├── hub-backend/                # 管理端 Spring Boot 服务
├── mobile-backend/             # 司机/货运 Spring Boot 服务
├── algorithm-service/          # FastAPI 算法中心（仅 Docker）
├── web-frontend/               # Vue 3 管理前端
├── mobile-app/                 # Flutter 司机 App
└── nginx/                      # 网关与静态托管
```

---

## 🧪 开发与测试

<details>
<summary><b>后端（Hub & Mobile）</b></summary>

```bash
cd hub-backend   # 或 mobile-backend
mvn -q test
./mvnw spring-boot:run
```

</details>

<details>
<summary><b>Web 前端</b></summary>

```bash
cd web-frontend
npm install
npm run dev
```

</details>

<details>
<summary><b>移动 App</b></summary>

```bash
cd mobile-app
flutter pub get
flutter pub run build_runner build
flutter run
```

</details>

<details>
<summary><b>算法服务（Docker Only）</b></summary>

```bash
cd algorithm-service
docker build -t luoyi-algorithm:dev .
docker run --rm -it \
  -p 8090:8090 \
  --env-file ../.env \
  --name luoyi-algorithm-dev \
  luoyi-algorithm:dev

# 集成测试
docker-compose up -d algorithm-service --build
docker-compose exec algorithm-service pytest -q
```

</details>

---

## 🛡️ 可观测 & 保障

- **实时推送**：WebSocket 通知与司机定位流。
- **指标采集**：FastAPI + Spring Boot 暴露 `/metrics`，可直接接入 Prometheus + Grafana。
- **日志治理**：分层日志分类输出，生产可接驳 ELK/EFK。
- **安全控制**：JWT + 细粒度角色、MinIO 临时凭证、Redis OTP 双重校验。

---

## 🤝 贡献指南 & 许可

1. Fork 仓库并创建特性分支。
2. 确保通过 `mvn -q test` / `docker-compose exec algorithm-service pytest -q`。
3. 提交 PR 并附带模块说明或截图。

项目采用 [MIT License](LICENSE)，欢迎商用、二次开发与共建。

---
</details>

<details>
<summary><b>移动端开发</b></summary>

```bash
cd mobile-app
flutter pub get
flutter pub run build_runner build
flutter run
```

</details>

<details>
<summary><b>算法服务开发</b></summary>

```bash
cd algorithm-service
docker build -t luoyi-algorithm:dev .
docker run --rm -it \
  -p 8090:8090 \
  --name luoyi-algorithm-dev \
  luoyi-algorithm:dev

# 或在项目根目录使用 docker-compose（推荐带监控的本地联调）
docker-compose up -d algorithm-service --build

# 执行单元测试
docker-compose exec algorithm-service pytest -q
```

</details>

---

## 📜 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

<p align="center">
  <sub>Made with ❤️ by LuoYi Team</sub>
</p>
