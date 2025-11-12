package com.swb.exceptions;
public class NoWorkersException extends RuntimeException {
    public NoWorkersException() { super("No workers available"); }
}
