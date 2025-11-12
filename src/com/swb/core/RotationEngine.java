package com.swb.core;

import com.swb.model.*;
import com.swb.exceptions.*;
import java.util.*;

public class RotationEngine {
    private final DataManager dm;

    public RotationEngine(DataManager dm) { this.dm = dm; }

    /**
     * Assign tasks for the given weekday string (MONDAY..SUNDAY).
     * Ensures tasks are not skipped: if everybody is off, assigns anyway.
     */
    public AssignmentRecord assignForDay(String day) {
        List<Worker> workers = dm.getWorkers();
        if (workers.isEmpty()) throw new NoWorkersException();

        List<Task> tasks = dm.getTasks();
        // build a set of blocked days from approved blackouts
        Set<String> blockedWorkers = new HashSet<>();
        List<BlackoutRequest> brs = dm.getBlackoutRequests();
        for (BlackoutRequest r : brs) {
            if (r.status != RequestStatus.APPROVED) continue;
            if (r.type == BlackoutType.WEEKLY) {
                // mark worker as blocked for weekly if requested day equals day
                if (r.day.equals(day)) blockedWorkers.add(r.worker);
            } else {
                // ONE_TIME - only if r.day == day
                if (r.day.equals(day)) blockedWorkers.add(r.worker);
            }
        }

        AssignmentRecord rec = new AssignmentRecord(day);

        for (Task t : tasks) {
            // skip weekly tasks on other days
            if (t.type == TaskType.WEEKLY && (t.scheduledDay == null || !t.scheduledDay.equals(day))) continue;

            // prepare eligible list
            List<Worker> eligible = new ArrayList<>();
            for (Worker w : workers) {
                // also consider worker.blackoutDays (persisted weekly blackouts)
                boolean personallyOff = w.blackoutDays.contains(day) || blockedWorkers.contains(w.username);
                if (!personallyOff) eligible.add(w);
            }
            if (eligible.isEmpty()) {
                // if nobody eligible, fall back to all workers (force assignment)
                eligible.addAll(workers);
            }

            // sort by workload ascending
            eligible.sort(Comparator.comparingInt(a -> a.workloadScore));

            // pick first who didn't do this task last time if possible
            Worker chosen = null;
            for (Worker w : eligible) {
                if (t.lastAssignedTo == null || !t.lastAssignedTo.equals(w.username)) {
                    chosen = w; break;
                }
            }
            if (chosen == null) chosen = eligible.get(0);

            // assign
            chosen.workloadScore += t.difficulty;
            t.lastAssignedTo = chosen.username;
            rec.put(t.name, chosen.username);
        }

        // set as today's current assignment and persist
        dm.setCurrentAssignment(rec);
        dm.save();
        return rec;
    }
}
