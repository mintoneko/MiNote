package net.micode.notes.gtask.exception;

/**
 * 表示GTask网络操作失败时抛出的受检异常
 * <p>
 * 当网络连接异常、请求超时或服务器返回错误状态码时抛出，
 * 调用方必须通过try-catch块或throws声明处理该异常
 */
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
