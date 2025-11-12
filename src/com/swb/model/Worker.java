package com.swb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Worker implements Serializable {
    private static final long serialVersionUID = 1L;
    public String username;
    public String password;
    public int workloadScore = 0;
    public List<String> blackoutDays = new ArrayList<>(); // weekly blackouts stored here

    public Worker(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return username + " [load=" + workloadScore + "] (off=" + blackoutDays + ")";
    }
}
