package com.contrastsecurity.csvdltool.model;

import java.util.List;

public class Route {
    private String signature;
    private List<Observation> observations;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }

}
