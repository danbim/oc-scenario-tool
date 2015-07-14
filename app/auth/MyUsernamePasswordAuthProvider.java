package auth;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.google.inject.Inject;
import controllers.routes;
import dto.EmailPasswordLogin;
import dto.EmailPasswordSignUp;
import models.TokenAction;
import models.TokenAction.TokenType;
import models.User;
import models.UserLinkedAccount;
import play.Application;
import play.Logger;
import play.data.Form;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static play.data.Form.form;

public class MyUsernamePasswordAuthProvider extends UsernamePasswordAuthProvider<
		String,
		MyLoginUsernamePasswordAuthUser,
		MyUsernamePasswordAuthUser,
		EmailPasswordLogin,
		EmailPasswordSignUp> {

	public static final Form<EmailPasswordSignUp> SIGNUP_FORM = form(EmailPasswordSignUp.class);
	public static final Form<EmailPasswordLogin> LOGIN_FORM = form(EmailPasswordLogin.class);

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL + "." + "verificationLink.secure";
	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL + "." + "passwordResetLink.secure";
	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";
	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	@Inject
	public MyUsernamePasswordAuthProvider(Application app) {
		super(app);
	}

	public static MyUsernamePasswordAuthProvider getProvider() {
		return (MyUsernamePasswordAuthProvider) PlayAuthenticate.getProvider(UsernamePasswordAuthProvider.PROVIDER_KEY);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> needed = new ArrayList<>(super.neededSettingKeys());
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
		return needed;
	}

	protected Form<EmailPasswordSignUp> getSignupForm() {
		return SIGNUP_FORM;
	}

	protected Form<EmailPasswordLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	@Override
	protected UsernamePasswordAuthProvider.SignupResult signupUser(final MyUsernamePasswordAuthUser user) {
		final Optional<User> u = User.findByUsernamePasswordIdentity(user);
		if (u.isPresent()) {
			if (u.get().emailValidated) {
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
			}
		}
		// The user either does not exist or is inactive - create a new one
		User.create(user);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		return SignupResult.USER_CREATED_UNVERIFIED;
	}

	@Override
	protected UsernamePasswordAuthProvider.LoginResult loginUser(MyLoginUsernamePasswordAuthUser authUser) {
		final User u = User.find.byId(authUser.getEmail());
		if (u == null) {
			return LoginResult.NOT_FOUND;
		} else {
			if (!u.emailValidated) {
				return LoginResult.USER_UNVERIFIED;
			} else {
				for (final UserLinkedAccount acc : u.linkedAccounts) {
					if (getKey().equals(acc.providerKey)) {
						if (authUser.checkPassword(acc.providerUserId, authUser.getPassword())) {
							// Password was correct
							return LoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return LoginResult.WRONG_PASSWORD;
						}
					}
				}
				return LoginResult.WRONG_PASSWORD;
			}
		}
	}

	@Override
	protected Call userExists(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser(final EmailPasswordSignUp signup, final Context ctx) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser(final EmailPasswordLogin login, final Context ctx) {
		return new MyLoginUsernamePasswordAuthUser(login.getPassword(), login.getEmail());
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser transformAuthUser(final MyUsernamePasswordAuthUser authUser, final Context context) {
		return new MyLoginUsernamePasswordAuthUser(authUser.getEmail());
	}

	@Override
	protected String getVerifyEmailMailingSubject(final MyUsernamePasswordAuthUser user, final Context ctx) {
		return Messages.get("playauthenticate.password.verify_signup.subject");
	}

	@Override
	protected String onLoginUserNotFound(final Context context) {
		context.flash().put(
				controllers.Application.FLASH_ERROR_KEY,
				Messages.get("playauthenticate.password.login.unknown_user_or_pw")
		);
		return super.onLoginUserNotFound(context);
	}

	@Override
	protected Body getVerifyEmailMailingBody(final String token,
											 final MyUsernamePasswordAuthUser user, final Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_VERIFICATION_LINK_SECURE);
		String url = routes.Signup.verify(token).absoluteURL(ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());

		String html = getEmailTemplate("views.html.account.signup.email.verify_email", lang.code(), url, token, user.getName(), user.getEmail());
		String text = getEmailTemplate("views.txt.account.signup.email.verify_email", lang.code(), url, token, user.getName(), user.getEmail());

		return new Body(text, html);
	}

	@Override
	protected String generateVerificationRecord(final MyUsernamePasswordAuthUser authUser) {
		return generateVerificationRecord(User.find.byId(authUser.getEmail()));
	}

	protected String generateVerificationRecord(final User user) {
		String token = generateToken();
		TokenAction.create(TokenType.EMAIL_VERIFICATION, token, user);
		return token;
	}

	protected String generatePasswordResetRecord(final User u) {
		String token = generateToken();
		TokenAction.create(TokenType.PASSWORD_RESET, token, u);
		return token;
	}

	protected Body getPasswordResetMailingBody(final String token, final User user, final Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		String url = routes.Signup.resetPassword(token).absoluteURL(ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());

		String html = getEmailTemplate("views.html.account.email.password_reset", lang.code(), url, token, user.name, user.email);
		String text = getEmailTemplate("views.txt.account.email.password_reset", lang.code(), url, token, user.name, user.email);

		return new Body(text, html);
	}

	public void sendPasswordResetMailing(final User user, final Context ctx) {
		final String token = generatePasswordResetRecord(user);
		final String subject = Messages.get("playauthenticate.password.reset_email.subject");
		final Body body = getPasswordResetMailingBody(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
	}

	protected String getEmailTemplate(final String template,
									  final String langCode, final String url, final String token,
									  final String name, final String email) {
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '"
					+ template
					+ "_"
					+ langCode
					+ "' was not found! Trying to use English fallback template instead.");
		}
		if (cls == null) {
			try {
				cls = Class.forName(template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
			} catch (ClassNotFoundException e) {
				Logger.error("Fallback template: '" + template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE
						+ "' was not found either!");
			}
		}
		if (cls != null) {
			Method htmlRender;
			try {
				htmlRender = cls.getMethod("render", String.class, String.class, String.class, String.class);
				ret = htmlRender.invoke(null, url, token, name, email).toString();
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	protected Body getVerifyEmailMailingBodyAfterSignup(final String token, final User user, final Context ctx) {

		boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_VERIFICATION_LINK_SECURE);
		String url = routes.Signup.verify(token).absoluteURL(ctx.request(), isSecure);

		Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		String langCode = lang.code();

		String html = getEmailTemplate("views.html.account.email.verify_email", langCode, url, token, user.name, user.email);
		String text = getEmailTemplate("views.txt.account.email.verify_email", langCode, url, token, user.name, user.email);

		return new Body(text, html);
	}

	public void sendVerifyEmailMailingAfterSignup(final User user, final Context ctx) {
		String subject = Messages.get("playauthenticate.password.verify_email.subject");
		String token = generateVerificationRecord(user);
		Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	private String getEmailName(final User user) {
		return getEmailName(user.email, user.name);
	}

}