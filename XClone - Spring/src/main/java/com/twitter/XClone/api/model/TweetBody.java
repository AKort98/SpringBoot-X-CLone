package com.twitter.XClone.api.model;

import com.twitter.XClone.model.Images;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;


public class TweetBody {

    @Size(min = 0, max = 4)
    private List<String> images;
    @NotBlank
    @NotNull
    private String content;

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}