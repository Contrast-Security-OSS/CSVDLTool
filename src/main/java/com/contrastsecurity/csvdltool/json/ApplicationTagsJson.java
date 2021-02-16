package com.contrastsecurity.csvdltool.json;

import java.util.List;

public class ApplicationTagsJson extends ContrastJson {
    private List<String> tags;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
