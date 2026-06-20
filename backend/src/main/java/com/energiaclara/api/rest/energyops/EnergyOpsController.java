package com.energiaclara.api.rest.energyops;

import com.energiaclara.api.rest.energyops.dto.AnalyzeReadingRequest;
import com.energiaclara.api.rest.energyops.dto.AnalyzeReadingResponse;
import com.energiaclara.api.rest.energyops.dto.EnergyReadingDto;
import com.energiaclara.api.rest.energyops.dto.EnergyReadingTrendDto;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingCommand;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingResult;
import com.energiaclara.application.energyops.dto.EnergyReadingHistoryResult;
import com.energiaclara.application.energyops.dto.EnergyReadingTrendResult;
import com.energiaclara.application.port.in.AnalyzeEnergyReadingUseCase;
import com.energiaclara.application.port.in.GetEnergyReadingsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/energyops")
public class EnergyOpsController {

    private final AnalyzeEnergyReadingUseCase analyzeEnergyReadingUseCase;
    private final GetEnergyReadingsUseCase getEnergyReadingsUseCase;

    public EnergyOpsController(AnalyzeEnergyReadingUseCase analyzeEnergyReadingUseCase,
                               GetEnergyReadingsUseCase getEnergyReadingsUseCase) {
        this.analyzeEnergyReadingUseCase = analyzeEnergyReadingUseCase;
        this.getEnergyReadingsUseCase = getEnergyReadingsUseCase;
    }

    @GetMapping("/readings")
    public List<EnergyReadingDto> readings() {
        return getEnergyReadingsUseCase.readings().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/readings/trends")
    public List<EnergyReadingTrendDto> readingTrends() {
        return getEnergyReadingsUseCase.trends().stream()
                .map(this::toDto)
                .toList();
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

    private EnergyReadingDto toDto(EnergyReadingHistoryResult result) {
        return new EnergyReadingDto(
                result.id(),
                result.meterId(),
                result.consumptionKwh(),
                result.voltage(),
                result.powerFactor(),
                result.baselineKwh(),
                result.deviationPercentage(),
                result.result(),
                result.createdAt()
        );
    }

    private EnergyReadingTrendDto toDto(EnergyReadingTrendResult result) {
        return new EnergyReadingTrendDto(
                result.date(),
                result.consumptionKwh(),
                result.baselineKwh(),
                result.deviationPercentage(),
                result.result()
        );
    }
}
