package com.swb.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class AssignmentRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    public String day; // MONDAY.. string
    public LinkedHashMap<String,String> assignments = new LinkedHashMap<>();

    public AssignmentRecord(String day) { this.day = day; }

    public void put(String task, String worker) { assignments.put(task, worker); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(day + "\n");
        for (Map.Entry<String,String> e : assignments.entrySet()) sb.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
        return sb.toString();
    }
}
