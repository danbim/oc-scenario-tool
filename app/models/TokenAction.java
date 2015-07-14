package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import play.data.format.Formats;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.Duration;
import java.time.LocalDateTime;

import static com.avaje.ebean.Expr.eq;

@Entity
public class TokenAction extends Model {

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 */
	private final static Duration VERIFICATION_DURATION = Duration.ofDays(7);

	public static Find<String, TokenAction> find = new Find<String, TokenAction>() {
	};

	@Id
	public String token;

	@ManyToOne
	public User user;

	public TokenType tokenType;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime created;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime expires;

	public static void deleteByUserAndType(final User u, final TokenType tokenType) {
		Ebean.execute(() -> {
			TokenAction.find.where()
					.and(eq("user.email", u.email), eq("tokenType", tokenType))
					.findList()
					.forEach(Model::delete);
		});
	}

	public static TokenAction create(final TokenType tokenType, final String token, final User user) {
		final LocalDateTime created = LocalDateTime.now();
		final TokenAction ua = new TokenAction();
		ua.user = user;
		ua.token = token;
		ua.tokenType = tokenType;
		ua.created = created;
		ua.expires = created.plus(VERIFICATION_DURATION);
		ua.save();
		return ua;
	}

	public boolean isValid() {
		return LocalDateTime.now().isBefore(expires);
	}

	public enum TokenType {
		EMAIL_VERIFICATION,
		PASSWORD_RESET
	}
}