package com.aegis.agent.api.dto;

import java.util.List;
import java.util.Map;

public class IncidentTimelineResponse {

    private String correlationId;
    private int total;
    private List<Map<String, Object>> events;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Map<String, Object>> getEvents() {
        return events;
    }

    public void setEvents(List<Map<String, Object>> events) {
        this.events = events;
    }
}
