package com.swb.ui;

import java.awt.*;
import java.awt.event.*;
import com.swb.core.DataManager;
import com.swb.model.BlackoutRequest;
import com.swb.model.BlackoutType;

public class WorkerDashboard extends Frame {
    DataManager dm; String user; Panel card; Label header; private String currentDay;

    public WorkerDashboard(String user, DataManager dm) {
        super("SWB - Worker: " + user);
        this.user = user; this.dm = dm; this.currentDay = dm.getCurrentDay();
        setSize(800, 520); setLayout(new BorderLayout());

        // header
        Panel top = new Panel(new BorderLayout());
        top.setBackground(new Color(245,245,245)); top.setPreferredSize(new Dimension(0,40));
        header = new Label("User: " + user);
        Button logout = new Button("Logout");
        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        top.add(header, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // sidebar
        Panel sidebar = new Panel(new GridLayout(0,1));
        sidebar.setBackground(new Color(230,230,230)); sidebar.setPreferredSize(new Dimension(200,0));
        Button bToday = new Button("Today's Tasks");
        Button bHist = new Button("My History");
        Button bBlack = new Button("Apply Blackout");
        sidebar.add(bToday); sidebar.add(bHist); sidebar.add(bBlack);
        add(sidebar, BorderLayout.WEST);

        // cards
        card = new Panel(new CardLayout());
        card.add(buildTodayPanel(), "today");
        card.add(buildHistPanel(), "hist");
        card.add(buildBlackPanel(), "black");
        add(card, BorderLayout.CENTER);

        bToday.addActionListener(e -> show("today"));
        bHist.addActionListener(e -> show("hist"));
        bBlack.addActionListener(e -> show("black"));

        addWindowListener(new WindowAdapter(){ public void windowClosing(WindowEvent e){ System.exit(0);} });
    }

    void show(String s) { ((CardLayout)card.getLayout()).show(card, s); }

    Panel buildTodayPanel() {
        Panel p = new Panel(new BorderLayout());
        java.awt.List l = new java.awt.List();
        p.add(l, BorderLayout.CENTER);
        Button r = new Button("Refresh");
        p.add(r, BorderLayout.SOUTH);
        r.addActionListener(e -> {
            l.removeAll();
            String today = dm.getCurrentDay();
            // iterate assignments (history+current)
            for (com.swb.model.AssignmentRecord o : dm.getAssignments()) {
                if (o.day.equals(today)) {
                    for (java.util.Map.Entry<String,String> en : o.assignments.entrySet()) {
                        if (en.getValue().equals(user)) l.add(en.getKey());
                    }
                }
            }
        });
        return p;
    }

    Panel buildHistPanel() {
        Panel p = new Panel(new BorderLayout());
        java.awt.List l = new java.awt.List();
        p.add(l, BorderLayout.CENTER);
        Button r = new Button("Refresh");
        p.add(r, BorderLayout.SOUTH);
        r.addActionListener(e -> {
            l.removeAll();
            for (com.swb.model.AssignmentRecord o : dm.getHistory()) {
                for (java.util.Map.Entry<String,String> en : o.assignments.entrySet()) {
                    if (en.getValue().equals(user)) l.add(o.day + " : " + en.getKey());
                }
            }
        });
        return p;
    }

    Panel buildBlackPanel() {
        Panel p = new Panel(new BorderLayout());
        Panel top = new Panel(new FlowLayout());
        Choice type = new Choice(); type.add("ONE_TIME"); type.add("WEEKLY");
        Choice dayc = new Choice();
        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) dayc.add(d.toString());
        Button apply = new Button("Apply");
        top.add(new Label("Type:")); top.add(type);
        top.add(new Label("Day:")); top.add(dayc);
        top.add(apply);
        p.add(top, BorderLayout.NORTH);
        java.awt.List l = new java.awt.List();
        p.add(l, BorderLayout.CENTER);

        apply.addActionListener(e -> {
            try {
                BlackoutRequest r = new BlackoutRequest(user, BlackoutType.valueOf(type.getSelectedItem()), dayc.getSelectedItem());
                dm.addBlackoutRequest(r);
                l.add("Requested: " + r.toString());
            } catch (Exception ex) { ex.printStackTrace(); l.add("Error: " + ex.getMessage()); }
        });
        return p;
    }
}
