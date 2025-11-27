package com.lorries.hub.algorithm.client;

import com.lorries.hub.algorithm.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 算法服务客户端实现
 * 通过HTTP调用Python算法服务
 */
@Slf4j
@Component
public class AlgorithmServiceClientImpl implements AlgorithmServiceClient {

    @Value("${algorithm.service.url}")
    private String algorithmServiceUrl;

    private final RestTemplate restTemplate;

    public AlgorithmServiceClientImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public VehicleRecognitionResponse recognizeVehicle(VehicleRecognitionRequest request) {
        String url = algorithmServiceUrl + "/api/recognize/vehicle";
        try {
            HttpEntity<VehicleRecognitionRequest> entity = createEntity(request);
            ResponseEntity<VehicleRecognitionResponse> response = restTemplate.postForEntity(
                    url, entity, VehicleRecognitionResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("调用车辆识别服务失败: {}", e.getMessage());
            VehicleRecognitionResponse errorResponse = new VehicleRecognitionResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("算法服务调用失败: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public CongestionPredictionResponse predictCongestion(CongestionPredictionRequest request) {
        String url = algorithmServiceUrl + "/api/predict/congestion";
        try {
            HttpEntity<CongestionPredictionRequest> entity = createEntity(request);
            ResponseEntity<CongestionPredictionResponse> response = restTemplate.postForEntity(
                    url, entity, CongestionPredictionResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("调用拥堵预测服务失败: {}", e.getMessage());
            CongestionPredictionResponse errorResponse = new CongestionPredictionResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("算法服务调用失败: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public RouteRecommendationResponse recommendRoute(RouteRecommendationRequest request) {
        String url = algorithmServiceUrl + "/api/recommend/route";
        try {
            HttpEntity<RouteRecommendationRequest> entity = createEntity(request);
            ResponseEntity<RouteRecommendationResponse> response = restTemplate.postForEntity(
                    url, entity, RouteRecommendationResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("调用路线推荐服务失败: {}", e.getMessage());
            RouteRecommendationResponse errorResponse = new RouteRecommendationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("算法服务调用失败: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public AnomalyDetectionResponse detectAnomaly(AnomalyDetectionRequest request) {
        String url = algorithmServiceUrl + "/api/detect/anomaly";
        try {
            HttpEntity<AnomalyDetectionRequest> entity = createEntity(request);
            ResponseEntity<AnomalyDetectionResponse> response = restTemplate.postForEntity(
                    url, entity, AnomalyDetectionResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("调用异常检测服务失败: {}", e.getMessage());
            AnomalyDetectionResponse errorResponse = new AnomalyDetectionResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("算法服务调用失败: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public boolean healthCheck() {
        String url = algorithmServiceUrl + "/health";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("算法服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    private <T> HttpEntity<T> createEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
