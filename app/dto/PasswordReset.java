package dto;

public class PasswordReset extends PasswordChange {

	public String token;

	public PasswordReset() {
	}

	public PasswordReset(final String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
