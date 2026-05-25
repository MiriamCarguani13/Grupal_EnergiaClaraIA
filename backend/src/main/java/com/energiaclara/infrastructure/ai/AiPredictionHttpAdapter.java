package com.energiaclara.infrastructure.ai;

import com.energiaclara.application.energyops.dto.EnergyAiPredictionInput;
import com.energiaclara.application.energyops.dto.EnergyAiPredictionResult;
import com.energiaclara.application.port.out.EnergyAiPredictionPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Component
@EnableConfigurationProperties(AiProperties.class)
public class AiPredictionHttpAdapter implements EnergyAiPredictionPort {

    private static final Logger log = LoggerFactory.getLogger(AiPredictionHttpAdapter.class);

    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    public AiPredictionHttpAdapter(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<EnergyAiPredictionResult> predict(EnergyAiPredictionInput input) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }

        Instant startedAt = Instant.now();

        try {
            AiPredictionRequest request = new AiPredictionRequest(
                    input.tenantId(),
                    input.medidorId(),
                    input.facilityId(),
                    input.meterId(),
                    input.measuredAt(),
                    input.kwh(),
                    input.voltage(),
                    input.powerFactor(),
                    input.baselineKwh(),
                    input.tolerancePercent()
            );

            String inputJson = objectMapper.writeValueAsString(request);
            String inputHash = sha256(inputJson);

            RestClient restClient = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .build();

            AiPredictionResponse response = restClient.post()
                    .uri(properties.getPredictPath())
                    .body(request)
                    .retrieve()
                    .body(AiPredictionResponse.class);

            if (response == null) {
                return Optional.empty();
            }

            String outputJson = objectMapper.writeValueAsString(response);
            String outputHash = sha256(outputJson);
            long latencyMs = Duration.between(startedAt, Instant.now()).toMillis();

            return Optional.of(new EnergyAiPredictionResult(
                    response.modelVersion(),
                    response.predictedKwh(),
                    response.confidence(),
                    response.anomalyDetected(),
                    response.anomalyScore(),
                    response.explanation(),
                    response.recommendation(),
                    latencyMs,
                    inputHash,
                    outputHash
            ));
        } catch (Exception ex) {
            log.warn("No se pudo obtener prediccion IA. Se usara fallback por reglas si esta habilitado. error={}",
                    ex.getMessage());
            return Optional.empty();
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encoded);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo calcular hash SHA-256", ex);
        }
    }
}
