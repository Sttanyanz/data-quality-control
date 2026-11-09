package com.example.data_quality_control.controller;

import com.example.data_quality_control.domain.summary.MetricDto;
import com.example.data_quality_control.domain.summary.RunInfo;
import com.example.data_quality_control.domain.summary.SummaryResponse;
import com.example.data_quality_control.domain.summary.UnitInfo;
import com.example.data_quality_control.service.KkdSummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KkdSummaryController.class)
class KkdSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KkdSummaryService kkdSummaryService;

    @Test
    void getSummary_returns200WithRunUnitMetrics() throws Exception {
        SummaryResponse summary = new SummaryResponse(
            new RunInfo(1L, LocalDate.of(2026, 11, 9)),
            null,
            new UnitInfo(1L, "HQ"),
            List.of(new MetricDto("goals_total", "Всего целей", "шт", 10.0, 8.0, 2.0, "UP")),
            List.of(),
            null
        );
        when(kkdSummaryService.getSummary(anyLong(), anyLong())).thenReturn(summary);

        mockMvc.perform(get("/api/kkd/summary")
                .param("run_id", "1")
                .param("unit_id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.run.id").value(1))
            .andExpect(jsonPath("$.unit.id").value(1))
            .andExpect(jsonPath("$.unit.name").value("HQ"))
            .andExpect(jsonPath("$.metrics").isArray())
            .andExpect(jsonPath("$.metrics[0].key").value("goals_total"));
    }
}
