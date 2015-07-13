package dto;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import play.data.validation.Constraints;

public class EmailPasswordLogin extends EmailIdentity implements UsernamePasswordAuthProvider.UsernamePassword {

	@Constraints.Required
	@Constraints.MinLength(5)
	public String password;

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
