package com.ifair.web.index.user.command;

import com.ifair.base.BaseCommand;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by feiying on 17/1/18.
 */
public class RegisterCommand extends BaseCommand {

	@NotBlank(message = "手机号码为空!")
	private String mobile;
	@NotBlank(message = "密码为空!")
	@Length(min = 6, max = 20, message = "密码长度要在6~20")
	private String password;
	@NotBlank(message = "确认密码为空")
	@Length(min = 6, max = 20, message = "密码长度要在6~20")
	private String confirmPassword;
	private String userName;

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
