package com.lorries.hub.algorithm.client;

import com.lorries.hub.algorithm.dto.*;

/**
 * 算法服务客户端接口
 * 用于调用算法服务的各种AI功能
 */
public interface AlgorithmServiceClient {

    /**
     * 车牌识别
     * 从图片中识别车辆的车牌号、颜色、类型等信息
     * 
     * @param request 包含图片信息的请求
     * @return 识别结果
     */
    VehicleRecognitionResponse recognizeVehicle(VehicleRecognitionRequest request);

    /**
     * 拥堵预测
     * 基于历史数据和当前交通状况预测未来的拥堵情况
     * 
     * @param request 预测请求
     * @return 预测结果
     */
    CongestionPredictionResponse predictCongestion(CongestionPredictionRequest request);

    /**
     * 路线推荐
     * 根据起终点和当前路况推荐最优路线
     * 
     * @param request 路线请求
     * @return 推荐路线
     */
    RouteRecommendationResponse recommendRoute(RouteRecommendationRequest request);

    /**
     * 异常行为检测
     * 检测车辆的违规行为、事故等异常情况
     * 
     * @param request 检测请求
     * @return 检测结果
     */
    AnomalyDetectionResponse detectAnomaly(AnomalyDetectionRequest request);

    /**
     * 健康检查
     * 检查算法服务是否正常运行
     * 
     * @return 是否健康
     */
    boolean healthCheck();
}
