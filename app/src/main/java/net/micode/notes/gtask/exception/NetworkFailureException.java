package net.micode.notes.gtask.exception;

public class NetworkFailureException extends Exception {
    private static final long serialVersionUID = 2107610287180234136L;

    public NetworkFailureException() {
        super();
    }

    public NetworkFailureException(String paramString) {
        super(paramString);
    }

    public NetworkFailureException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
    }
}
