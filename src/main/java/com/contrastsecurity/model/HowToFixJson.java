package com.contrastsecurity.model;

import java.util.ArrayList;
import java.util.List;

public class HowToFixJson extends Contrast {
    private String owasp;
    private String cwe;
    private Recommendation recommendation;

    public String getOwasp() {
        return owasp;
    }

    public void setOwasp(String owasp) {
        this.owasp = owasp;
    }

    public String getCwe() {
        return cwe;
    }

    public void setCwe(String cwe) {
        this.cwe = cwe;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("owasp: " + this.owasp);
        strList.add("cwe: " + this.cwe);
        strList.add(this.recommendation.toString());
        return String.join("\r\n", strList);
    }

}
