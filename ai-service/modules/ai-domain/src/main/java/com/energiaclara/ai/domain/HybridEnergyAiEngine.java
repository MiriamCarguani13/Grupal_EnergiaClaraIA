package com.energiaclara.ai.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public final class HybridEnergyAiEngine {

    public static final String MODEL_VERSION = "hybrid-stat-rules-v1.0";
    public static final int DEFAULT_MINIMUM_HISTORY_SIZE = 5;
    public static final int DEFAULT_HISTORY_WINDOW_SIZE = 7;

    private HybridEnergyAiEngine() {
    }

    public static EnergyAiResult analyze(EnergyAiInput input, List<EnergyAiHistoricalReading> history) {
        return analyze(input, history, DEFAULT_MINIMUM_HISTORY_SIZE, DEFAULT_HISTORY_WINDOW_SIZE);
    }

    public static EnergyAiResult analyze(
            EnergyAiInput input,
            List<EnergyAiHistoricalReading> history,
            int minimumHistorySize,
            int historyWindowSize
    ) {
        if (minimumHistorySize <= 0) {
            throw new IllegalArgumentException("minimumHistorySize must be greater than zero");
        }
        if (historyWindowSize < minimumHistorySize) {
            throw new IllegalArgumentException("historyWindowSize must be greater than or equal to minimumHistorySize");
        }

        List<EnergyAiHistoricalReading> window = normalizedWindow(history, historyWindowSize);
        DynamicBaseline baseline = resolveBaseline(input, window, minimumHistorySize);
        StatisticalAnomalyScore score = calculateScore(input, baseline);
        ExplainableRecommendation recommendation = explain(input, baseline, score, minimumHistorySize);

        return new EnergyAiResult(baseline, score, recommendation, baseline.fallback(), MODEL_VERSION);
    }

    private static List<EnergyAiHistoricalReading> normalizedWindow(
            List<EnergyAiHistoricalReading> history,
            int historyWindowSize
    ) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        return history.stream()
                .filter(reading -> reading != null && reading.kwh() != null)
                .sorted(Comparator.comparing(EnergyAiHistoricalReading::measuredAt).reversed())
                .limit(historyWindowSize)
                .toList();
    }

    private static DynamicBaseline resolveBaseline(
            EnergyAiInput input,
            List<EnergyAiHistoricalReading> window,
            int minimumHistorySize
    ) {
        if (window.size() >= minimumHistorySize) {
            BigDecimal movingAverage = movingAverage(window);
            BigDecimal standardDeviation = standardDeviation(window, movingAverage);
            return new DynamicBaseline(
                    movingAverage,
                    movingAverage,
                    standardDeviation,
                    window.size(),
                    DynamicBaseline.Source.HISTORY,
                    false
            );
        }

        if (input.staticBaselineKwh() != null && input.staticBaselineKwh().compareTo(BigDecimal.ZERO) > 0) {
            return new DynamicBaseline(
                    scale(input.staticBaselineKwh()),
                    scale(input.staticBaselineKwh()),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    window.size(),
                    DynamicBaseline.Source.STATIC_BASELINE,
                    true
            );
        }

        return new DynamicBaseline(
                scale(input.kwh()),
                scale(input.kwh()),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                window.size(),
                DynamicBaseline.Source.INPUT_AS_EXPECTED,
                true
        );
    }

    private static StatisticalAnomalyScore calculateScore(EnergyAiInput input, DynamicBaseline baseline) {
        BigDecimal deviationPercent = deviationPercent(input.kwh(), baseline.expectedKwh());
        BigDecimal excessKwh = scale(input.kwh().subtract(baseline.expectedKwh()).max(BigDecimal.ZERO));
        BigDecimal zScore = zScore(input.kwh(), baseline.expectedKwh(), baseline.standardDeviationKwh());
        boolean anomalyDetected = isAnomaly(deviationPercent, zScore, input.effectiveTolerancePercent());
        HybridSeverity severity = severityFor(deviationPercent, zScore, anomalyDetected);
        BigDecimal anomalyScore = anomalyScoreFor(severity, deviationPercent, zScore);
        BigDecimal confidence = confidenceFor(baseline, anomalyDetected);
        BigDecimal estimatedCostImpact = scale(excessKwh.multiply(input.effectiveCostPerKwh()));
        BigDecimal estimatedCo2Impact = scale(excessKwh.multiply(input.effectiveCo2KgPerKwh()));

        return new StatisticalAnomalyScore(
                deviationPercent,
                zScore,
                excessKwh,
                anomalyScore,
                severity,
                confidence,
                anomalyDetected,
                estimatedCostImpact,
                estimatedCo2Impact
        );
    }

    private static boolean isAnomaly(BigDecimal deviationPercent, BigDecimal zScore, BigDecimal tolerancePercent) {
        return deviationPercent.compareTo(tolerancePercent) > 0 || zScore.compareTo(new BigDecimal("2.00")) >= 0;
    }

    private static HybridSeverity severityFor(BigDecimal deviationPercent, BigDecimal zScore, boolean anomalyDetected) {
        if (!anomalyDetected) {
            return HybridSeverity.NORMAL;
        }
        if (deviationPercent.compareTo(new BigDecimal("100.00")) >= 0 || zScore.compareTo(new BigDecimal("4.00")) >= 0) {
            return HybridSeverity.CRITICAL;
        }
        if (deviationPercent.compareTo(new BigDecimal("50.00")) >= 0 || zScore.compareTo(new BigDecimal("3.00")) >= 0) {
            return HybridSeverity.HIGH;
        }
        if (deviationPercent.compareTo(new BigDecimal("25.00")) >= 0 || zScore.compareTo(new BigDecimal("2.00")) >= 0) {
            return HybridSeverity.MEDIUM;
        }
        return HybridSeverity.LOW;
    }

    private static ExplainableRecommendation explain(
            EnergyAiInput input,
            DynamicBaseline baseline,
            StatisticalAnomalyScore score,
            int minimumHistorySize
    ) {
        String baselineReason = switch (baseline.source()) {
            case HISTORY -> "Se uso baseline dinamico calculado con " + baseline.sampleCount() + " lecturas historicas.";
            case STATIC_BASELINE -> "No hay historial suficiente (" + baseline.sampleCount() + "/" + minimumHistorySize
                    + "); se uso el baseline fijo disponible.";
            case INPUT_AS_EXPECTED -> "No hay historial suficiente ni baseline fijo; se uso la lectura actual como referencia segura.";
        };

        String explanation = baselineReason + " Lectura=" + fmt(input.kwh()) + " kWh, esperado="
                + fmt(baseline.expectedKwh()) + " kWh, desviacion=" + fmt(score.deviationPercent())
                + "%, zScore=" + fmt(score.zScore()) + ", severidad=" + score.severity() + ".";

        String recommendation = recommendationFor(score, input);
        return new ExplainableRecommendation(explanation, recommendation);
    }

    private static String recommendationFor(StatisticalAnomalyScore score, EnergyAiInput input) {
        String electricalHint = electricalHint(input);
        String base = switch (score.severity()) {
            case NORMAL -> "Consumo dentro del patron esperado; continuar monitoreo regular.";
            case LOW -> "Monitorear proximas lecturas y verificar si el incremento se repite.";
            case MEDIUM -> "Revisar horarios de uso y equipos activos fuera de horario.";
            case HIGH -> "Inspeccionar equipos de alto consumo, climatizacion e iluminacion del area.";
            case CRITICAL -> "Escalar a mantenimiento y auditoria energetica para revision prioritaria.";
        };
        return electricalHint.isBlank() ? base : base + " " + electricalHint;
    }

    private static String electricalHint(EnergyAiInput input) {
        if (input.powerFactor() != null && input.powerFactor().compareTo(new BigDecimal("0.85")) < 0) {
            return "El factor de potencia es bajo; revisar cargas inductivas o compensacion.";
        }
        if (input.voltage() != null
                && (input.voltage().compareTo(new BigDecimal("200")) < 0
                || input.voltage().compareTo(new BigDecimal("240")) > 0)) {
            return "El voltaje esta fuera del rango esperado; revisar estabilidad electrica.";
        }
        return "";
    }

    private static BigDecimal movingAverage(List<EnergyAiHistoricalReading> window) {
        BigDecimal sum = window.stream()
                .map(EnergyAiHistoricalReading::kwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return scale(sum.divide(BigDecimal.valueOf(window.size()), 6, RoundingMode.HALF_UP));
    }

    private static BigDecimal standardDeviation(List<EnergyAiHistoricalReading> window, BigDecimal average) {
        double avg = average.doubleValue();
        double variance = window.stream()
                .map(EnergyAiHistoricalReading::kwh)
                .mapToDouble(BigDecimal::doubleValue)
                .map(value -> Math.pow(value - avg, 2))
                .average()
                .orElse(0.0);
        return scale(BigDecimal.valueOf(Math.sqrt(variance)));
    }

    private static BigDecimal deviationPercent(BigDecimal currentKwh, BigDecimal expectedKwh) {
        if (expectedKwh.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scale(currentKwh.subtract(expectedKwh)
                .divide(expectedKwh, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")));
    }

    private static BigDecimal zScore(BigDecimal currentKwh, BigDecimal expectedKwh, BigDecimal standardDeviation) {
        if (standardDeviation == null || standardDeviation.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scale(currentKwh.subtract(expectedKwh).divide(standardDeviation, 6, RoundingMode.HALF_UP));
    }

    private static BigDecimal anomalyScoreFor(HybridSeverity severity, BigDecimal deviationPercent, BigDecimal zScore) {
        BigDecimal base = switch (severity) {
            case NORMAL -> new BigDecimal("0.00");
            case LOW -> new BigDecimal("0.25");
            case MEDIUM -> new BigDecimal("0.50");
            case HIGH -> new BigDecimal("0.75");
            case CRITICAL -> new BigDecimal("0.95");
        };
        BigDecimal deviationBoost = deviationPercent.max(BigDecimal.ZERO)
                .divide(new BigDecimal("500"), 4, RoundingMode.HALF_UP);
        BigDecimal zBoost = zScore.max(BigDecimal.ZERO)
                .divide(new BigDecimal("20"), 4, RoundingMode.HALF_UP);
        return scale(base.add(deviationBoost).add(zBoost).min(BigDecimal.ONE));
    }

    private static BigDecimal confidenceFor(DynamicBaseline baseline, boolean anomalyDetected) {
        BigDecimal confidence = switch (baseline.source()) {
            case HISTORY -> new BigDecimal("0.80")
                    .add(BigDecimal.valueOf(Math.min(baseline.sampleCount(), DEFAULT_HISTORY_WINDOW_SIZE))
                            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            case STATIC_BASELINE -> new BigDecimal("0.55");
            case INPUT_AS_EXPECTED -> new BigDecimal("0.30");
        };
        if (!anomalyDetected) {
            confidence = confidence.subtract(new BigDecimal("0.05"));
        }
        return scale(confidence.max(BigDecimal.ZERO).min(new BigDecimal("0.95")));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String fmt(BigDecimal value) {
        return scale(value).toPlainString();
    }
}

