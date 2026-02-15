package com.aegis.agent.api.dto;

public class ComponentStatusItem {

    private String status;
    private String url;
    private String detail;

    public ComponentStatusItem() {
    }

    public ComponentStatusItem(String status, String url, String detail) {
        this.status = status;
        this.url = url;
        this.detail = detail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
