package auth;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import models.User;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static play.libs.F.Promise;

public class OrganiCityAuthorizationHandler extends AbstractDeadboltHandler {

	@Override
	public F.Promise<Optional<Result>> beforeAuthCheck(play.mvc.Http.Context context) {
		return Promise.pure(Optional.empty());
	}

	@Override
	public F.Promise<Optional<Subject>> getSubject(Http.Context context) {
		AuthUser user = PlayAuthenticate.getUser(context.session());
		if (user == null) {
			return Promise.pure(Optional.empty());
		}
		Optional<User> localUser = User.findByAuthUserIdentity(user);
		if (!localUser.isPresent()) {
			return Promise.pure(Optional.empty());
		}
		return Promise.pure(Optional.of(new LocalSubject(localUser.get())));
	}

	public static class LocalRole implements Role {

		private final String role;

		public LocalRole(String role) {
			this.role = role;
		}

		@Override
		public String getName() {
			return role;
		}
	}

	public static class LocalSubject implements Subject {

		private final User user;

		public LocalSubject(User user) {
			this.user = user;
		}

		@Override
		public List<? extends Role> getRoles() {
			if (user.roles == null) {
				return new ArrayList<>();
			}
			return user.roles.stream()
					.map(LocalRole::new)
					.collect(Collectors.toList());
		}

		@Override
		public List<? extends Permission> getPermissions() {
			return new ArrayList<>();
		}

		@Override
		public String getIdentifier() {
			return user.id;
		}
	}
}
