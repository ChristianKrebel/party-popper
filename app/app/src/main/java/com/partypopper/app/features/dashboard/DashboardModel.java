package com.partypopper.app.features.dashboard;

public class DashboardModel {

    private String title;
    private String date;
    private String image;
    private String organizer;
    private long visitor_count;

    public DashboardModel() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOrganizer() { return organizer; }

    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public long getVisitor_count() {
        return visitor_count;
    }

    public void setVisitor_count(long visitor_count) {
        this.visitor_count = visitor_count;
    }
}
