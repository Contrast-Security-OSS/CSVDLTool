package com.contrastsecurity.csvdltool.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MyCookieJar implements CookieJar {

    private HttpUrl contrastUrl;
    private HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

    public MyCookieJar(String url) {
        this.contrastUrl = HttpUrl.parse(url);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl arg0) {
        List<Cookie> cookies = cookieStore.get(this.contrastUrl);
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }

    @Override
    public void saveFromResponse(HttpUrl arg0, List<Cookie> cookies) {
        cookieStore.put(this.contrastUrl, cookies);
    }

}
