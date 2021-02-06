package com.contrastsecurity.csvdltool.json;

import com.contrastsecurity.csvdltool.model.HttpRequest;

public class HttpRequestJson extends ContrastJson {
    private HttpRequest http_request;

    public HttpRequest getHttp_request() {
        return http_request;
    }

    public void setHttp_request(HttpRequest http_request) {
        this.http_request = http_request;
    }

}
