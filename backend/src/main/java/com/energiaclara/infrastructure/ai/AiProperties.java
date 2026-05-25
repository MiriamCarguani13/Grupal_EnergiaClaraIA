package com.energiaclara.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private boolean enabled = false;
    private String baseUrl = "http://localhost:8090";
    private String predictPath = "/predict-energy-anomaly";
    private int timeoutMs = 2000;
    private boolean fallbackToRules = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPredictPath() {
        return predictPath;
    }

    public void setPredictPath(String predictPath) {
        this.predictPath = predictPath;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isFallbackToRules() {
        return fallbackToRules;
    }

    public void setFallbackToRules(boolean fallbackToRules) {
        this.fallbackToRules = fallbackToRules;
    }
}
