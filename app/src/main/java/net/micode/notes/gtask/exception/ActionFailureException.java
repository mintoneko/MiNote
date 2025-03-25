package net.micode.notes.gtask.exception;

/**
 * 表示在任务操作过程中发生不可恢复错误时抛出的运行时异常。
 * <p>
 * 当与任务相关的操作（如创建、更新或同步任务）由于意外情况无法完成时，
 * 应抛出此异常。异常实例应包含具体的错误描述信息。
 *
 * @see RuntimeException
 */
public class ActionFailureException extends RuntimeException {
  private static final long serialVersionUID = 4425249765923293627L;

  /**
   * 构造一个不带详细信息和原因的新异常
   *
   * @see RuntimeException#RuntimeException()
   */
  public ActionFailureException() {
    super();
  }

  /**
   * 构造带有详细信息的新异常
   *
   * @param paramString 异常详细信息
   * @see RuntimeException#RuntimeException(String)
   */
  public ActionFailureException(String paramString) {
    super(paramString);
  }

  /**
   * 构造带有详细信息和根本原因的新异常
   *
   * @param paramString    异常详细信息
   * @param paramThrowable 异常根本原因
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public ActionFailureException(String paramString, Throwable paramThrowable) {
    super(paramString, paramThrowable);
  }
}