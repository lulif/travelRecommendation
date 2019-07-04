package com.gdxx.exception;

import com.gdxx.base.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * 全局异常处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ApiResponse errorHandler(Exception e) throws Exception {
        return ApiResponse.ofStatus(ApiResponse.Status.HAPPEN_ERROR);
    }

}
