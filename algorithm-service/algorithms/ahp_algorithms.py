"""
络绎(Lorries)智慧交通管理系统 - AHP层次分析法算法模块
提供客流分析、拥堵预测、人员调度、基建建议等AHP算法实现
"""

import numpy as np
from typing import List, Dict, Tuple, Optional
from datetime import datetime, timedelta
import math


class AHPAnalyzer:
    """AHP层次分析法分析器"""
    
    # 随机一致性指标RI表
    RI_TABLE = {
        1: 0, 2: 0, 3: 0.52, 4: 0.89, 5: 1.12, 6: 1.26, 7: 1.36, 
        8: 1.41, 9: 1.46, 10: 1.49, 11: 1.52, 12: 1.54, 13: 1.56, 
        14: 1.58, 15: 1.59
    }
    
    def __init__(self):
        pass
    
    def calculate_weights(self, matrix: np.ndarray) -> Tuple[np.ndarray, float]:
        """
        计算权重向量和一致性比率
        
        Args:
            matrix: 成对比较矩阵
            
        Returns:
            (权重向量, 一致性比率CR)
        """
        n = matrix.shape[0]
        
        # 算术平均法计算权重
        # 1. 归一化每列
        col_sums = matrix.sum(axis=0)
        normalized = matrix / col_sums
        
        # 2. 计算每行平均值作为权重
        weights = normalized.mean(axis=1)
        
        # 一致性检验
        # 计算最大特征值
        weighted_sum = matrix @ weights
        lambda_max = np.mean(weighted_sum / weights)
        
        # 计算一致性指标CI
        ci = (lambda_max - n) / (n - 1) if n > 1 else 0
        
        # 计算一致性比率CR
        ri = self.RI_TABLE.get(n, 1.59)
        cr = ci / ri if ri > 0 else 0
        
        return weights, cr
    
    def check_consistency(self, cr: float, threshold: float = 0.1) -> bool:
        """检查矩阵一致性"""
        return cr < threshold


class TrafficFlowAnalyzer(AHPAnalyzer):
    """客流分析算法 - 基于AHP的动态权重调整"""
    
    def __init__(self):
        super().__init__()
        # 准则层判断矩阵: 历史趋势 vs 外部影响
        self.criteria_matrix = np.array([
            [1, 2],      # 历史趋势
            [0.5, 1]     # 外部影响
        ])
        
        # 历史趋势指标层判断矩阵
        self.history_matrix = np.array([
            [1, 2, 3],       # 上周同日
            [0.5, 1, 2],     # 上月同日
            [1/3, 0.5, 1]    # 上季度同日
        ])
        
        # 外部影响指标层判断矩阵
        self.external_matrix = np.array([
            [1, 0.5, 1/3],   # 天气
            [2, 1, 2],       # 节假日
            [3, 0.5, 1]      # 特殊事件
        ])
        
    def predict_traffic_flow(
        self, 
        last_week_flow: float,
        last_month_flow: float,
        last_quarter_flow: float,
        weather_factor: float = 1.0,  # 天气影响系数 0.7-1.3
        is_holiday: bool = False,
        special_event_factor: float = 1.0  # 特殊事件影响系数
    ) -> Dict:
        """
        预测客流量
        
        Args:
            last_week_flow: 上周同日客流量
            last_month_flow: 上月同日客流量
            last_quarter_flow: 上季度同日客流量
            weather_factor: 天气影响系数
            is_holiday: 是否节假日
            special_event_factor: 特殊事件影响系数
            
        Returns:
            预测结果字典
        """
        # 计算准则层权重
        criteria_weights, _ = self.calculate_weights(self.criteria_matrix)
        history_weight = criteria_weights[0]  # ~0.667
        external_weight = criteria_weights[1]  # ~0.333
        
        # 计算历史趋势指标权重
        history_indicator_weights, _ = self.calculate_weights(self.history_matrix)
        # [0.55, 0.30, 0.15] 近似
        
        # 计算外部影响指标权重
        external_indicator_weights, _ = self.calculate_weights(self.external_matrix)
        # [0.15, 0.35, 0.50] 近似
        
        # 计算历史趋势预测值
        history_prediction = (
            history_indicator_weights[0] * last_week_flow +
            history_indicator_weights[1] * last_month_flow +
            history_indicator_weights[2] * last_quarter_flow
        )
        
        # 计算外部影响系数
        holiday_factor = 1.5 if is_holiday else 1.0
        external_coefficient = (
            external_indicator_weights[0] * weather_factor +
            external_indicator_weights[1] * holiday_factor +
            external_indicator_weights[2] * special_event_factor
        )
        
        # 综合预测
        base_prediction = history_weight * history_prediction
        adjusted_prediction = base_prediction * external_coefficient
        
        # 计算置信度 (基于数据稳定性)
        data_variance = np.std([last_week_flow, last_month_flow, last_quarter_flow])
        confidence = max(0.5, 1.0 - (data_variance / max(last_week_flow, 1)) * 0.5)
        
        return {
            "predicted_flow": round(adjusted_prediction),
            "base_prediction": round(base_prediction),
            "history_weight": round(history_weight, 4),
            "external_weight": round(external_weight, 4),
            "external_coefficient": round(external_coefficient, 4),
            "confidence": round(confidence, 4),
            "factors": {
                "weather": weather_factor,
                "holiday": is_holiday,
                "special_event": special_event_factor
            }
        }


class CongestionPredictor(AHPAnalyzer):
    """拥堵预测算法 - 基于时间序列和AHP多因素分析"""
    
    def __init__(self):
        super().__init__()
        # 拥堵等级定义
        self.congestion_levels = {
            (0, 2): "畅通",
            (2, 4): "缓行",
            (4, 6): "拥堵",
            (6, 10): "严重拥堵"
        }
        
    def predict_congestion(
        self,
        current_flow: float,
        road_capacity: float,
        time_of_day: int,  # 0-23
        day_of_week: int,  # 0-6
        weather_condition: str = "sunny",
        historical_data: Optional[List[float]] = None
    ) -> Dict:
        """
        预测拥堵指数
        
        Args:
            current_flow: 当前车流量
            road_capacity: 道路容量
            time_of_day: 当前小时
            day_of_week: 星期几
            weather_condition: 天气条件
            historical_data: 历史同时段拥堵指数
            
        Returns:
            预测结果
        """
        # 基础拥堵指数计算
        saturation = current_flow / max(road_capacity, 1)
        base_index = saturation * 5
        
        # 时间权重 (早晚高峰)
        peak_hours = {7: 1.4, 8: 1.6, 9: 1.3, 17: 1.5, 18: 1.6, 19: 1.3}
        time_weight = peak_hours.get(time_of_day, 1.0)
        
        # 星期权重
        weekday_weight = 1.2 if day_of_week < 5 else 0.8
        
        # 天气权重
        weather_weights = {
            "sunny": 1.0, "cloudy": 1.0, "rain": 1.3, 
            "heavy_rain": 1.6, "snow": 1.8, "fog": 1.4
        }
        weather_weight = weather_weights.get(weather_condition, 1.0)
        
        # 历史数据修正
        if historical_data and len(historical_data) > 0:
            historical_avg = np.mean(historical_data)
            historical_weight = 0.3
        else:
            historical_avg = base_index
            historical_weight = 0
            
        # 综合计算
        predicted_index = (
            base_index * time_weight * weekday_weight * weather_weight * (1 - historical_weight) +
            historical_avg * historical_weight
        )
        
        # 限制范围
        predicted_index = max(0, min(10, predicted_index))
        
        # 确定拥堵等级
        level = "未知"
        for (low, high), name in self.congestion_levels.items():
            if low <= predicted_index < high:
                level = name
                break
        
        # 生成建议
        suggestions = self._generate_suggestions(predicted_index, level)
        
        return {
            "predicted_index": round(predicted_index, 2),
            "level": level,
            "base_index": round(base_index, 2),
            "factors": {
                "time_weight": time_weight,
                "weekday_weight": weekday_weight,
                "weather_weight": weather_weight
            },
            "suggestions": suggestions,
            "confidence": 0.85 if historical_data else 0.7
        }
    
    def _generate_suggestions(self, index: float, level: str) -> List[str]:
        """生成交通建议"""
        suggestions = []
        if level == "畅通":
            suggestions.append("道路通行良好，可正常出行")
        elif level == "缓行":
            suggestions.append("建议错峰出行")
            suggestions.append("可选择备用路线")
        elif level == "拥堵":
            suggestions.append("建议推迟出行或选择公共交通")
            suggestions.append("请使用导航避开拥堵路段")
        else:
            suggestions.append("强烈建议改变出行时间")
            suggestions.append("请密切关注路况信息")
            suggestions.append("考虑使用地铁等替代方式")
        return suggestions


class StaffScheduler(AHPAnalyzer):
    """人员调度算法 - 基于AHP的多因素优化调度"""
    
    def __init__(self):
        super().__init__()
        # 准则层判断矩阵
        # 任务紧急度、人员距离、技能匹配度、工作状态、调度成本、交通状况
        self.criteria_matrix = np.array([
            [1, 2, 3, 4, 5, 5],      # 任务紧急度
            [0.5, 1, 2, 3, 4, 4],    # 人员距离
            [1/3, 0.5, 1, 2, 3, 3],  # 技能匹配度
            [0.25, 1/3, 0.5, 1, 2, 2], # 工作状态
            [0.2, 0.25, 1/3, 0.5, 1, 1], # 调度成本
            [0.2, 0.25, 1/3, 0.5, 1, 1]  # 交通状况
        ])
    
    def schedule(
        self,
        staff_list: List[Dict],
        tasks: List[Dict]
    ) -> Dict:
        """
        批量任务人员调度
        
        Args:
            staff_list: 人员列表 [{id, name, skill_level, current_location}]
            tasks: 任务列表 [{id, location, urgency, required_skill}]
            
        Returns:
            调度结果
        """
        if not staff_list or not tasks:
            return {"assignments": [], "message": "无可用人员或任务"}
        
        assignments = []
        available_staff = list(staff_list)
        
        # 按任务紧急度排序
        sorted_tasks = sorted(tasks, key=lambda t: t.get("urgency", 1), reverse=True)
        
        for task in sorted_tasks:
            if not available_staff:
                break
                
            # 为每个任务找最佳人员
            best_match = None
            best_score = -1
            
            for staff in available_staff:
                score = self._calculate_match_score(staff, task)
                if score > best_score:
                    best_score = score
                    best_match = staff
            
            if best_match:
                assignments.append({
                    "task_id": task.get("id"),
                    "task_location": task.get("location"),
                    "staff_id": best_match.get("id"),
                    "staff_name": best_match.get("name"),
                    "match_score": round(best_score, 4),
                    "urgency": task.get("urgency", 1)
                })
                available_staff.remove(best_match)
        
        return {
            "assignments": assignments,
            "total_assigned": len(assignments),
            "unassigned_tasks": len(sorted_tasks) - len(assignments),
            "available_staff_remaining": len(available_staff),
            "generated_at": datetime.now().isoformat()
        }
    
    def _calculate_match_score(self, staff: Dict, task: Dict) -> float:
        """计算人员与任务的匹配得分"""
        score = 0.0
        
        # 技能匹配
        required_skill = task.get("required_skill", 1)
        staff_skill = staff.get("skill_level", 1)
        if staff_skill >= required_skill:
            score += 0.4  # 技能满足
        else:
            score += 0.2 * (staff_skill / required_skill)
        
        # 距离因素（假设用ID差距模拟距离）
        staff_loc = staff.get("current_location", 0)
        task_loc = task.get("location", 0)
        distance = abs(staff_loc - task_loc)
        score += 0.3 * max(0, 1 - distance / 10)
        
        # 紧急度因素
        urgency = task.get("urgency", 1)
        if urgency >= 4 and distance < 3:
            score += 0.3  # 紧急任务优先近距离人员
        else:
            score += 0.1
        
        return score
        
    def schedule_staff(
        self,
        task: Dict,
        candidates: List[Dict]
    ) -> List[Dict]:
        """
        为任务分配最优人员
        
        Args:
            task: 任务信息 {urgency, location, required_skills, ...}
            candidates: 候选人员列表 [{id, name, skills, location, status, ...}]
            
        Returns:
            排序后的候选人员列表，含得分
        """
        if not candidates:
            return []
            
        # 计算准则层权重
        criteria_weights, cr = self.calculate_weights(self.criteria_matrix)
        
        # 权重分配
        weights = {
            "urgency": criteria_weights[0],
            "distance": criteria_weights[1],
            "skill_match": criteria_weights[2],
            "work_status": criteria_weights[3],
            "cost": criteria_weights[4],
            "traffic": criteria_weights[5]
        }
        
        results = []
        for candidate in candidates:
            scores = {}
            
            # 1. 任务紧急度得分 (紧急任务倾向选择近距离人员)
            urgency = task.get("urgency", 3)
            if urgency >= 4:
                scores["urgency"] = 1.0 if candidate.get("distance", 10) < 5 else 0.5
            else:
                scores["urgency"] = 0.8
                
            # 2. 人员距离得分 (距离越近越好)
            distance = candidate.get("distance", 10)
            scores["distance"] = max(0, 1 - distance / 20)
            
            # 3. 技能匹配度得分
            required_skills = set(task.get("required_skills", []))
            candidate_skills = set(candidate.get("skills", []))
            if required_skills:
                match_ratio = len(required_skills & candidate_skills) / len(required_skills)
            else:
                match_ratio = 1.0
            scores["skill_match"] = match_ratio
            
            # 4. 工作状态得分 (空闲最优)
            status_scores = {"idle": 1.0, "available": 0.8, "busy": 0.3, "offline": 0}
            scores["work_status"] = status_scores.get(candidate.get("status", "available"), 0.5)
            
            # 5. 调度成本得分 (成本越低越好)
            cost = candidate.get("hourly_cost", 50)
            scores["cost"] = max(0, 1 - cost / 200)
            
            # 6. 交通状况得分
            traffic_index = candidate.get("traffic_index", 2)
            scores["traffic"] = max(0, 1 - traffic_index / 10)
            
            # 计算综合得分
            total_score = sum(
                weights[key] * scores[key] 
                for key in weights.keys()
            )
            
            results.append({
                "candidate": candidate,
                "total_score": round(total_score, 4),
                "detail_scores": {k: round(v, 4) for k, v in scores.items()},
                "weights": {k: round(v, 4) for k, v in weights.items()}
            })
        
        # 按得分排序
        results.sort(key=lambda x: x["total_score"], reverse=True)
        
        return results


class InfrastructureAdvisor(AHPAnalyzer):
    """基建建议算法 - 基于AHP的基础设施改进建议"""
    
    def __init__(self):
        super().__init__()
        # 准则层判断矩阵
        # 交通流量、拥堵指数、事故率、改造成本、环境影响
        self.criteria_matrix = np.array([
            [1, 3, 5, 7, 9],      # 交通流量
            [1/3, 1, 3, 5, 7],    # 拥堵指数
            [0.2, 1/3, 1, 3, 5],  # 事故率
            [1/7, 0.2, 1/3, 1, 3], # 改造成本
            [1/9, 1/7, 0.2, 1/3, 1] # 环境影响
        ])
        
        # 方案选项
        self.options = [
            {"id": "road_widen", "name": "道路拓宽", "cost_factor": 1.5},
            {"id": "add_overpass", "name": "增加立交桥", "cost_factor": 2.0},
            {"id": "optimize_signal", "name": "优化信号灯", "cost_factor": 0.3},
            {"id": "add_footbridge", "name": "增设人行天桥", "cost_factor": 0.8}
        ]
    
    def analyze(
        self,
        roads: List[Dict],
        junctions: List[Dict],
        budget: Optional[float] = None
    ) -> Dict:
        """
        分析基础设施改建需求
        
        Args:
            roads: 道路列表 [{id, name, current_capacity, usage_rate, age_years}]
            junctions: 路口列表 [{id, name, throughput, delay_time}]
            budget: 预算（万元）
            
        Returns:
            改建建议
        """
        recommendations = []
        
        # 分析道路
        for road in roads:
            usage_rate = road.get("usage_rate", 0.5)
            age_years = road.get("age_years", 5)
            
            priority_score = 0.0
            issues = []
            
            if usage_rate > 0.85:
                priority_score += 0.4
                issues.append("容量接近饱和")
            if age_years > 15:
                priority_score += 0.3
                issues.append("设施老化")
            if road.get("accident_rate", 0) > 0.1:
                priority_score += 0.2
                issues.append("事故率较高")
            
            if priority_score > 0.3:
                recommendations.append({
                    "type": "road",
                    "id": road.get("id"),
                    "name": road.get("name"),
                    "priority_score": round(priority_score, 4),
                    "issues": issues,
                    "suggested_action": "道路拓宽" if usage_rate > 0.85 else "道路翻新",
                    "estimated_cost": 100 * (1 + priority_score)
                })
        
        # 分析路口
        for junction in junctions:
            delay_time = junction.get("delay_time", 30)
            throughput = junction.get("throughput", 1000)
            
            priority_score = 0.0
            issues = []
            
            if delay_time > 60:
                priority_score += 0.4
                issues.append("等待时间过长")
            if throughput < 500:
                priority_score += 0.3
                issues.append("通行能力不足")
            
            if priority_score > 0.3:
                recommendations.append({
                    "type": "junction",
                    "id": junction.get("id"),
                    "name": junction.get("name"),
                    "priority_score": round(priority_score, 4),
                    "issues": issues,
                    "suggested_action": "增设立交桥" if delay_time > 90 else "优化信号灯",
                    "estimated_cost": 150 * (1 + priority_score)
                })
        
        # 按优先级排序
        recommendations.sort(key=lambda x: x["priority_score"], reverse=True)
        
        # 预算过滤
        if budget:
            total_cost = 0
            filtered = []
            for rec in recommendations:
                if total_cost + rec["estimated_cost"] <= budget:
                    filtered.append(rec)
                    total_cost += rec["estimated_cost"]
            recommendations = filtered
        
        return {
            "recommendations": recommendations,
            "total_count": len(recommendations),
            "total_estimated_cost": sum(r["estimated_cost"] for r in recommendations),
            "budget": budget,
            "generated_at": datetime.now().isoformat()
        }
        
    def generate_recommendations(
        self,
        road_data: Dict
    ) -> Dict:
        """
        生成基建改进建议
        
        Args:
            road_data: 道路数据 {flow, capacity, congestion_avg, accident_count, ...}
            
        Returns:
            建议结果
        """
        # 计算准则层权重
        criteria_weights, _ = self.calculate_weights(self.criteria_matrix)
        
        # 分析当前问题
        issues = []
        saturation = road_data.get("flow", 0) / max(road_data.get("capacity", 1), 1)
        congestion_avg = road_data.get("congestion_avg", 3)
        accident_rate = road_data.get("accident_count", 0) / 30  # 月均
        
        if saturation > 0.8:
            issues.append({"type": "high_flow", "severity": "high", "description": "交通流量接近饱和"})
        if congestion_avg > 4:
            issues.append({"type": "congestion", "severity": "high", "description": "平均拥堵指数较高"})
        if accident_rate > 0.5:
            issues.append({"type": "accident", "severity": "medium", "description": "事故发生率偏高"})
            
        # 评估各方案
        option_scores = []
        for option in self.options:
            score = self._evaluate_option(option, road_data, criteria_weights)
            option_scores.append({
                "option": option,
                "score": round(score, 4),
                "priority": "高" if score > 0.6 else "中" if score > 0.4 else "低"
            })
        
        # 排序
        option_scores.sort(key=lambda x: x["score"], reverse=True)
        
        # 生成建议
        recommendations = []
        for i, opt in enumerate(option_scores[:3]):  # 取前3个
            recommendations.append({
                "rank": i + 1,
                "option_id": opt["option"]["id"],
                "option_name": opt["option"]["name"],
                "score": opt["score"],
                "priority": opt["priority"],
                "estimated_cost": f"约{int(opt['option']['cost_factor'] * 100)}万元",
                "expected_benefit": self._get_benefit_description(opt["option"]["id"])
            })
            
        return {
            "road_id": road_data.get("road_id"),
            "current_issues": issues,
            "recommendations": recommendations,
            "criteria_weights": {
                "traffic_flow": round(criteria_weights[0], 4),
                "congestion": round(criteria_weights[1], 4),
                "accident_rate": round(criteria_weights[2], 4),
                "cost": round(criteria_weights[3], 4),
                "environment": round(criteria_weights[4], 4)
            },
            "generated_at": datetime.now().isoformat()
        }
    
    def _evaluate_option(
        self, 
        option: Dict, 
        road_data: Dict, 
        weights: np.ndarray
    ) -> float:
        """评估单个方案"""
        option_id = option["id"]
        
        # 各方案对各准则的适配度
        adaptability = {
            "road_widen": [0.9, 0.7, 0.4, 0.3, 0.4],
            "add_overpass": [0.7, 0.9, 0.6, 0.2, 0.3],
            "optimize_signal": [0.5, 0.8, 0.5, 0.9, 0.8],
            "add_footbridge": [0.3, 0.4, 0.8, 0.7, 0.7]
        }
        
        scores = adaptability.get(option_id, [0.5, 0.5, 0.5, 0.5, 0.5])
        return float(np.dot(weights, scores))
    
    def _get_benefit_description(self, option_id: str) -> str:
        """获取预期效益描述"""
        benefits = {
            "road_widen": "预计提升通行能力30-50%",
            "add_overpass": "消除交叉口瓶颈，减少等待时间60%",
            "optimize_signal": "提升路口通行效率20-30%，低成本高效益",
            "add_footbridge": "分离人车流，减少事故率40%"
        }
        return benefits.get(option_id, "待评估")


class AnomalyDetector:
    """异常识别算法 - 基于规则和统计的异常检测"""
    
    def __init__(self):
        # 停留时间阈值(分钟)
        self.stay_threshold_base = 10
        
        # 车辆密度阈值(辆/平方公里)
        self.density_threshold = 30
        
        # 场景系数
        self.scene_coefficients = {
            "industrial": 2.0,  # 工业区
            "commercial": 1.5,  # 商业区
            "core_road": 1.0,   # 核心道路
            "residential": 1.2  # 住宅区
        }
        
        # 天气修正系数
        self.weather_coefficients = {
            "sunny": 0,
            "rain": 0.3,
            "snow": 0.5,
            "fog": 0.4
        }
    
    def analyze(
        self,
        vehicle_id: str,
        current_location_id: int,
        stay_duration: float,  # 分钟
        location_capacity: int,
        current_density: float,  # 0-1
        historical_stay_times: Optional[List[float]] = None
    ) -> Dict:
        """
        综合异常分析
        
        Args:
            vehicle_id: 车辆ID
            current_location_id: 当前位置ID
            stay_duration: 当前滞留时间（分钟）
            location_capacity: 位置容量
            current_density: 当前密度 0-1
            historical_stay_times: 历史滞留时间列表
            
        Returns:
            异常分析结果
        """
        anomalies = []
        
        # 1. 滞留时间异常检测
        avg_stay = np.mean(historical_stay_times) if historical_stay_times else 20
        std_stay = np.std(historical_stay_times) if historical_stay_times and len(historical_stay_times) > 1 else 5
        
        stay_threshold = avg_stay + 2 * std_stay  # 2个标准差
        if stay_duration > stay_threshold:
            severity = "HIGH" if stay_duration > stay_threshold * 1.5 else "MEDIUM"
            anomalies.append({
                "type": "LONG_STAY",
                "severity": severity,
                "description": f"车辆滞留时间过长: {stay_duration:.1f}分钟 (阈值: {stay_threshold:.1f}分钟)",
                "score": min(0.99, (stay_duration - stay_threshold) / stay_threshold + 0.7)
            })
        
        # 2. 密度异常检测
        if current_density > 0.8:
            severity = "HIGH" if current_density > 0.95 else "MEDIUM"
            anomalies.append({
                "type": "HIGH_DENSITY",
                "severity": severity,
                "description": f"区域密度过高: {current_density*100:.1f}%",
                "score": current_density
            })
        
        # 3. 综合异常评分
        overall_score = 0
        if anomalies:
            overall_score = max(a["score"] for a in anomalies)
        
        return {
            "vehicle_id": vehicle_id,
            "location_id": current_location_id,
            "is_anomaly": len(anomalies) > 0,
            "anomaly_count": len(anomalies),
            "anomalies": anomalies,
            "overall_score": round(overall_score, 4),
            "statistics": {
                "stay_duration": stay_duration,
                "avg_stay": round(avg_stay, 2),
                "stay_threshold": round(stay_threshold, 2),
                "current_density": current_density
            },
            "detected_at": datetime.now().isoformat()
        }
        
    def detect_long_stay(
        self,
        vehicle_id: str,
        entry_time: datetime,
        current_time: datetime,
        scene_type: str = "core_road",
        weather: str = "sunny"
    ) -> Dict:
        """
        检测车辆停留时间过长
        
        Args:
            vehicle_id: 车辆ID
            entry_time: 进入时间
            current_time: 当前时间
            scene_type: 场景类型
            weather: 天气条件
            
        Returns:
            检测结果
        """
        # 计算停留时间(分钟)
        stay_duration = (current_time - entry_time).total_seconds() / 60
        
        # 动态阈值计算
        alpha = self.scene_coefficients.get(scene_type, 1.0)
        beta = self.weather_coefficients.get(weather, 0)
        weather_level = 1 if weather in ["rain", "snow"] else 0
        
        threshold = self.stay_threshold_base * alpha + beta * weather_level * 5
        
        # 判断异常
        is_anomaly = stay_duration > threshold
        severity = "low"
        if stay_duration > threshold * 2:
            severity = "high"
        elif stay_duration > threshold * 1.5:
            severity = "medium"
            
        return {
            "vehicle_id": vehicle_id,
            "is_anomaly": is_anomaly,
            "stay_duration_minutes": round(stay_duration, 1),
            "threshold_minutes": round(threshold, 1),
            "severity": severity if is_anomaly else None,
            "scene_type": scene_type,
            "weather": weather,
            "detected_at": current_time.isoformat()
        }
    
    def detect_high_density(
        self,
        camera_id: str,
        vehicle_count: int,
        area_sqkm: float,
        scene_type: str = "core_road",
        weather: str = "sunny",
        is_special_event: bool = False
    ) -> Dict:
        """
        检测区域车辆密度过大
        
        Args:
            camera_id: 摄像头ID
            vehicle_count: 车辆数量
            area_sqkm: 监控区域面积(平方公里)
            scene_type: 场景类型
            weather: 天气条件
            is_special_event: 是否有特殊活动
            
        Returns:
            检测结果
        """
        # 计算实时密度
        density = vehicle_count / max(area_sqkm, 0.01)
        
        # 动态阈值
        threshold = self.density_threshold
        if scene_type == "city_center":
            threshold = 50
        if weather in ["rain", "snow"]:
            threshold *= 0.7
        if is_special_event:
            threshold *= 1.3
            
        # 判断异常
        is_anomaly = density > threshold
        
        # 分级报警
        alert_level = None
        if density > threshold * 1.5:
            alert_level = "severe"
            duration_required = 2  # 分钟
        elif density > threshold:
            alert_level = "moderate"
            duration_required = 3
        else:
            duration_required = 0
            
        return {
            "camera_id": camera_id,
            "is_anomaly": is_anomaly,
            "vehicle_count": vehicle_count,
            "density": round(density, 2),
            "threshold": round(threshold, 2),
            "alert_level": alert_level,
            "duration_required_minutes": duration_required,
            "detected_at": datetime.now().isoformat()
        }
    
    def detect_speed_anomaly(
        self,
        vehicle_id: str,
        current_speed: float,
        speed_limit: float,
        road_type: str = "main"
    ) -> Dict:
        """
        检测速度异常(超速/低速)
        """
        is_speeding = current_speed > speed_limit
        is_too_slow = current_speed < speed_limit * 0.3 and current_speed > 0
        
        anomaly_type = None
        severity = None
        
        if is_speeding:
            anomaly_type = "speeding"
            over_ratio = (current_speed - speed_limit) / speed_limit
            if over_ratio > 0.5:
                severity = "high"
            elif over_ratio > 0.2:
                severity = "medium"
            else:
                severity = "low"
        elif is_too_slow:
            anomaly_type = "too_slow"
            severity = "low"
            
        return {
            "vehicle_id": vehicle_id,
            "is_anomaly": anomaly_type is not None,
            "anomaly_type": anomaly_type,
            "current_speed": current_speed,
            "speed_limit": speed_limit,
            "severity": severity,
            "detected_at": datetime.now().isoformat()
        }
    
    def detect_route_deviation(
        self,
        vehicle_id: str,
        current_location: Tuple[float, float],
        planned_route: List[Tuple[float, float]],
        max_deviation_km: float = 0.5
    ) -> Dict:
        """
        检测路径偏离
        """
        if not planned_route:
            return {
                "vehicle_id": vehicle_id,
                "is_anomaly": False,
                "reason": "无计划路线"
            }
            
        # 计算到最近路线点的距离
        min_distance = float('inf')
        for point in planned_route:
            distance = self._haversine_distance(
                current_location[0], current_location[1],
                point[0], point[1]
            )
            min_distance = min(min_distance, distance)
            
        is_deviation = min_distance > max_deviation_km
        
        return {
            "vehicle_id": vehicle_id,
            "is_anomaly": is_deviation,
            "deviation_distance_km": round(min_distance, 3),
            "threshold_km": max_deviation_km,
            "severity": "high" if min_distance > max_deviation_km * 2 else "medium" if is_deviation else None,
            "current_location": current_location,
            "detected_at": datetime.now().isoformat()
        }
    
    def _haversine_distance(
        self, 
        lat1: float, lon1: float, 
        lat2: float, lon2: float
    ) -> float:
        """计算两点间的球面距离(km)"""
        R = 6371  # 地球半径(km)
        
        lat1_rad = math.radians(lat1)
        lat2_rad = math.radians(lat2)
        delta_lat = math.radians(lat2 - lat1)
        delta_lon = math.radians(lon2 - lon1)
        
        a = math.sin(delta_lat/2)**2 + \
            math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(delta_lon/2)**2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
        
        return R * c


# 导出算法类
__all__ = [
    'AHPAnalyzer',
    'TrafficFlowAnalyzer',
    'CongestionPredictor',
    'StaffScheduler',
    'InfrastructureAdvisor',
    'AnomalyDetector'
]
