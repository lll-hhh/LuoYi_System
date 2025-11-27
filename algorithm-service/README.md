# 络绎算法服务（algorithm-service）

算法服务为整个平台提供路线规划、调度优化、异常检测与需求预测等智能算法接口，基于 FastAPI 封装统一的 HTTP API，并配套 Prometheus 指标、pytest 用例。

## 快速开始（仅限 Docker）

> ⚠️ **说明**：算法服务不再支持在宿主机直接运行 Python 进程，所有调试与测试必须通过 Docker 容器完成，以保持与生产环境一致。

```bash
# 构建镜像（位于仓库根目录）
cd algorithm-service
docker build -t luoyi-algorithm:dev .

# 启动单服务容器
docker run --rm -it \
	-p 8090:8090 \
	--env-file ../.env \
	--name luoyi-algorithm-dev \
	luoyi-algorithm:dev

# 或使用 docker-compose（在仓库根目录）
docker-compose up -d algorithm-service --build

# 运行测试
docker-compose exec algorithm-service pytest -q
```

## 主要功能

| 功能 | 说明 | 相关接口 |
| --- | --- | --- |
| 车辆识别 / 异常检测 | 模拟识别车辆、分析违规行为 | `/api/recognize/vehicle`, `/api/detect/anomaly` |
| AHP 分析 | 客流、拥堵、人员调度、基建建议 | `/api/ahp/*` |
| 路线规划 | 传统路线、A*、多站点优化 | `/api/route/optimize`, `/api/route/multistop`, `/api/route/astar` |
| 调度优化 | 基于技能/距离/优先级的司机-任务匹配 | `/api/dispatch/optimize` |
| 时序异常检测 | Z-Score + IQR 组合检测 | `/api/anomaly/timeseries` |
| 需求预测 | 双指数平滑 + 季节性分解 | `/api/demand/forecast` |
| LightGBM 拥堵预测 | 训练与批量预测 | `/api/lightgbm/*` |

## 新增算法模块

- `algorithms/route_planning.py`：多站点路线规划，结合最近邻 + 2-opt 局部搜索，支持时间窗与拥堵权重。
- `algorithms/dispatch_optimizer.py`：司机-任务匹配评分器，考虑技能、距离、工作量与任务优先级。
- `algorithms/anomaly_stream.py`：基于 Z 分数与 IQR 的时序异常检测，输出尖峰与趋势事件。
- `algorithms/demand_forecast.py`：双指数平滑 + 简易季节分解的需求预测器，返回未来周期预测值及置信度。

## 健康检查与监控

- 健康检查：`GET /health`
- Prometheus 指标：`GET /metrics`

## 提交前检查

1. 运行 `docker-compose exec algorithm-service pytest -q` 确保接口基础用例通过。
2. 如修改依赖，请同步更新 `requirements.txt`。
3. 推荐通过 `uvicorn main:app --reload` 本地联调 API。
