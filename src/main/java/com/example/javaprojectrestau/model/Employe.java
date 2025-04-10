package com.example.javaprojectrestau.model;

public class Employe {
    private Long id;
    private String name;
    private int working_hour;
    private int hour_worked;
    private String post;

    public Employe(Long id, String name, int working_hour, int hour_worked, String post) {
        this.id = id;
        this.name = name;
        this.working_hour = working_hour;
        this.hour_worked = hour_worked;
        this.post = post;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWorking_hour() {
        return working_hour;
    }

    public void setWorking_hour(int working_hour) {
        this.working_hour = working_hour;
    }
    public int getHour_worked() {
        return hour_worked;
    }

    public void setHour_worked(int hour_worked) {
        this.hour_worked = hour_worked;
    }
    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}
