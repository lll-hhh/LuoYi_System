"""时序异常检测模块"""
from __future__ import annotations

from dataclasses import dataclass
from typing import List, Dict, Optional
from datetime import datetime
import statistics


@dataclass
class SeriesPoint:
    """单个传感器数据点"""
    timestamp: datetime
    value: float


@dataclass
class AnomalyEvent:
    index: int
    timestamp: datetime
    value: float
    score: float
    category: str
    message: str


class TemporalAnomalyDetector:
    """结合Z分数与IQR的时序异常检测器"""

    def __init__(self, min_window: int = 5):
        self.min_window = min_window

    def _z_score_detection(self, values: List[float], threshold: float) -> List[int]:
        if len(values) < 2:
            return []
        mean = statistics.mean(values)
        stdev = statistics.pstdev(values)
        if stdev == 0:
            return []
        return [idx for idx, value in enumerate(values) if abs((value - mean) / stdev) >= threshold]

    def _iqr_detection(self, values: List[float], multiplier: float) -> List[int]:
        if len(values) < 4:
            return []
        quartiles = statistics.quantiles(values, n=4)
        q1, q3 = quartiles[0], quartiles[2]
        iqr = q3 - q1
        lower = q1 - multiplier * iqr
        upper = q3 + multiplier * iqr
        return [idx for idx, value in enumerate(values) if value < lower or value > upper]

    def analyze(self, series: List[SeriesPoint], window: int = 12, sensitivity: float = 3.0,
                trend_threshold: float = 0.15) -> Dict[str, List[AnomalyEvent]]:
        if len(series) < max(self.min_window, window):
            return {"spikes": [], "trend": []}

        values = [pt.value for pt in series]
        timestamps = [pt.timestamp for pt in series]

        spike_indexes = set(self._z_score_detection(values, sensitivity))
        spike_indexes.update(self._iqr_detection(values, sensitivity / 2))

        spikes = [
            AnomalyEvent(
                index=idx,
                timestamp=timestamps[idx],
                value=values[idx],
                score=round(abs(values[idx] - statistics.mean(values)) / (statistics.pstdev(values) or 1), 3),
                category="spike",
                message="值偏离均值超过阈值"
            )
            for idx in sorted(spike_indexes)
        ]

        trend = []
        window = min(window, len(series))
        for start in range(len(series) - window + 1):
            segment = values[start:start + window]
            slope = self._slope(segment)
            if abs(slope) >= trend_threshold:
                trend.append(AnomalyEvent(
                    index=start + window - 1,
                    timestamp=timestamps[start + window - 1],
                    value=segment[-1],
                    score=round(abs(slope), 4),
                    category="trend_increase" if slope > 0 else "trend_decrease",
                    message="窗口内存在显著趋势变化"
                ))

        return {"spikes": spikes, "trend": trend}

    @staticmethod
    def _slope(segment: List[float]) -> float:
        n = len(segment)
        if n < 2:
            return 0.0
        x_mean = (n - 1) / 2
        y_mean = statistics.mean(segment)
        numerator = sum((i - x_mean) * (value - y_mean) for i, value in enumerate(segment))
        denominator = sum((i - x_mean) ** 2 for i in range(n)) or 1
        return numerator / denominator
