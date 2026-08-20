package org.example.ecms.common;

/**
 * 业务异常基类。
 * 抛出后由 GlobalExceptionHandler 捕获，转成 Result.error(code, message) 返回前端。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
