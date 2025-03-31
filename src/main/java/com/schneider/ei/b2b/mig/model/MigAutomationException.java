package com.schneider.ei.b2b.mig.model;

public class MigAutomationException extends Exception {

    public MigAutomationException(String message) {
        super(message);
    }

    public MigAutomationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigAutomationException(Throwable cause) {
        super(cause);
    }

    public MigAutomationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
