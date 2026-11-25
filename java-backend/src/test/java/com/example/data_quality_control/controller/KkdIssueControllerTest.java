package com.example.data_quality_control.controller;

import com.example.data_quality_control.domain.KkdIssueDto;
import com.example.data_quality_control.domain.PageResponse;
import com.example.data_quality_control.service.KkdIssueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KkdIssueController.class)
class KkdIssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KkdIssueService kkdIssueService;

    @Test
    void getIssues_returns200WithPageResponse() throws Exception {
        PageResponse<KkdIssueDto> page = new PageResponse<>(
            List.of(new KkdIssueDto(1L, "RULE_A", "HIGH", "GOAL", 10L, "message")),
            20, 0, 1
        );
        when(kkdIssueService.getIssues(anyLong(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
            .thenReturn(page);

        mockMvc.perform(get("/api/kkd/issues").param("run_id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.limit").value(20))
            .andExpect(jsonPath("$.offset").value(0))
            .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getIssues_negativeLimit_returns422WithError() throws Exception {
        mockMvc.perform(get("/api/kkd/issues")
                .param("run_id", "1")
                .param("limit", "-5"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    void getIssues_invalidRunIdType_returns422() throws Exception {
        mockMvc.perform(get("/api/kkd/issues").param("run_id", "abc"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
    }
}
