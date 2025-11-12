package com.swb.ui;

import java.awt.*;
import java.awt.event.*;
import com.swb.core.DataManager;

public class LoginFrame extends Frame implements ActionListener {
    TextField user = new TextField(15), pass = new TextField(15);
    Choice role = new Choice();
    Label status = new Label(" ");
    DataManager dm;

    public LoginFrame() {
        super("SWB - Login");
        setSize(480, 220);
        setLayout(new FlowLayout());
        add(new Label("Role:")); role.add("Supervisor"); role.add("Worker"); add(role);
        add(new Label("Username:")); add(user);
        add(new Label("Password:")); pass.setEchoChar('*'); add(pass);
        Button b = new Button("Login"); add(b); add(status);
        b.addActionListener(this);
        addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { System.exit(0); }});
        // data file stored relative to project root
        dm = new DataManager("data/db.ser");
    }

    public void actionPerformed(ActionEvent e) {
        String r = role.getSelectedItem(), u = user.getText().trim(), p = pass.getText().trim();
        try {
            if (r.equals("Supervisor")) {
                if (dm.authenticateSupervisor(u, p)) {
                    SupervisorDashboard sd = new SupervisorDashboard(u, dm);
                    sd.setVisible(true);
                    setVisible(false);
                    return;
                }
            } else {
                if (dm.authenticateWorker(u, p)) {
                    WorkerDashboard wd = new WorkerDashboard(u, dm);
                    wd.setVisible(true);
                    setVisible(false);
                    return;
                }
            }
            status.setText("Invalid credentials");
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
