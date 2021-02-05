package com.contrastsecurity.model;

import java.util.List;

public abstract class ContrastJson {
    private String success;
    private List<String> messages;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.success, this.messages);
    }

}
