package net.micode.notes.gtask.exception;

/**
 * 表示GTask相关操作执行失败时抛出的运行时异常
 * <p>
 * 当GTask操作（如网络请求、数据同步等）无法成功完成时抛出，
 * 调用方应捕获并处理该异常
 */
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
