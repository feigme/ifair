package com.ifair.base;

/**
 * 业务结果
 *
 * Created by feiying on 16/11/15.
 */
public class BizResult<T> {

	private Boolean success;

	private String message;

	private String code;

	private T t;

	public BizResult(boolean isSuccess) {
		this.success = isSuccess;
	}

	public Boolean getSuccess() {
		return success;
	}

	public BizResult setSuccess(Boolean success) {
		this.success = success;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public BizResult setMessage(String message) {
		this.message = message;
		return this;
	}

	public T getData() {
		return t;
	}

	public BizResult setData(T t) {
		this.t = t;
		return this;
	}

	public String getCode() {
		return code;
	}

	public BizResult setCode(String code) {
		this.code = code;
		return this;
	}
}
