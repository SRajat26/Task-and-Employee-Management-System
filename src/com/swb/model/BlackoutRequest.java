package com.swb.model;

import java.io.Serializable;

public class BlackoutRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    public String worker;
    public BlackoutType type;
    public String day; // MONDAY.. string
    public RequestStatus status = RequestStatus.PENDING;

    public BlackoutRequest(String worker, BlackoutType type, String day) {
        this.worker = worker;
        this.type = type;
        this.day = day;
    }

    @Override
    public String toString() {
        return worker + " | " + type + " | " + day + " | " + status;
    }
}
