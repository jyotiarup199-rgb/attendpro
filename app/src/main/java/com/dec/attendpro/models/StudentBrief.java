package com.dec.attendpro.models;

public class StudentBrief {
    private String name;
    private String id;
    private String photoUrl;

    public StudentBrief(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public StudentBrief(String name, String id, String photoUrl) {
        this.name = name;
        this.id = id;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
