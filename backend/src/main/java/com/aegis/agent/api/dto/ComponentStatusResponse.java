package com.aegis.agent.api.dto;

import java.util.Map;

public class ComponentStatusResponse {

    private Map<String, String> statuses;

    public Map<String, String> getStatuses() {
        return statuses;
    }

    public void setStatuses(Map<String, String> statuses) {
        this.statuses = statuses;
    }
}
