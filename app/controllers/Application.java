package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

import java.util.Optional;

public class Application extends Controller {

    public static final String FLASH_MESSAGE_KEY = "message";

    public static final String FLASH_ERROR_KEY = "error";

    public static Result index() {
        return ok(index.render());
    }

    public static Result oAuthDenied(final String providerKey) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection in order to use this website!");
        return redirect(controllers.routes.Application.index());
    }

    public static Optional<User> getLocalUser(final Http.Session session) {
        return User.findByAuthUserIdentity(PlayAuthenticate.getUser(session));
    }

}
