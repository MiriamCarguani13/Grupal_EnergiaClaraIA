package com.energiaclara.energyops.api;

import com.energiaclara.energyops.api.dto.AnalyzeReadingRequest;
import com.energiaclara.energyops.api.dto.AnalyzeReadingResponse;
import com.energiaclara.energyops.application.EnergyAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/energyops")
public class EnergyOpsController {

    private final EnergyAnalysisService energyAnalysisService;

    public EnergyOpsController(EnergyAnalysisService energyAnalysisService) {
        this.energyAnalysisService = energyAnalysisService;
    }

    @PostMapping("/analyze-reading")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalyzeReadingResponse analyzeReading(@Valid @RequestBody AnalyzeReadingRequest request) {
        return energyAnalysisService.analyze(request);
    }
}
