package auth;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;
import dto.EmailPasswordSignUp;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final String name;

	public MyUsernamePasswordAuthUser(final EmailPasswordSignUp signup) {
		super(signup.password, signup.email);
		this.name = signup.name;
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 *
	 * @param password the password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		name = null;
	}

	@Override
	public String getName() {
		return name;
	}
}