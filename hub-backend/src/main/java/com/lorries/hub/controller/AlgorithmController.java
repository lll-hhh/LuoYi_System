package com.lorries.hub.controller;

import com.lorries.hub.algorithm.client.AlgorithmServiceClient;
import com.lorries.hub.algorithm.dto.*;
import com.lorries.hub.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 算法服务控制器
 * 提供算法服务的HTTP接口
 */
@Tag(name = "算法服务", description = "车辆识别、拥堵预测、路线推荐、异常检测等AI功能")
@RestController
@RequestMapping("/algorithm")
@RequiredArgsConstructor
public class AlgorithmController {

    private final AlgorithmServiceClient algorithmServiceClient;

    @Operation(summary = "车辆识别", description = "从图片中识别车牌号、车辆类型、颜色等信息")
    @PostMapping("/recognize/vehicle")
    public Result<VehicleRecognitionResponse> recognizeVehicle(@RequestBody VehicleRecognitionRequest request) {
        return Result.success(algorithmServiceClient.recognizeVehicle(request));
    }

    @Operation(summary = "拥堵预测", description = "预测指定道路或路口未来的拥堵情况")
    @PostMapping("/predict/congestion")
    public Result<CongestionPredictionResponse> predictCongestion(@RequestBody CongestionPredictionRequest request) {
        return Result.success(algorithmServiceClient.predictCongestion(request));
    }

    @Operation(summary = "路线推荐", description = "根据起终点和当前路况推荐最优路线")
    @PostMapping("/recommend/route")
    public Result<RouteRecommendationResponse> recommendRoute(@RequestBody RouteRecommendationRequest request) {
        return Result.success(algorithmServiceClient.recommendRoute(request));
    }

    @Operation(summary = "异常检测", description = "检测车辆违规行为、交通事故等异常情况")
    @PostMapping("/detect/anomaly")
    public Result<AnomalyDetectionResponse> detectAnomaly(@RequestBody AnomalyDetectionRequest request) {
        return Result.success(algorithmServiceClient.detectAnomaly(request));
    }

    @Operation(summary = "健康检查", description = "检查算法服务是否正常运行")
    @GetMapping("/health")
    public Result<Boolean> healthCheck() {
        return Result.success(algorithmServiceClient.healthCheck());
    }
}
