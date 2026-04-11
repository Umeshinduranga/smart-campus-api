package com.smartcampus.exception;

public class SensorUnavailableException
        extends RuntimeException {
    public SensorUnavailableException(String msg) {
        super(msg);
    }
}