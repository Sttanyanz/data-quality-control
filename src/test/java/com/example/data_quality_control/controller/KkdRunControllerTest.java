package com.example.data_quality_control.controller;

import com.example.data_quality_control.service.KkdRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KkdRunController.class)
class KkdRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KkdRunService kkdRunService;

    @Test
    void triggerRun_returns201Created() throws Exception {
        doNothing().when(kkdRunService).executeRun();

        mockMvc.perform(post("/api/runs"))
            .andExpect(status().isCreated());
    }
}
