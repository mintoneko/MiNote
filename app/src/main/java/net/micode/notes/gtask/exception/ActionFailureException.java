package net.micode.notes.gtask.exception;

public class ActionFailureException extends RuntimeException {
    private static final long serialVersionUID = 4425249765923293627L;

    public ActionFailureException() {
        super();
    }

    public ActionFailureException(String paramString) {
        super(paramString);
    }

    public ActionFailureException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
    }
}
