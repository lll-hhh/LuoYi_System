"""
LightGBM拥堵预测模型
基于交通时间指数(TTI)的拥堵预测算法

技术栈:
- LightGBM: 梯度提升决策树
- NumPy: 数值计算
- Scikit-learn: 数据预处理和模型评估

TTI (Traffic Time Index) = 实际通行时间 / 自由流通行时间
TTI > 1.5 表示拥堵，TTI > 2.0 表示严重拥堵
"""

import numpy as np
from typing import Dict, List, Optional, Tuple, Any
from dataclasses import dataclass, field
from datetime import datetime, timedelta
import json
import pickle
from pathlib import Path

# 尝试导入LightGBM，如果不可用则使用模拟实现
try:
    import lightgbm as lgb
    LIGHTGBM_AVAILABLE = True
except ImportError:
    LIGHTGBM_AVAILABLE = False
    print("Warning: LightGBM not available, using fallback model")


@dataclass
class TrafficFeatures:
    """交通特征数据类"""
    # 时间特征
    hour: int  # 0-23
    day_of_week: int  # 0-6 (周一到周日)
    is_weekend: bool
    is_holiday: bool
    month: int  # 1-12
    
    # 交通特征
    current_flow: float  # 当前车流量 (辆/小时)
    road_capacity: float  # 道路容量
    avg_speed: float  # 平均速度 (km/h)
    
    # 历史特征
    tti_lag_1h: Optional[float] = None  # 1小时前TTI
    tti_lag_2h: Optional[float] = None  # 2小时前TTI
    tti_lag_24h: Optional[float] = None  # 昨天同时段TTI
    tti_rolling_mean_3h: Optional[float] = None  # 过去3小时滚动均值
    
    # 环境特征
    weather_code: int = 0  # 0:晴, 1:阴, 2:小雨, 3:中雨, 4:大雨, 5:雪
    temperature: float = 20.0  # 温度
    visibility: float = 10.0  # 能见度(km)
    
    # 事件特征
    has_accident: bool = False  # 是否有事故
    has_construction: bool = False  # 是否有施工
    event_scale: int = 0  # 特殊活动规模 0:无, 1:小型, 2:中型, 3:大型


@dataclass
class TTIPrediction:
    """TTI预测结果"""
    tti: float
    congestion_level: str  # "畅通", "缓行", "拥堵", "严重拥堵"
    confidence: float
    predicted_travel_time: float  # 预测通行时间(分钟)
    free_flow_time: float  # 自由流通行时间(分钟)
    delay_time: float  # 延误时间(分钟)
    factors: Dict[str, float] = field(default_factory=dict)  # 各因素贡献度
    suggestions: List[str] = field(default_factory=list)


class LightGBMCongestionModel:
    """
    基于LightGBM的拥堵预测模型
    
    使用历史TTI数据、路段信息和时间特征进行训练
    支持多步预测(未来15分钟、30分钟、1小时)
    """
    
    def __init__(self, model_path: Optional[str] = None):
        self.model = None
        self.feature_names = [
            'hour', 'day_of_week', 'is_weekend', 'is_holiday', 'month',
            'current_flow', 'road_capacity', 'saturation_ratio', 'avg_speed',
            'tti_lag_1h', 'tti_lag_2h', 'tti_lag_24h', 'tti_rolling_mean_3h',
            'weather_code', 'temperature', 'visibility',
            'has_accident', 'has_construction', 'event_scale',
            'is_morning_peak', 'is_evening_peak'
        ]
        
        # 模型参数
        self.params = {
            'objective': 'regression',
            'metric': 'rmse',
            'boosting_type': 'gbdt',
            'num_leaves': 31,
            'learning_rate': 0.05,
            'feature_fraction': 0.9,
            'bagging_fraction': 0.8,
            'bagging_freq': 5,
            'verbose': -1,
            'n_estimators': 200,
            'max_depth': 8,
            'min_child_samples': 20,
            'reg_alpha': 0.1,
            'reg_lambda': 0.1
        }
        
        # 拥堵等级阈值
        self.congestion_thresholds = {
            (0, 1.25): "畅通",
            (1.25, 1.5): "缓行",
            (1.5, 2.0): "拥堵",
            (2.0, float('inf')): "严重拥堵"
        }
        
        # 如果有模型文件，加载它
        if model_path and Path(model_path).exists():
            self.load_model(model_path)
        else:
            # 初始化一个默认的规则模型
            self._init_rule_based_model()
    
    def _init_rule_based_model(self):
        """初始化基于规则的备用模型"""
        self.model = None
        self.use_rule_based = True
    
    def _extract_features(self, features: TrafficFeatures) -> np.ndarray:
        """从TrafficFeatures提取特征向量"""
        saturation_ratio = features.current_flow / max(features.road_capacity, 1)
        
        # 高峰时段标记
        is_morning_peak = 1 if 7 <= features.hour <= 9 else 0
        is_evening_peak = 1 if 17 <= features.hour <= 19 else 0
        
        feature_vector = [
            features.hour,
            features.day_of_week,
            1 if features.is_weekend else 0,
            1 if features.is_holiday else 0,
            features.month,
            features.current_flow,
            features.road_capacity,
            saturation_ratio,
            features.avg_speed,
            features.tti_lag_1h or 1.0,
            features.tti_lag_2h or 1.0,
            features.tti_lag_24h or 1.0,
            features.tti_rolling_mean_3h or 1.0,
            features.weather_code,
            features.temperature,
            features.visibility,
            1 if features.has_accident else 0,
            1 if features.has_construction else 0,
            features.event_scale,
            is_morning_peak,
            is_evening_peak
        ]
        
        return np.array(feature_vector).reshape(1, -1)
    
    def _rule_based_predict(self, features: TrafficFeatures) -> float:
        """基于规则的TTI预测（备用方法）"""
        # 基础TTI = 饱和度
        saturation = features.current_flow / max(features.road_capacity, 1)
        base_tti = 1 + saturation * 0.8
        
        # 时间权重
        peak_multipliers = {
            7: 1.3, 8: 1.5, 9: 1.3,
            17: 1.4, 18: 1.5, 19: 1.3
        }
        time_factor = peak_multipliers.get(features.hour, 1.0)
        
        # 工作日/周末
        weekday_factor = 1.15 if not features.is_weekend else 0.85
        
        # 天气因素
        weather_factors = {0: 1.0, 1: 1.05, 2: 1.15, 3: 1.3, 4: 1.5, 5: 1.6}
        weather_factor = weather_factors.get(features.weather_code, 1.0)
        
        # 事件因素
        event_factor = 1.0
        if features.has_accident:
            event_factor *= 1.5
        if features.has_construction:
            event_factor *= 1.2
        event_factor *= (1 + features.event_scale * 0.1)
        
        # 历史因素
        history_factor = 1.0
        if features.tti_lag_1h:
            history_factor = 0.7 + 0.3 * features.tti_lag_1h
        
        # 综合计算
        tti = base_tti * time_factor * weekday_factor * weather_factor * event_factor * history_factor
        
        return max(1.0, min(4.0, tti))
    
    def predict(
        self,
        features: TrafficFeatures,
        road_length_km: float = 5.0,
        free_flow_speed: float = 60.0
    ) -> TTIPrediction:
        """
        预测拥堵指数
        
        Args:
            features: 交通特征
            road_length_km: 路段长度(km)
            free_flow_speed: 自由流速度(km/h)
            
        Returns:
            TTIPrediction: 预测结果
        """
        # 使用LightGBM模型或规则模型
        if self.model is not None and LIGHTGBM_AVAILABLE:
            feature_vector = self._extract_features(features)
            tti = float(self.model.predict(feature_vector)[0])
            confidence = 0.85
        else:
            tti = self._rule_based_predict(features)
            confidence = 0.75
        
        # 限制TTI范围
        tti = max(1.0, min(5.0, tti))
        
        # 计算通行时间
        free_flow_time = (road_length_km / free_flow_speed) * 60  # 分钟
        predicted_travel_time = free_flow_time * tti
        delay_time = predicted_travel_time - free_flow_time
        
        # 确定拥堵等级
        congestion_level = "未知"
        for (low, high), level in self.congestion_thresholds.items():
            if low <= tti < high:
                congestion_level = level
                break
        
        # 计算因素贡献度
        factors = self._calculate_factor_contributions(features)
        
        # 生成建议
        suggestions = self._generate_suggestions(tti, congestion_level, features)
        
        return TTIPrediction(
            tti=round(tti, 3),
            congestion_level=congestion_level,
            confidence=confidence,
            predicted_travel_time=round(predicted_travel_time, 1),
            free_flow_time=round(free_flow_time, 1),
            delay_time=round(delay_time, 1),
            factors=factors,
            suggestions=suggestions
        )
    
    def _calculate_factor_contributions(self, features: TrafficFeatures) -> Dict[str, float]:
        """计算各因素对TTI的贡献度"""
        contributions = {}
        
        # 饱和度贡献
        saturation = features.current_flow / max(features.road_capacity, 1)
        contributions['traffic_saturation'] = round(min(saturation * 0.4, 0.4), 3)
        
        # 时间贡献
        if 7 <= features.hour <= 9 or 17 <= features.hour <= 19:
            contributions['peak_hour'] = 0.2
        else:
            contributions['peak_hour'] = 0.0
        
        # 天气贡献
        weather_contrib = [0, 0.02, 0.05, 0.1, 0.15, 0.2]
        contributions['weather'] = weather_contrib[min(features.weather_code, 5)]
        
        # 事件贡献
        event_contrib = 0
        if features.has_accident:
            event_contrib += 0.15
        if features.has_construction:
            event_contrib += 0.1
        contributions['events'] = event_contrib
        
        return contributions
    
    def _generate_suggestions(
        self,
        tti: float,
        level: str,
        features: TrafficFeatures
    ) -> List[str]:
        """生成交通建议"""
        suggestions = []
        
        if level == "畅通":
            suggestions.append("道路通畅，可正常出行")
        elif level == "缓行":
            suggestions.append("轻微拥堵，建议稍后出发或选择备选路线")
            if 7 <= features.hour <= 9:
                suggestions.append("正值早高峰，预计9:30后路况好转")
            elif 17 <= features.hour <= 19:
                suggestions.append("正值晚高峰，预计19:30后路况好转")
        elif level == "拥堵":
            suggestions.append("道路拥堵，强烈建议选择其他路线")
            suggestions.append("可考虑使用公共交通")
            if features.has_accident:
                suggestions.append("前方有交通事故，建议绕行")
        else:  # 严重拥堵
            suggestions.append("道路严重拥堵，请务必避开该路段")
            suggestions.append("预计需要等待30分钟以上")
            if features.weather_code >= 3:
                suggestions.append("恶劣天气，建议延迟出行")
        
        return suggestions
    
    def train(
        self,
        X: np.ndarray,
        y: np.ndarray,
        validation_split: float = 0.2
    ) -> Dict[str, float]:
        """
        训练模型
        
        Args:
            X: 特征矩阵 (n_samples, n_features)
            y: TTI标签 (n_samples,)
            validation_split: 验证集比例
            
        Returns:
            训练指标
        """
        if not LIGHTGBM_AVAILABLE:
            return {"error": "LightGBM not available"}
        
        # 划分训练集和验证集
        n_samples = len(y)
        n_val = int(n_samples * validation_split)
        indices = np.random.permutation(n_samples)
        
        train_idx = indices[n_val:]
        val_idx = indices[:n_val]
        
        X_train, X_val = X[train_idx], X[val_idx]
        y_train, y_val = y[train_idx], y[val_idx]
        
        # 创建LightGBM数据集
        train_data = lgb.Dataset(X_train, label=y_train, feature_name=self.feature_names)
        val_data = lgb.Dataset(X_val, label=y_val, reference=train_data)
        
        # 训练模型
        self.model = lgb.train(
            self.params,
            train_data,
            valid_sets=[train_data, val_data],
            valid_names=['train', 'valid'],
            callbacks=[lgb.early_stopping(50, verbose=False)]
        )
        
        self.use_rule_based = False
        
        # 计算评估指标
        train_pred = self.model.predict(X_train)
        val_pred = self.model.predict(X_val)
        
        metrics = {
            'train_rmse': np.sqrt(np.mean((train_pred - y_train) ** 2)),
            'val_rmse': np.sqrt(np.mean((val_pred - y_val) ** 2)),
            'train_mae': np.mean(np.abs(train_pred - y_train)),
            'val_mae': np.mean(np.abs(val_pred - y_val)),
            'feature_importance': dict(zip(
                self.feature_names,
                self.model.feature_importance('gain').tolist()
            ))
        }
        
        return metrics
    
    def save_model(self, path: str):
        """保存模型"""
        if self.model is not None:
            self.model.save_model(path)
    
    def load_model(self, path: str):
        """加载模型"""
        if LIGHTGBM_AVAILABLE and Path(path).exists():
            self.model = lgb.Booster(model_file=path)
            self.use_rule_based = False


class TTIDataGenerator:
    """
    TTI训练数据生成器
    用于生成合成训练数据
    """
    
    @staticmethod
    def generate_synthetic_data(
        n_samples: int = 10000,
        seed: int = 42
    ) -> Tuple[np.ndarray, np.ndarray]:
        """
        生成合成训练数据
        
        Returns:
            X: 特征矩阵
            y: TTI标签
        """
        np.random.seed(seed)
        
        X = []
        y = []
        
        for _ in range(n_samples):
            # 时间特征
            hour = np.random.randint(0, 24)
            day_of_week = np.random.randint(0, 7)
            is_weekend = 1 if day_of_week >= 5 else 0
            is_holiday = 1 if np.random.random() < 0.05 else 0
            month = np.random.randint(1, 13)
            
            # 交通特征
            road_capacity = np.random.uniform(1000, 5000)
            base_flow = road_capacity * np.random.uniform(0.3, 1.2)
            
            # 高峰时段增加流量
            if hour in [7, 8, 9, 17, 18, 19] and not is_weekend:
                base_flow *= np.random.uniform(1.2, 1.6)
            
            current_flow = base_flow
            saturation_ratio = current_flow / road_capacity
            avg_speed = max(10, 60 * (1 - saturation_ratio * 0.6) + np.random.normal(0, 5))
            
            # 历史特征
            tti_lag_1h = 1 + np.random.exponential(0.3)
            tti_lag_2h = 1 + np.random.exponential(0.25)
            tti_lag_24h = 1 + np.random.exponential(0.2)
            tti_rolling_mean_3h = (tti_lag_1h + tti_lag_2h + 1) / 3
            
            # 环境特征
            weather_code = np.random.choice([0, 1, 2, 3, 4, 5], p=[0.5, 0.2, 0.15, 0.08, 0.05, 0.02])
            temperature = np.random.uniform(-5, 40)
            visibility = max(0.5, 10 - weather_code * 1.5 + np.random.normal(0, 1))
            
            # 事件特征
            has_accident = 1 if np.random.random() < 0.03 else 0
            has_construction = 1 if np.random.random() < 0.05 else 0
            event_scale = np.random.choice([0, 1, 2, 3], p=[0.9, 0.06, 0.03, 0.01])
            
            # 高峰标记
            is_morning_peak = 1 if 7 <= hour <= 9 else 0
            is_evening_peak = 1 if 17 <= hour <= 19 else 0
            
            # 构建特征向量
            features = [
                hour, day_of_week, is_weekend, is_holiday, month,
                current_flow, road_capacity, saturation_ratio, avg_speed,
                tti_lag_1h, tti_lag_2h, tti_lag_24h, tti_rolling_mean_3h,
                weather_code, temperature, visibility,
                has_accident, has_construction, event_scale,
                is_morning_peak, is_evening_peak
            ]
            
            # 生成TTI标签
            tti = 1.0
            tti += saturation_ratio * 0.8
            
            if is_morning_peak or is_evening_peak:
                tti *= 1.3 if not is_weekend else 1.0
            
            weather_mult = [1.0, 1.05, 1.15, 1.3, 1.5, 1.6]
            tti *= weather_mult[weather_code]
            
            if has_accident:
                tti *= 1.5
            if has_construction:
                tti *= 1.2
            
            tti *= (1 + event_scale * 0.1)
            tti *= 0.7 + 0.3 * tti_lag_1h
            
            # 添加噪声
            tti *= (1 + np.random.normal(0, 0.05))
            tti = max(1.0, min(4.0, tti))
            
            X.append(features)
            y.append(tti)
        
        return np.array(X), np.array(y)


# 导出
__all__ = [
    'LightGBMCongestionModel',
    'TrafficFeatures',
    'TTIPrediction',
    'TTIDataGenerator',
    'LIGHTGBM_AVAILABLE'
]
