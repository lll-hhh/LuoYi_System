"""高级路线规划与多站点优化模块"""
from __future__ import annotations

from dataclasses import dataclass
from typing import List, Optional, Dict, Any
from datetime import datetime, timedelta
import math


@dataclass
class Stop:
    """配送或巡检任务的站点信息"""
    id: str
    lat: float
    lng: float
    name: Optional[str] = None
    window_start: Optional[datetime] = None
    window_end: Optional[datetime] = None
    service_minutes: int = 5
    priority: int = 1


@dataclass
class RoutePlanResult:
    """路线规划结果"""
    sequence: List[Stop]
    total_distance_km: float
    travel_minutes: int
    window_violations: int
    congestion_penalty: float
    score: float


def haversine(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
    """计算两点间球面距离（公里）"""
    radius = 6371
    lat1, lng1, lat2, lng2 = map(math.radians, [lat1, lng1, lat2, lng2])
    dlat = lat2 - lat1
    dlng = lng2 - lng1
    a = math.sin(dlat / 2) ** 2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlng / 2) ** 2
    c = 2 * math.asin(math.sqrt(a))
    return radius * c


class DynamicRouteOptimizer:
    """多站点路线优化器，结合启发式与局部搜索"""

    def __init__(self):
        self.default_speed = 35  # km/h

    def _build_distance_matrix(self, stops: List[Stop]) -> List[List[float]]:
        matrix = []
        for stop_a in stops:
            row = []
            for stop_b in stops:
                if stop_a.id == stop_b.id:
                    row.append(0)
                else:
                    row.append(haversine(stop_a.lat, stop_a.lng, stop_b.lat, stop_b.lng))
            matrix.append(row)
        return matrix

    def _nearest_neighbor(self, depot: Stop, stops: List[Stop], congestion_map: Optional[Dict[str, float]] = None) -> List[Stop]:
        remaining = stops.copy()
        route = [depot]
        current = depot
        while remaining:
            best_stop = None
            best_cost = float("inf")
            for stop in remaining:
                distance = haversine(current.lat, current.lng, stop.lat, stop.lng)
                congestion = congestion_map.get(stop.id, 1.0) if congestion_map else 1.0
                priority_factor = 1 / max(stop.priority, 1)
                cost = distance * congestion * priority_factor
                if cost < best_cost:
                    best_cost = cost
                    best_stop = stop
            route.append(best_stop)
            remaining.remove(best_stop)
            current = best_stop
        return route

    def _two_opt(self, route: List[Stop]) -> List[Stop]:
        improved = True
        best_route = route
        best_distance = self._route_distance(route)
        while improved:
            improved = False
            for i in range(1, len(best_route) - 2):
                for j in range(i + 1, len(best_route) - 1):
                    if j - i == 1:
                        continue
                    new_route = best_route[:]
                    new_route[i:j] = reversed(new_route[i:j])
                    new_distance = self._route_distance(new_route)
                    if new_distance < best_distance:
                        best_distance = new_distance
                        best_route = new_route
                        improved = True
        return best_route

    def _route_distance(self, route: List[Stop]) -> float:
        distance = 0.0
        for current, nxt in zip(route[:-1], route[1:]):
            distance += haversine(current.lat, current.lng, nxt.lat, nxt.lng)
        return distance

    def _evaluate(self, route: List[Stop], depot: Stop, traffic_index: float) -> RoutePlanResult:
        sequence = route + [depot]
        total_distance = self._route_distance(sequence)
        travel_time = total_distance / max(self.default_speed, 1) * 60
        congestion_penalty = travel_time * (traffic_index - 1) if traffic_index > 1 else 0

        window_violations = 0
        clock = datetime.now()
        for stop in sequence[1:-1]:
            clock += timedelta(minutes=stop.service_minutes)
            if stop.window_start and clock < stop.window_start:
                clock = stop.window_start
            if stop.window_end and clock > stop.window_end:
                window_violations += 1

        score = 1 / (1 + total_distance + congestion_penalty + window_violations * 10)
        return RoutePlanResult(
            sequence=sequence,
            total_distance_km=round(total_distance, 2),
            travel_minutes=int(travel_time + congestion_penalty),
            window_violations=window_violations,
            congestion_penalty=round(congestion_penalty, 2),
            score=round(score, 4)
        )

    def optimize(self, depot: Stop, stops: List[Stop], congestion_map: Optional[Dict[str, float]] = None,
                 traffic_index: float = 1.0, alternatives: int = 2) -> Dict[str, Any]:
        if not stops:
            return {
                "best_plan": RoutePlanResult([depot, depot], 0.0, 0, 0, 0.0, 1.0),
                "alternatives": []
            }

        base_route = self._nearest_neighbor(depot, stops, congestion_map)
        optimized_route = self._two_opt(base_route)
        best_plan = self._evaluate(optimized_route, depot, traffic_index)

        alt_routes = []
        for idx in range(min(alternatives, len(stops))):
            shuffled = stops[idx:] + stops[:idx]
            alt_base = self._nearest_neighbor(depot, shuffled, congestion_map)
            alt_opt = self._two_opt(alt_base)
            alt_routes.append(self._evaluate(alt_opt, depot, traffic_index))

        alt_routes.sort(key=lambda r: r.score, reverse=True)
        return {
            "best_plan": best_plan,
            "alternatives": alt_routes[:alternatives]
        }
