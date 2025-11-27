"""交通与运力需求预测模块"""
from __future__ import annotations

from dataclasses import dataclass
from typing import List, Dict
import statistics


@dataclass
class ForecastResult:
    history: List[float]
    predictions: List[float]
    trend: float
    seasonality: float
    confidence: float


class DemandForecaster:
    """采用双指数平滑+简单季节性分解的需求预测器"""

    def __init__(self, alpha: float = 0.4, beta: float = 0.3, season_length: int = 24):
        self.alpha = alpha
        self.beta = beta
        self.season_length = season_length

    def forecast(self, series: List[float], periods: int = 12) -> ForecastResult:
        if len(series) < 2:
            raise ValueError("Series length must be greater than 1")

        level = series[0]
        trend = series[1] - series[0]
        seasonals = self._initial_seasonals(series)
        predictions = []

        for idx, value in enumerate(series):
            season = seasonals[idx % self.season_length]
            prev_level = level
            level = self.alpha * (value / (season or 1)) + (1 - self.alpha) * (level + trend)
            trend = self.beta * (level - prev_level) + (1 - self.beta) * trend
            seasonals[idx % self.season_length] = 0.3 * (value / (level or 1)) + 0.7 * season

        for i in range(1, periods + 1):
            season = seasonals[(len(series) + i - 1) % self.season_length]
            predictions.append((level + i * trend) * season)

        variance = statistics.pvariance(series)
        confidence = max(0.5, 1 - variance / (sum(series) or 1))

        return ForecastResult(
            history=series,
            predictions=[round(pred, 2) for pred in predictions],
            trend=round(trend, 4),
            seasonality=round(sum(seasonals) / len(seasonals), 4),
            confidence=round(confidence, 4)
        )

    def _initial_seasonals(self, series: List[float]) -> List[float]:
        seasons = [1.0] * self.season_length
        if len(series) < self.season_length:
            return seasons
        season_count = len(series) // self.season_length
        for i in range(self.season_length):
            values = [series[j] for j in range(i, len(series), self.season_length)]
            seasons[i] = (sum(values) / len(values)) / (sum(series) / len(series)) if values else 1.0
        return seasons
