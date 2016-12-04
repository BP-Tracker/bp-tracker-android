package com.bptracker.firmware;

/**
 * A generic firmware related exception class
 */
public class FirmwareException extends RuntimeException {

    public FirmwareException(Throwable cause) {
        super(cause);
    }

    public FirmwareException(String message, Throwable cause) {
        super(message, cause);
    }

    public FirmwareException(String message) {
        super(message);
    }
}
