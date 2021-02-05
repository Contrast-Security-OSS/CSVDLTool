package com.contrastsecurity.model;

import java.util.ArrayList;
import java.util.List;

public class StoryJson extends ContrastJson {

    private Story story;

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add(story.toString());
        return String.join("\r\n", strList);
    }

}
