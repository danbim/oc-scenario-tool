package controllers;

import auth.MyUsernamePasswordAuthProvider;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.profile;
import views.html.signup;

import java.util.Optional;

public class Application extends Controller {

    public static final String FLASH_MESSAGE_KEY = "message";

    public static final String FLASH_ERROR_KEY = "error";

	public static final String USER_ROLE = "user";

    public static Result index() {
        return ok(index.render());
    }

	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() {
		final User localUser = getLocalUser(session()).get();
		return ok(profile.render(localUser));
	}

	public static Result login() {
		return ok(login.render());
	}

	public static Result logout() {
		return Authenticate.logout();
	}

	public static Result signup() {
		return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
	}

	public static Result authenticate(String provider) {
		return Authenticate.authenticate(provider);
	}

	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyUsernamePasswordAuthProvider.MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(filledForm));
		} else {
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return UsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}

    public static Result oAuthDenied(final String providerKey) {
        Authenticate.noCache(response());
        flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection in order to use this website!");
        return redirect(controllers.routes.Application.index());
    }

    public static Optional<User> getLocalUser(final Http.Session session) {
        return User.findByAuthUserIdentity(PlayAuthenticate.getUser(session));
    }

}
