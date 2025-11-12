package com.swb.exceptions;
public class InvalidDifficultyException extends RuntimeException {
    public InvalidDifficultyException(int d){ super("Invalid difficulty: "+d); }
}
