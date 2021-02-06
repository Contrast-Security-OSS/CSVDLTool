package com.contrastsecurity.comware.api;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.comware.json.OrganizationJson;
import com.contrastsecurity.comware.model.Contrast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class OrganizationAPI extends Api {
    private String url;
    private String username;
    private String sevice_key;
    private String api_key;

    public OrganizationAPI(IPreferenceStore preferenceStore, String url, String username, String sevice_key, String api_key) {
        super(preferenceStore);
        this.url = url;
        this.username = username;
        this.sevice_key = sevice_key;
        this.api_key = api_key;
    }

    @Override
    protected String getUrl() {
        return String.format("%s/api/ng/profile/organizations/default", this.url);
    }

    @Override
    protected List<Header> getHeaders() {
        String auth = String.format("%s:%s", this.username, this.sevice_key);
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = new String(encodedAuth);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
        headers.add(new BasicHeader("API-Key", this.api_key));
        headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader));
        return headers;
    }

    @Override
    protected Contrast convert(String response) {
        Gson gson = new Gson();
        Type organizationType = new TypeToken<OrganizationJson>() {
        }.getType();
        OrganizationJson organizationJson = gson.fromJson(response, organizationType);
        return organizationJson.getOrganization();
    }

}
