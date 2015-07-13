package controllers;

import auth.MyLoginUsernamePasswordAuthUser;
import auth.MyUsernamePasswordAuthProvider;
import dto.EmailIdentity;
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
import views.html.account.signup.*;

import javax.inject.Inject;
import java.util.Optional;

import static play.data.Form.form;

public class Signup extends Controller {

	private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);
	private static final Form<EmailIdentity> FORGOT_PASSWORD_FORM = form(EmailIdentity.class);

	private final Application application;

	@Inject
	public Signup(Application application) {
		this.application = application;
	}

	public Result unverified() {
		Authenticate.noCache(response());
		return ok(unverified.render());
	}

	public Result forgotPassword(final String email) {
		Authenticate.noCache(response());
		Form<EmailIdentity> form = FORGOT_PASSWORD_FORM;
		if (email != null && !email.trim().isEmpty()) {
			form = FORGOT_PASSWORD_FORM.fill(new EmailIdentity(email));
		}
		return ok(password_forgot.render(form));
	}

	public Result doForgotPassword() {
		Authenticate.noCache(response());
		final Form<EmailIdentity> filledForm = FORGOT_PASSWORD_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill in his/her email
			return badRequest(password_forgot.render(filledForm));
		} else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final String email = filledForm.get().email;

			// We don't want to expose whether a given email address is signed
			// up, so just say an email has been sent, even though it might not
			// be true - that's protecting our user privacy.
			flash(
					Application.FLASH_MESSAGE_KEY,
					Messages.get("playauthenticate.reset_password.message.instructions_sent", email)
			);

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
	private TokenAction tokenIsValid(final String token, final TokenType tokenType) {
		if (token != null && !token.trim().isEmpty()) {
			final Optional<TokenAction> ta = TokenAction.findByToken(token, tokenType);
			if (ta.isPresent() && ta.get().isValid()) {
				return ta.get();
			}
		}
		return null;
	}

	public Result resetPassword(final String token) {
		Authenticate.noCache(response());
		TokenAction ta = tokenIsValid(token, TokenType.PASSWORD_RESET);
		if (ta == null) {
			return badRequest(no_token_or_invalid.render());
		}
		return ok(password_reset.render(PASSWORD_RESET_FORM.fill(new PasswordReset(token))));
	}

	public Result doResetPassword() {
		Authenticate.noCache(response());
		final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(password_reset.render(filledForm));
		} else {
			final String token = filledForm.get().token;
			final String newPassword = filledForm.get().password;

			final TokenAction ta = tokenIsValid(token, TokenType.PASSWORD_RESET);
			if (ta == null) {
				return badRequest(no_token_or_invalid.render());
			}

			final Optional<User> u = User.findById(ta.user);
			try {

				// Pass true for the second parameter if you want to
				// automatically create a password and the exception never to
				// happen
				u.get().resetPassword(new MyUsernamePasswordAuthUser(newPassword), false);
			} catch (final RuntimeException re) {
				flash(
						Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.no_password_account")
				);
			}

			final boolean login = MyUsernamePasswordAuthProvider.getProvider().isLoginAfterPasswordReset();
			if (login) {

				// automatically log in
				flash(
						Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.auto_login")
				);
				return PlayAuthenticate.loginAndRedirect(ctx(), new MyLoginUsernamePasswordAuthUser(u.get().email));
			} else {
				// send the user to the login page
				flash(
						Application.FLASH_MESSAGE_KEY,
						Messages.get("playauthenticate.reset_password.message.success.manual_login")
				);
			}
			return redirect(routes.Application.login());
		}
	}

	public Result oAuthDenied(final String getProviderKey) {
		Authenticate.noCache(response());
		return ok(oAuthDenied.render(getProviderKey));
	}

	public Result exists() {
		Authenticate.noCache(response());
		return ok(exists.render());
	}

	public Result verify(final String token) {
		Authenticate.noCache(response());
		final TokenAction ta = tokenIsValid(token, TokenType.EMAIL_VERIFICATION);
		if (ta == null) {
			return badRequest(no_token_or_invalid.render());
		}
		final Optional<User> user = User.findById(ta.user);
		if (!user.isPresent()) {
			play.Logger.error("Couldn't find matching user for valid token \"" + token + "\" and user \"" + ta.user + "\"");
			return internalServerError();
		}
		User.verify(user.get());
		TokenAction.deleteByUserAndType(user.get(), TokenAction.TokenType.EMAIL_VERIFICATION);
		flash(Application.FLASH_MESSAGE_KEY, Messages.get("playauthenticate.verify_email.success", user.get().email));
		if (application.getLocalUser(session()) != null) {
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
