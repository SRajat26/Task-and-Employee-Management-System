package com.swb.core;

import com.swb.model.*;
import java.io.*;
import java.util.*;

public class DataManager {
    private final File file;
    private DataStore store;

    public static class DataStore implements Serializable {
        private static final long serialVersionUID = 1L;
        public List<Worker> workers = new ArrayList<>();
        public List<Task> tasks = new ArrayList<>();
        public List<BlackoutRequest> blackoutRequests = new ArrayList<>();
        public AssignmentRecord currentAssignment = null;
        public List<AssignmentRecord> history = new ArrayList<>();
        public List<String[]> supervisors = new ArrayList<>(); // [username,password]
        public String currentDay = java.time.DayOfWeek.from(java.time.LocalDate.now()).toString();
    }

    public DataManager(String path) {
        this.file = new File(path);
        load();
    }

    private void load() {
        try {
            if (!file.exists()) { initDefault(); save(); return; }
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                store = (DataStore) ois.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // fallback to defaults to avoid crash
            initDefault();
            save();
        }
    }

    private void initDefault() {
        store = new DataStore();
        store.supervisors.add(new String[]{"admin","admin"});
        store.workers.add(new Worker("alice","alice123"));
        store.workers.add(new Worker("bob","bob123"));
        store.workers.add(new Worker("charlie","charlie123"));
        store.tasks.add(new Task("Clean Corridor", TaskType.DAILY, 2));
        store.tasks.add(new Task("Wash Dishes", TaskType.DAILY, 3));
        Task t = new Task("Refill Water", TaskType.WEEKLY, 1);
        t.scheduledDay = "WEDNESDAY";
        store.tasks.add(t);
        store.currentDay = java.time.DayOfWeek.from(java.time.LocalDate.now()).toString();
    }

    public synchronized void save() {
        try {
            file.getParentFile().mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(store);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Authentication ---
    public boolean authenticateSupervisor(String user, String pass) {
        for (String[] s : store.supervisors) if (s[0].equals(user) && s[1].equals(pass)) return true;
        return false;
    }

    public boolean authenticateWorker(String user, String pass) {
        for (Worker w : store.workers) if (w.username.equals(user) && w.password.equals(pass)) return true;
        return false;
    }

    // --- Workers ---
    public List<Worker> getWorkers() { return store.workers; }
    public void addWorker(Worker w) { store.workers.add(w); save(); }
    public void removeWorker(String username) { store.workers.removeIf(x -> x.username.equals(username)); save(); }

    // --- Tasks ---
    public List<Task> getTasks() { return store.tasks; }
    public void addTask(Task t) { store.tasks.add(t); save(); }
    public void removeTask(String name) { store.tasks.removeIf(x -> x.name.equals(name)); save(); }

    // --- Blackouts ---
    public List<BlackoutRequest> getBlackoutRequests() { return store.blackoutRequests; }
    public void addBlackoutRequest(BlackoutRequest r) { store.blackoutRequests.add(r); save(); }
    public void updateBlackoutRequests(List<BlackoutRequest> list) { store.blackoutRequests = list; save(); }

    // --- Assignments & history ---
    public AssignmentRecord getCurrentAssignment() { return store.currentAssignment; }
    public void setCurrentAssignment(AssignmentRecord a) { store.currentAssignment = a; save(); }
    public List<AssignmentRecord> getHistory() { return store.history; }
    public List<AssignmentRecord> getAssignments() {
        List<AssignmentRecord> all = new ArrayList<>(store.history);
        if (store.currentAssignment != null) all.add(store.currentAssignment);
        return all;
    }
    public void addToHistory(AssignmentRecord a) { store.history.add(a); save(); }

    // --- Day handling ---
    public String getCurrentDay() { return store.currentDay; }

    /**
     * Move to next day:
     * - One-time blackouts for the previous day are removed (they expire).
     * - Weekly blackouts persist.
     * - Current assignment (if any) is moved to history, then cleared.
     * - If wrap to MONDAY, also reset workloads (weekly reset).
     */
    public void nextDay() {
        try {
            java.time.DayOfWeek d = java.time.DayOfWeek.valueOf(store.currentDay);
            java.time.DayOfWeek prev = d;
            d = d.plus(1);
            store.currentDay = d.toString();

            // move current -> history
            if (store.currentAssignment != null) {
                store.history.add(store.currentAssignment);
                store.currentAssignment = null;
            }

            // expire ONE_TIME blackouts for the previous day (prev)
            String prevStr = prev.toString();
            List<BlackoutRequest> keep = new ArrayList<>();
            for (BlackoutRequest r : store.blackoutRequests) {
                if (r.type == BlackoutType.WEEKLY) keep.add(r);
                else if (r.type == BlackoutType.ONE_TIME) {
                    if (!r.day.equals(prevStr)) keep.add(r);
                }
            }
            store.blackoutRequests = keep;

            // when back to Monday, reset workloads
            if (d == java.time.DayOfWeek.MONDAY) {
                for (Worker w : store.workers) w.workloadScore = 0;
            }
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
