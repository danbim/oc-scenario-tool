package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import dto.EmailPasswordLogin;
import dto.EmailPasswordSignUp;
import exceptions.EntityExistsException;
import models.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.profile;
import views.html.signup;

import java.util.Optional;

import static auth.MyUsernamePasswordAuthProvider.LOGIN_FORM;
import static auth.MyUsernamePasswordAuthProvider.SIGNUP_FORM;

public class Application extends Controller {

    public static final String FLASH_MESSAGE_KEY = "message";

    public static final String FLASH_ERROR_KEY = "error";

	public static final String USER_ROLE = "user";

    public Result index() {
        return ok(index.render());
    }

	@Restrict(@Group(Application.USER_ROLE))
	public Result profile() {
		final User localUser = getLocalUser(session()).get();
		return ok(profile.render(localUser));
	}

	public Result login() {
		return ok(login.render(LOGIN_FORM));
	}

	public Result doLogin() {
		Authenticate.noCache(response());
		Form<EmailPasswordLogin> form = LOGIN_FORM.bindFromRequest();
		if (form.hasErrors()) {
			return badRequest(login.render(form));
		}
		return UsernamePasswordAuthProvider.handleLogin(ctx());
	}

	public Result logout() {
		return Authenticate.logout();
	}

	public Result signup() {
		return ok(signup.render(SIGNUP_FORM));
	}

	public Result authenticate(String provider) {
		return Authenticate.authenticate(provider);
	}

	public Result doSignup() {
		Authenticate.noCache(response());
		Form<EmailPasswordSignUp> filledForm = SIGNUP_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(filledForm));
		} else {
			// Everything was filled, do something with your part of the form before handling the user signup
			try {
				return UsernamePasswordAuthProvider.handleSignup(ctx());
			} catch (EntityExistsException e) {
				// user already exists
				filledForm.reject(Messages.get("playauthenticate.user.exists.message"));
				return badRequest(signup.render(filledForm));
			}
		}
	}

    public Result oAuthDenied(final String providerKey) {
        Authenticate.noCache(response());
        flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection in order to use this website!");
        return redirect(controllers.routes.Application.index());
    }

    public Optional<User> getLocalUser(final Http.Session session) {
		final AuthUser user = PlayAuthenticate.getUser(session);
		if (user == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(User.find.byId(user.getId()));
    }

}
