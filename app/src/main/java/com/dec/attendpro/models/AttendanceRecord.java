package com.dec.attendpro.models;

public class AttendanceRecord {
    public enum Status {
        PRESENT, ABSENT, LATE
    }

    private String subject;
    private String date;
    private Status status;

    public AttendanceRecord(String subject, String date, Status status) {
        this.subject = subject;
        this.date = date;
        this.status = status;
    }

    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public Status getStatus() { return status; }
}
