package com.pjx.vser.common.exception;

public final class LifecycleException extends Exception {

    private String message = null;

    private Throwable throwable = null;

    public LifecycleException() {

    }

    public LifecycleException(String message) {

        this(message, null);

    }

    public LifecycleException(Throwable throwable) {

        this(null, throwable);

    }

    public LifecycleException(String message, Throwable throwable) {

        super();
        this.message = message;
        this.throwable = throwable;

    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("LifecycleException:  ");
        if (message != null) {
            sb.append(message);
            if (throwable != null) {
                sb.append(":  ");
            }
        }
        if (throwable != null) {
            sb.append(throwable);
        }
        return (sb.toString());

    }
}
