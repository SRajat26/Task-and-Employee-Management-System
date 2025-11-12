package com.swb.exceptions;
public class NoEligibleWorkerException extends RuntimeException {
    public NoEligibleWorkerException(String task) { super("No eligible worker for task: " + task); }
}
