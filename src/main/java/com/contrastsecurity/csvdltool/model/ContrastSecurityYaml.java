package com.contrastsecurity.csvdltool.model;

import java.util.Map;

public class ContrastSecurityYaml {

    private Map<String, Object> api;

    public Map<String, Object> getApi() {
        return api;
    }

    public void setApi(Map<String, Object> api) {
        this.api = api;
    }

    public String getUrl() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("url")) {
            return this.api.get("url").toString();
        }
        return "";
    }

    public String getApiKey() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("api_key")) {
            return this.api.get("api_key").toString();
        }
        return "";
    }

    public String getServiceKey() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("service_key")) {
            return this.api.get("service_key").toString();
        }
        return "";
    }

    public String getUserName() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("user_name")) {
            return this.api.get("user_name").toString();
        }
        return "";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("url         : %s\n", getUrl()));
        builder.append(String.format("api_key     : %s\n", getApiKey()));
        builder.append(String.format("service_key : %s\n", getServiceKey()));
        builder.append(String.format("user_name   : %s\n", getUserName()));
        return builder.toString();
    }

}
