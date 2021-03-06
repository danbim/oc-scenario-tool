package controllers;

import auth.MyUsernamePasswordAuthProvider;
import auth.MyUsernamePasswordAuthUser;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import com.feth.play.module.pa.user.AuthUser;
import dto.Accept;
import dto.PasswordChange;
import models.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.*;

import javax.inject.Inject;

import static play.data.Form.form;

public class Account extends Controller {

	private static final Form<Accept> ACCEPT_FORM = form(Accept.class);
	private static final Form<PasswordChange> PASSWORD_CHANGE_FORM = form(PasswordChange.class);

	private final Application application;

	@Inject
	public Account(Application application) {
		this.application = application;
	}

	@SubjectPresent
	public Result link() {
		Authenticate.noCache(response());
		return ok(link.render());
	}

	@Restrict(@Group(Application.USER_ROLE))
	public Result verifyEmail() {
		Authenticate.noCache(response());
		final User user = application.getLocalUser(session()).get();
		if (user.emailValidated) {
			// E-Mail has been validated already
			flash(
					Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.error.already_validated")
			);
		} else if (user.email != null && !user.email.trim().isEmpty()) {
			flash(
					Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.message.instructions_sent", user.email)
			);
			MyUsernamePasswordAuthProvider.getProvider().sendVerifyEmailMailingAfterSignup(user, ctx());
		} else {
			flash(
					Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.verify_email.error.set_email_first", user.email)
			);
		}
		return redirect(controllers.routes.Application.profile());
	}

	@Restrict(@Group(Application.USER_ROLE))
	public Result changePassword() {
		Authenticate.noCache(response());
		final User u = application.getLocalUser(session()).get();
		if (!u.emailValidated) {
			return ok(unverified.render());
		} else {
			return ok(password_change.render(PASSWORD_CHANGE_FORM));
		}
	}

	@Restrict(@Group(Application.USER_ROLE))
	public Result doChangePassword() {
		Authenticate.noCache(response());
		final Form<PasswordChange> filledForm = PASSWORD_CHANGE_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(password_change.render(filledForm));
		} else {
			final User user = application.getLocalUser(session()).get();
			final String newPassword = filledForm.get().password;
			user.changePassword(new MyUsernamePasswordAuthUser(newPassword),
					true);
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.change_password.success"));
			return redirect(controllers.routes.Application.profile());
		}
	}

	@SubjectPresent
	public Result askLink() {
		Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(controllers.routes.Application.index());
		}
		return ok(ask_link.render(ACCEPT_FORM, u));
	}

	@SubjectPresent
	public Result doLink() {
		Authenticate.noCache(response());
		final AuthUser u = PlayAuthenticate.getLinkUser(session());
		if (u == null) {
			// account to link could not be found, silently redirect to login
			return redirect(controllers.routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to link or not link
			return badRequest(ask_link.render(filledForm, u));
		} else {
			// User made a choice :)
			final boolean link = filledForm.get().accept;
			if (link) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.link.success"));
			}
			return PlayAuthenticate.link(ctx(), link);
		}
	}

	@SubjectPresent
	public Result askMerge() {
		Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(controllers.routes.Application.index());
		}

		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
	}

	@SubjectPresent
	public Result doMerge() {
		Authenticate.noCache(response());
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser(session());

		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser(session());
		if (bUser == null) {
			// user to merge with could not be found, silently redirect to login
			return redirect(controllers.routes.Application.index());
		}

		final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not select whether to merge or not merge
			return badRequest(ask_merge.render(filledForm, aUser, bUser));
		} else {
			// User made a choice :)
			final boolean merge = filledForm.get().accept;
			if (merge) {
				flash(
						Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.accounts.merge.success")
				);
			}
			return PlayAuthenticate.merge(ctx(), merge);
		}
	}

}
