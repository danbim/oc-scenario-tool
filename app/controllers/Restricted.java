package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.restricted;

import java.util.Optional;

@Security.Authenticated(Secured.class)
public class Restricted extends Controller {

	public static Result index() {
		final Optional<User> localUser = Application.getLocalUser(session());
		return ok(restricted.render(localUser.get()));
	}
}