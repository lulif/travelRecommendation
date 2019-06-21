package com.gdxx.base;
/*
 * 自定义异常处理类
 */
public class ExceptionResolver extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExceptionResolver(String msg) {
		super(msg);
	}

}
