package auth;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import models.User;
import play.Application;

import javax.inject.Inject;

public class OrganiCityAuthenticationHandler extends UserServicePlugin {

	@Inject
	public OrganiCityAuthenticationHandler(final Application app) {
		super(app);
	}

	@Override
	public Object save(final AuthUser authUser) {
		User user = User.find.byId(authUser.getId());
		if (user == null) {
			return User.create(authUser).email;
		} else {
			return null;
		}
	}

	@Override
	public Object getLocalIdentity(final AuthUserIdentity identity) {
		// For production: Caching might be a good idea here...
		// ...and don't forget to sync the cache when users get deactivated/deleted
		final User u = User.find.byId(identity.getId());
		if (u != null) {
			return u.email;
		} else {
			return null;
		}
	}

	@Override
	public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
		if (!oldUser.equals(newUser)) {
			User.merge(oldUser, newUser);
		}
		return oldUser;
	}

	@Override
	public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
		User.addLinkedAccount(oldUser, newUser);
		return null;
	}

}