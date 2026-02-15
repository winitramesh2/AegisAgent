package com.aegis.agent.api.dto;

import java.util.Map;

public class ComponentStatusResponse {

    private Map<String, ComponentStatusItem> components;

    public Map<String, ComponentStatusItem> getComponents() {
        return components;
    }

    public void setComponents(Map<String, ComponentStatusItem> components) {
        this.components = components;
    }
}
