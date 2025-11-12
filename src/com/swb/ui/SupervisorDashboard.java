package com.swb.ui;

import java.awt.*;
import java.awt.event.*;
import com.swb.core.DataManager;
import com.swb.core.RotationEngine;
import com.swb.model.*;
import java.util.List;

public class SupervisorDashboard extends Frame {
    DataManager dm;
    String user;
    Panel card;
    Label headerDay;
    Label headerUser;
    private String currentDay;

    public SupervisorDashboard(String user, DataManager dm) {
        super("SWB - Supervisor: " + user);
        this.user = user; this.dm = dm;
        this.currentDay = dm.getCurrentDay();
        setSize(1000, 650);
        setLayout(new BorderLayout());

        // Header bar
        Panel top = new Panel(new BorderLayout());
        top.setBackground(new Color(245,245,245)); top.setPreferredSize(new Dimension(0,40));
        Panel left = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        Panel right = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        headerUser = new Label("User: " + user);
        headerDay = new Label("Day: " + currentDay);
        Button logout = new Button("Logout");
        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        left.add(headerUser);
        right.add(headerDay);
        right.add(logout);
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Sidebar
        Panel sidebar = new Panel();
        sidebar.setBackground(new Color(230,230,230)); sidebar.setPreferredSize(new Dimension(220,0));
        sidebar.setLayout(new GridLayout(0,1,4,4));
        Button bWorkers = new Button("Workers");
        Button bTasks = new Button("Tasks");
        Button bAssign = new Button("Assignments");
        Button bBlack = new Button("Blackouts");
        Button bActive = new Button("Active Blackouts");
        Button bHistory = new Button("History");
        Button bStats = new Button("Stats");
        sidebar.add(bWorkers); sidebar.add(bTasks); sidebar.add(bAssign); sidebar.add(bBlack);
        sidebar.add(bActive); sidebar.add(bHistory); sidebar.add(bStats);
        add(sidebar, BorderLayout.WEST);

        // Main cards
        card = new Panel(new CardLayout());
        card.add(buildWorkersPanel(), "workers");
        card.add(buildTasksPanel(), "tasks");
        card.add(buildAssignPanel(), "assign");
        card.add(buildBlackoutPanel(), "blackout");
        card.add(buildActiveBlackoutsPanel(), "active");
        card.add(buildHistoryPanel(), "history");
        card.add(buildStatsPanel(), "stats");
        add(card, BorderLayout.CENTER);

        // nav
        bWorkers.addActionListener(a -> show("workers"));
        bTasks.addActionListener(a -> show("tasks"));
        bAssign.addActionListener(a -> show("assign"));
        bBlack.addActionListener(a -> show("blackout"));
        bActive.addActionListener(a -> show("active"));
        bHistory.addActionListener(a -> show("history"));
        bStats.addActionListener(a -> show("stats"));

        addWindowListener(new WindowAdapter(){ public void windowClosing(WindowEvent e){ System.exit(0);} });
    }

    void show(String name) { ((CardLayout)card.getLayout()).show(card, name); }

    // Workers panel (add/remove)
    Panel buildWorkersPanel() {
    Panel p = new Panel(new BorderLayout());
    Panel top = new Panel(new FlowLayout());
    TextField userf = new TextField(12);
    TextField passf = new TextField(12);
    Button add = new Button("Add Worker");
    Button remove = new Button("Remove Selected");
    top.add(new Label("Username:"));
    top.add(userf);
    top.add(new Label("Password:"));
    top.add(passf);
    top.add(add);
    top.add(remove);
    p.add(top, BorderLayout.NORTH);

    java.awt.List list = new java.awt.List();
    p.add(list, BorderLayout.CENTER);

    Runnable refresh = () -> {
        list.removeAll();
        for (Worker w : dm.getWorkers())
            list.add(w.username + " (workload=" + w.workloadScore + ")");
    };
    refresh.run();

    add.addActionListener(e -> {
        try {
            String u = userf.getText().trim();
            String pw = passf.getText().trim();
            if (u.isEmpty() || pw.isEmpty()) return;
            dm.addWorker(new Worker(u, pw));
            userf.setText("");
            passf.setText("");
            refresh.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    remove.addActionListener(e -> {
        try {
            String sel = list.getSelectedItem();
            if (sel == null) return;
            int idx = sel.indexOf(" (");
            String name = (idx > 0) ? sel.substring(0, idx).trim() : sel.trim();
            dm.removeWorker(name);
            refresh.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    return p;
}


    void refreshWorkers(java.awt.List list) {
        list.removeAll();
        for (Worker w : dm.getWorkers()) list.add(w.toString());
    }

    // Tasks panel (add/remove)
    Panel buildTasksPanel() {
    Panel p = new Panel(new BorderLayout());

    Panel form = new Panel(new GridLayout(0, 1));
    TextField tname = new TextField(20);
    Choice type = new Choice();
    type.add("DAILY");
    type.add("WEEKLY");
    type.add("ONE_TIME");
    Choice diff = new Choice();
    for (int i = 1; i <= 5; i++) diff.add("" + i);
    TextField sched = new TextField(10);
    Button add = new Button("Add Task");
    Button remove = new Button("Remove Selected");

    form.add(new Label("Task name:"));
    form.add(tname);
    form.add(new Label("Type:"));
    form.add(type);
    form.add(new Label("Difficulty:"));
    form.add(diff);
    form.add(new Label("ScheduledDay (for weekly):"));
    form.add(sched);
    form.add(add);
    form.add(remove);
    p.add(form, BorderLayout.NORTH);

    java.awt.List list = new java.awt.List();
    p.add(list, BorderLayout.CENTER);

    Runnable refresh = () -> {
        list.removeAll();
        for (Task t : dm.getTasks())
            list.add(t.name + " (d=" + t.difficulty + ")");
    };
    refresh.run();

    add.addActionListener(e -> {
        try {
            String n = tname.getText().trim();
            if (n.isEmpty()) return;
            int d = Integer.parseInt(diff.getSelectedItem());
            Task t = new Task(n, TaskType.valueOf(type.getSelectedItem()), d);
            if (!sched.getText().trim().isEmpty())
                t.scheduledDay = sched.getText().trim().toUpperCase();
            dm.addTask(t);
            tname.setText("");
            sched.setText("");
            refresh.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    remove.addActionListener(e -> {
        try {
            String sel = list.getSelectedItem();
            if (sel == null) return;
            int idx = sel.indexOf(" (d=");
            String name = (idx > 0) ? sel.substring(0, idx).trim() : sel.trim();
            dm.removeTask(name);
            refresh.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    return p;
}

    void refreshTasks(java.awt.List list) {
        list.removeAll();
        for (Task t : dm.getTasks()) list.add(t.toString());
    }

    // Assign panel (generate + next day)
    Panel buildAssignPanel() {
        Panel p = new Panel(new BorderLayout());
        Panel top = new Panel(new FlowLayout());
        Button gen = new Button("Generate Rotation");
        Button next = new Button("Next Day");
        top.add(gen); top.add(next);
        p.add(top, BorderLayout.NORTH);

        java.awt.List list = new java.awt.List();
        p.add(list, BorderLayout.CENTER);

        RotationEngine engine = new RotationEngine(dm);

        gen.addActionListener(e -> {
            try {
                list.removeAll();
                AssignmentRecord rec = engine.assignForDay(currentDay);
                list.add("Rotation for " + currentDay);
                for (java.util.Map.Entry<String,String> en : rec.assignments.entrySet())
                    list.add(en.getKey() + " -> " + en.getValue());
            } catch (Exception ex) { list.add("Error: " + ex.getMessage()); ex.printStackTrace(); }
        });

        next.addActionListener(e -> {
            try {
                // Move day forward in DataManager (it will move current->history and expire one-time blackouts)
                dm.nextDay();
                currentDay = dm.getCurrentDay();
                headerDay.setText("Day: " + currentDay);

                // auto-generate rotation for the new day
                list.removeAll();
                AssignmentRecord rec = engine.assignForDay(currentDay);
                list.add("Rotation for " + currentDay);
                for (java.util.Map.Entry<String,String> en : rec.assignments.entrySet())
                    list.add(en.getKey() + " -> " + en.getValue());
            } catch (Exception ex) { list.add("Error: " + ex.getMessage()); ex.printStackTrace(); }
        });

        return p;
    }

    // Blackout requests panel
    Panel buildBlackoutPanel() {
        Panel p = new Panel(new BorderLayout());
        java.awt.List list = new java.awt.List();
        p.add(list, BorderLayout.CENTER);
        Button refresh = new Button("Refresh");
        Button approve = new Button("Approve All");
        Panel top = new Panel(new FlowLayout());
        top.add(refresh); top.add(approve);
        p.add(top, BorderLayout.NORTH);

        refresh.addActionListener(e -> {
            list.removeAll();
            for (BlackoutRequest r : dm.getBlackoutRequests()) list.add(r.toString());
        });

        approve.addActionListener(e -> {
            try {
                for (BlackoutRequest r : dm.getBlackoutRequests()) {
                    r.status = RequestStatus.APPROVED;
                    if (r.type == BlackoutType.WEEKLY) {
                        // persist weekly blackout into worker record
                        for (Worker w : dm.getWorkers()) {
                            if (w.username.equals(r.worker) && !w.blackoutDays.contains(r.day)) w.blackoutDays.add(r.day);
                        }
                    }
                }
                dm.updateBlackoutRequests(dm.getBlackoutRequests());
                refresh.dispatchEvent(new ActionEvent(refresh, ActionEvent.ACTION_PERFORMED, null));
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        refresh.dispatchEvent(new ActionEvent(refresh, ActionEvent.ACTION_PERFORMED, null));
        return p;
    }

    // Active blackouts (approved ones) for supervisor
    Panel buildActiveBlackoutsPanel() {
        Panel p = new Panel(new BorderLayout());
        java.awt.List list = new java.awt.List();
        p.add(list, BorderLayout.CENTER);
        Button r = new Button("Refresh");
        p.add(r, BorderLayout.SOUTH);
        r.addActionListener(e -> {
            list.removeAll();
            String today = dm.getCurrentDay();
            for (BlackoutRequest br : dm.getBlackoutRequests()) {
                if (br.status == RequestStatus.APPROVED) {
                    if (br.type == BlackoutType.WEEKLY) list.add(br.worker + " - " + br.day + " (WEEKLY)");
                    else if (br.type == BlackoutType.ONE_TIME && br.day.equals(today)) list.add(br.worker + " - " + br.day + " (ONE_TIME)");
                }
            }
        });
        return p;
    }

    // History view (shows history only; current assignment is stored in DataManager)
    Panel buildHistoryPanel() {
        Panel p = new Panel(new BorderLayout());
        java.awt.List list = new java.awt.List();
        p.add(list, BorderLayout.CENTER);
        Button r = new Button("Refresh");
        p.add(r, BorderLayout.SOUTH);
        r.addActionListener(e -> {
            list.removeAll();
            for (AssignmentRecord a : dm.getHistory()) {
                list.add(a.day);
                for (java.util.Map.Entry<String,String> en : a.assignments.entrySet()) list.add("  " + en.getKey() + " -> " + en.getValue());
            }
        });
        return p;
    }

    // Stats
    Panel buildStatsPanel() {
        Panel p = new Panel(new BorderLayout());
        java.awt.List list = new java.awt.List();
        p.add(list, BorderLayout.CENTER);
        Button r = new Button("Refresh");
        p.add(r, BorderLayout.SOUTH);
        r.addActionListener(e -> {
            list.removeAll();
            for (Worker w : dm.getWorkers()) list.add(w.toString());
        });
        return p;
    }
}
