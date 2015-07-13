package dto;

import play.data.validation.Constraints;
import play.i18n.Messages;

public class EmailPasswordSignUp extends EmailPasswordLogin {

	@Constraints.Required
	@Constraints.MinLength(5)
	public String repeatPassword;

	@Constraints.Required
	public String name;

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String validate() {
		if (password == null || !password.equals(repeatPassword)) {
			return Messages.get("playauthenticate.password.signup.error.passwords_not_same");
		}
		return null;
	}
}
