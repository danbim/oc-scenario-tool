package controllers;

import auth.MyLoginUsernamePasswordAuthUser;
import auth.MyUsernamePasswordAuthProvider;
import auth.MyUsernamePasswordAuthProvider.MyIdentity;
import auth.MyUsernamePasswordAuthUser;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.Authenticate;
import models.TokenAction;
import models.TokenAction.TokenType;
import models.User;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.signup.exists;

import java.util.Optional;

import static play.data.Form.form;

public class Signup extends Controller {

	private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);
	private static final Form<MyIdentity> FORGOT_PASSWORD_FORM = form(MyIdentity.class);

	public static Result unverified() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(views.html.account.signup.unverified.render());
	}

	public static Result forgotPassword(final String email) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		Form<MyIdentity> form = FORGOT_PASSWORD_FORM;
		if (email != null && !email.trim().isEmpty()) {
			form = FORGOT_PASSWORD_FORM.fill(new MyIdentity(email));
		}
		return ok(views.html.account.signup.password_forgot.render(form));
	}

	public static Result doForgotPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyIdentity> filledForm = FORGOT_PASSWORD_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill in his/her email
			return badRequest(views.html.account.signup.password_forgot.render(filledForm));
		} else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final String email = filledForm.get().email;

			// We don't want to expose whether a given email address is signed
			// up, so just say an email has been sent, even though it might not
			// be true - that's protecting our user privacy.
			flash(Application.FLASH_MESSAGE_KEY,
					Messages.get(
							"playauthenticate.reset_password.message.instructions_sent",
							email));

			final Optional<User> user = User.findByEmail(email);
			if (user.isPresent()) {
				// yep, we have a user with this email that is active - we do
				// not know if the user owning that account has requested this
				// reset, though.
				final MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
				// User exists
				if (user.get().emailValidated) {
					provider.sendPasswordResetMailing(user.get(), ctx());
					// In case you actually want to let (the unknown person)
					// know whether a user was found/an email was sent, use,
					// change the flash message
				} else {
					// We need to change the message here, otherwise the user
					// does not understand whats going on - we should not verify
					// with the password reset, as a "bad" user could then sign
					// up with a fake email via OAuth and get it verified by an
					// a unsuspecting user that clicks the link.
					flash(
							Application.FLASH_MESSAGE_KEY,
							Messages.get("playauthenticate.reset_password.message.email_not_verified")
					);

					// You might want to re-send the verification email here...
					provider.sendVerifyEmailMailingAfterSignup(user.get(), ctx());
				}
			}

			return redirect(routes.Application.index());
		}
	}

	/**
	 * Returns a token object if valid, {@code null} otherwise.
	 *
	 * @param token
	 * @param tokenType
	 * @return
	 */
	private static TokenAction tokenIsValid(final String token, final TokenType tokenType) {
		if (token != null && !token.trim().isEmpty()) {
			final Optional<TokenAction> ta = TokenAction.findByToken(token, tokenType);
			if (ta.isPresent() && ta.get().isValid()) {
				return ta.get();
			}
		}
		return null;
	}

	public static Result resetPassword(final String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, TokenType.PASSWORD_RESET);
		if (ta == null) {
			return badRequest(views.html.account.signup.no_token_or_invalid.render());
		}

		return ok(views.html.account.signup.password_reset.render(PASSWORD_RESET_FORM.fill(new PasswordReset(token))));
	}

	public static Result doResetPassword() {
		Authenticate.noCache(response());
		final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.account.signup.password_reset.render(filledForm));
		} else {
			final String token = filledForm.get().token;
			final String newPassword = filledForm.get().password;

			final TokenAction ta = tokenIsValid(token, TokenType.PASSWORD_RESET);
			if (ta == null) {
				return badRequest(views.html.account.signup.no_token_or_invalid.render());
			}
			final Optional<User> u = User.findById(ta.user);
			try {
				// Pass true for the second parameter if you want to
				// automatically create a password and the exception never to
				// happen
				u.get().resetPassword(new MyUsernamePasswordAuthUser(newPassword), false);
			} catch (final RuntimeException re) {
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.no_password_account"));
			}
			final boolean login = MyUsernamePasswordAuthProvider.getProvider()
					.isLoginAfterPasswordReset();
			if (login) {
				// automatically log in
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.auto_login"));

				return PlayAuthenticate.loginAndRedirect(ctx(), new MyLoginUsernamePasswordAuthUser(u.get().email));
			} else {
				// send the user to the login page
				flash(Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.manual_login"));
			}
			return redirect(routes.Application.login());
		}
	}

	public static Result oAuthDenied(final String getProviderKey) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(views.html.account.signup.oAuthDenied.render(getProviderKey));
	}

	public static Result exists() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		return ok(exists.render());
	}

	public static Result verify(final String token) {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, TokenType.EMAIL_VERIFICATION);
		if (ta == null) {
			return badRequest(views.html.account.signup.no_token_or_invalid.render());
		}
		final Optional<User> user = User.findById(ta.user);
		if (!user.isPresent()) {
			play.Logger.error("Couldn't find matching user for valid token \"" + token + "\" and user \"" + ta.user + "\"");
			return internalServerError();
		}
		User.verify(user.get());
		TokenAction.deleteByUserAndType(user.get(), TokenAction.TokenType.EMAIL_VERIFICATION);
		flash(Application.FLASH_MESSAGE_KEY, Messages.get("playauthenticate.verify_email.success", user.get().email));
		if (Application.getLocalUser(session()) != null) {
			return redirect(routes.Application.index());
		} else {
			return redirect(routes.Application.login());
		}
	}

	public static class PasswordReset extends Account.PasswordChange {

		public String token;

		public PasswordReset() {
		}

		public PasswordReset(final String token) {
			this.token = token;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}
}
