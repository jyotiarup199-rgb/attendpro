package com.dec.attendpro.models;

public class ClassInfo {
    private String subject;
    private String time;
    private String room;
    private int studentCount;

    public ClassInfo(String subject, String time, String room, int studentCount) {
        this.subject = subject;
        this.time = time;
        this.room = room;
        this.studentCount = studentCount;
    }

    public String getSubject() { return subject; }
    public String getTime() { return time; }
    public String getRoom() { return room; }
    public int getStudentCount() { return studentCount; }
}
