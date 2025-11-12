package com.swb.model;

import java.io.Serializable;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public TaskType type;
    public int difficulty;
    public String scheduledDay; // MONDAY.. for weekly tasks
    public String lastAssignedTo; // track last assignee to avoid immediate repeat

    public Task(String name, TaskType type, int difficulty) {
        this.name = name;
        this.type = type;
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return name + " (d=" + difficulty + ", " + type + (scheduledDay!=null?(", "+scheduledDay):"") + ")";
    }
}
