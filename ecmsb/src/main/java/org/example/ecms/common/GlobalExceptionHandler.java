package org.example.ecms.common;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理：统一把异常转成 Result JSON，避免向前端泄露堆栈。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：用其携带的 code/message */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException e) {
        log.warn("业务异常 code={} msg={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode() < 600 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    /** JWT 异常：token 失效或非法 */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Result<Void>> handleJwt(JwtException e) {
        log.warn("JWT 校验失败 {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401, "token 已失效，请重新登录"));
    }

    /** 接口不存在 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoHandlerFoundException e) {
        log.warn("接口不存在 {}", e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(404, "接口不存在"));
    }

    /** 请求体 JSON 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败 {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, "请求体格式错误"));
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("未捕获异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "服务异常，请稍后再试"));
    }
}
