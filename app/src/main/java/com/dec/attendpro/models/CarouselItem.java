package com.dec.attendpro.models;

public class CarouselItem {
    private String tag;
    private String title;
    private String description;
    private int backgroundRes; // Optional if you want different gradients

    public CarouselItem(String tag, String title, String description) {
        this.tag = tag;
        this.title = title;
        this.description = description;
    }

    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
