package dto;

import play.data.validation.Constraints;

public class EmailIdentity {

	@Constraints.Required
	@Constraints.Email
	public String email;

	public EmailIdentity() {
	}

	public EmailIdentity(final String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
