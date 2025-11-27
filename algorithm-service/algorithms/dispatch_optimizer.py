"""任务调度优化模块"""
from __future__ import annotations

from dataclasses import dataclass
from typing import List, Dict, Any
import math


def distance(a: Dict[str, float], b: Dict[str, float]) -> float:
    if not a or not b:
        return 0.0
    return math.sqrt((a.get("lat", 0) - b.get("lat", 0)) ** 2 + (a.get("lng", 0) - b.get("lng", 0)) ** 2)


@dataclass
class DriverProfile:
    id: str
    name: str
    skills: List[str]
    status: str
    capacity: int
    location: Dict[str, float]
    workload: int = 0


@dataclass
class TaskProfile:
    id: str
    required_skill: str
    priority: int
    location: Dict[str, float]
    estimated_duration: int


class DispatchOptimizer:
    """基于启发式评分的调度优化器"""

    def _score(self, driver: DriverProfile, task: TaskProfile) -> float:
        skill_match = 1.0 if task.required_skill in driver.skills else 0.5
        distance_penalty = distance(driver.location, task.location)
        load_penalty = driver.workload / max(driver.capacity, 1)
        priority_bonus = task.priority / 5
        availability = 1.0 if driver.status == "AVAILABLE" else 0.4
        score = skill_match * availability * (1 + priority_bonus) - (distance_penalty + load_penalty)
        return score

    def optimize(self, drivers: List[DriverProfile], tasks: List[TaskProfile]) -> Dict[str, Any]:
        if not drivers or not tasks:
            return {"assignments": [], "unassigned_tasks": [task.id for task in tasks]}

        scores = []
        for driver in drivers:
            for task in tasks:
                scores.append({
                    "driver": driver,
                    "task": task,
                    "score": self._score(driver, task)
                })

        scores.sort(key=lambda item: item["score"], reverse=True)

        assigned_drivers = set()
        assigned_tasks = set()
        assignments = []

        for record in scores:
            driver = record["driver"]
            task = record["task"]
            if driver.id in assigned_drivers or task.id in assigned_tasks:
                continue
            if record["score"] <= 0:
                break
            assignments.append({
                "driver_id": driver.id,
                "driver_name": driver.name,
                "task_id": task.id,
                "score": round(record["score"], 3)
            })
            assigned_drivers.add(driver.id)
            assigned_tasks.add(task.id)

        return {
            "assignments": assignments,
            "unassigned_tasks": [task.id for task in tasks if task.id not in assigned_tasks],
            "idle_drivers": [driver.id for driver in drivers if driver.id not in assigned_drivers]
        }
