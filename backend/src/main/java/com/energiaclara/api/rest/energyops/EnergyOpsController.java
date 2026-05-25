package com.energiaclara.api.rest.energyops;

import com.energiaclara.api.rest.energyops.dto.AnalyzeReadingRequest;
import com.energiaclara.api.rest.energyops.dto.AnalyzeReadingResponse;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingCommand;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingResult;
import com.energiaclara.application.port.in.AnalyzeEnergyReadingUseCase;
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

    private final AnalyzeEnergyReadingUseCase analyzeEnergyReadingUseCase;

    public EnergyOpsController(AnalyzeEnergyReadingUseCase analyzeEnergyReadingUseCase) {
        this.analyzeEnergyReadingUseCase = analyzeEnergyReadingUseCase;
    }

    @PostMapping("/analyze-reading")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalyzeReadingResponse analyzeReading(@Valid @RequestBody AnalyzeReadingRequest request) {
        return toResponse(analyzeEnergyReadingUseCase.analyze(toCommand(request)));
    }

    private AnalyzeEnergyReadingCommand toCommand(AnalyzeReadingRequest request) {
        return new AnalyzeEnergyReadingCommand(
                request.facilityId(),
                request.meterId(),
                request.measuredAt(),
                request.kwh(),
                request.voltage(),
                request.powerFactor()
        );
    }

    private AnalyzeReadingResponse toResponse(AnalyzeEnergyReadingResult result) {
        return new AnalyzeReadingResponse(
                result.readingId(),
                result.anomalyId(),
                result.anomalyDetected(),
                result.severity() == null ? null : result.severity().name(),
                result.deviationPercent(),
                result.recommendation(),
                result.estimatedCostImpact(),
                result.estimatedCo2Impact()
        );
    }
}
