package com.contrastsecurity.csvdltool.model;

public class Server {
    private String name;
    private String hostname;
    private String serverpath;
    private String environment;
    private String enabled;
    private String server_id;
    private String agent_version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getServerpath() {
        return serverpath;
    }

    public void setServerpath(String serverpath) {
        this.serverpath = serverpath;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getEnabled() {
        return enabled;
    }

    public boolean isEnabled() {
        return Boolean.valueOf(this.enabled);
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public String getAgent_version() {
        return agent_version;
    }

    public void setAgent_version(String agent_version) {
        this.agent_version = agent_version;
    }

}
