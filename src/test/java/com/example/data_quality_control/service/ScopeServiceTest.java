package com.example.data_quality_control.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScopeServiceTest {

    @Autowired
    private ScopeService scopeService;

    @Test
    void shouldCollectAllGoalsForRootUnit() {
        // Департамент HQ (id=1) должен включать в себя все активные цели
        Set<Long> scope = scopeService.getScopeGoalIds(1L);

        assertThat(scope).isNotEmpty();
        assertThat(scope).contains(1L, 2L, 3L, 6L, 7L, 18L); // Выборочная проверка
    }

    @Test
    void shouldCollectCascadedGoalsAcrossUnits() {
        // Департамент CORP (id=2) должен подтянуть цель 18 из RETAIL,
        // так как она каскадирована (parent_goal_id = 7)
        Set<Long> scope = scopeService.getScopeGoalIds(2L);

        assertThat(scope).contains(6L, 7L, 8L, 18L);
        assertThat(scope).doesNotContain(1L, 2L, 24L); // Цели HQ не должны попасть
    }
}