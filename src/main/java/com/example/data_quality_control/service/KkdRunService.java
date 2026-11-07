package com.example.data_quality_control.service;

import com.example.data_quality_control.domain.KkdRun;
import com.example.data_quality_control.repository.KkdRunRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KkdRunService {

    private final KkdRunRepository kkdRunRepository;

    public KkdRunService(KkdRunRepository kkdRunRepository) {
        this.kkdRunRepository = kkdRunRepository;
    }

    public List<KkdRun> getAllRuns() {
        return kkdRunRepository.findAll();
    }
}
