package com.gdxx.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/*
 * Json包装类
 */
@Setter
@Getter
public class ApiResponse {
	private int code;
	private String message;
	private Object data;

	public ApiResponse(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public ApiResponse() {
		this.code = Status.SUCCESS.getCode();
		this.message = Status.SUCCESS.getStandardMessage();
	}

	public static ApiResponse ofMessage(int code, String message) {
		return new ApiResponse(code, message, null);
	}

	public static ApiResponse ofSuccess(Object data) {
		return new ApiResponse(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
	}

	public static ApiResponse ofStatus(Status status) {
		return new ApiResponse(status.getCode(), status.getStandardMessage(), null);
	}

	@Getter
	@AllArgsConstructor
	public enum Status {
		FAIL(100, "Fail"), SUCCESS(200, "OK"),
		BAD_REQUEST(400, "Bad Request"), NOT_FOUND(404, "Not Found"),
		INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"), NOT_VALID_PARAM(40005, "Not valid Params"),
		NOT_SUPPORTED_OPERATION(40006, "Operation not supported"), FIXED_POSITION_FAIL(40007, "fixed position fail"),
		NOT_MATCHED_LOCATION(40008, "not matched location"), NOT_TRAVEL_STATUS(40009, "not_travel_status"),
		HAPPEN_ERROR(40010,"happen error");

		private int code;
		private String standardMessage;
	}
}
