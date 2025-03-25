package net.micode.notes.gtask.exception;

/**
 * 表示在任务同步过程中发生网络通信失败时抛出的受检异常。
 * <p>
 * 当与服务器进行网络交互（如身份验证、数据同步等操作）出现连接超时、
 * 服务器无响应或HTTP状态码异常等情况时，应抛出此异常。
 *
 * @see
 */
public class NetworkFailureException extends Exception {
  private static final long serialVersionUID = 2107610287180234136L;

  /**
   * 构造一个不带详细信息和原因的新网络异常
   *
   * @see Exception#Exception()
   */
  public NetworkFailureException() {
    super();
  }

  /**
   * 构造带有详细信息的网络异常
   *
   * @param paramString 异常描述信息
   * @see Exception#Exception(String)
   */
  public NetworkFailureException(String paramString) {
    super(paramString);
  }

  /**
   * 构造包含详细信息和根本原因的网络异常
   *
   * @param paramString    异常描述信息
   * @param paramThrowable 异常触发原因
   * @see Exception#Exception(String, Throwable)
   */
  public NetworkFailureException(String paramString, Throwable paramThrowable) {
    super(paramString, paramThrowable);
  }
}
