package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.summary.GoalData;
import com.example.data_quality_control.repository.GoalRepository;
import com.example.data_quality_control.repository.UnitRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScopeService {

    private final UnitRepository unitRepository;
    private final GoalRepository goalRepository;

    public ScopeService(UnitRepository unitRepository, GoalRepository goalRepository) {
        this.unitRepository = unitRepository;
        this.goalRepository = goalRepository;
    }

    public Set<Long> getScopeGoalIds(Long unitId) {
        // Get unit parent map and collect all descendant unit IDs
        Map<Long, Long> unitParents = unitRepository.getUnitParentMap();
        Set<Long> unitIds = collectUnitIds(unitId, unitParents);

        // Get all goals
        List<GoalData> allGoals = goalRepository.findAllGoals();

        // Build maps for goal lookup
        Map<Long, GoalData> goalsById = allGoals.stream()
            .collect(Collectors.toMap(GoalData::id, g -> g));

        Map<Long, List<Long>> childrenByParentId = allGoals.stream()
            .filter(g -> g.parentGoalId() != null)
            .collect(Collectors.groupingBy(
                GoalData::parentGoalId,
                Collectors.mapping(GoalData::id, Collectors.toList())
            ));

        // Find base goals: goals in the unit scope with ACTIVE status
        Set<Long> scopeGoalIds = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();

        for (GoalData goal : allGoals) {
            if (unitIds.contains(goal.unitId()) && "ACTIVE".equals(goal.status())) {
                queue.add(goal.id());
                scopeGoalIds.add(goal.id());
            }
        }

        // BFS to collect scope
        Set<Long> visited = new HashSet<>(queue);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<Long> children = childrenByParentId.getOrDefault(currentId, List.of());

            for (Long childId : children) {
                if (!visited.contains(childId)) {
                    visited.add(childId);
                    queue.add(childId);

                    GoalData child = goalsById.get(childId);
                    if (child != null && "ACTIVE".equals(child.status())) {
                        scopeGoalIds.add(childId);
                    }
                }
            }
        }

        return scopeGoalIds;
    }

    private Set<Long> collectUnitIds(Long unitId, Map<Long, Long> unitParents) {
        Set<Long> unitIds = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(unitId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            if (!unitIds.add(currentId)) {
                continue;
            }

            // Find all children (units where parent_id == currentId)
            for (Map.Entry<Long, Long> entry : unitParents.entrySet()) {
                if (currentId.equals(entry.getValue())) {
                    queue.add(entry.getKey());
                }
            }
        }

        return unitIds;
    }
}
