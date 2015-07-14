package models;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.user.AuthUser;

import javax.persistence.*;
import java.util.Locale;
import java.util.Optional;

import static com.avaje.ebean.Expr.eq;

@Entity
public class UserLinkedAccount extends Model {

	public static Find<String, UserLinkedAccount> find = new Find<String, UserLinkedAccount>() {
	};

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@ManyToOne
	public User user;

	public String providerKey;

	public String providerUserId;

	public String email;

	public String name;

	public String firstName;

	public String lastName;

	public String picture;

	public String gender;

	public Locale locale;

	public String profile;

	public static UserLinkedAccount create(final AuthUser authUser) {
		final UserLinkedAccount ret = new UserLinkedAccount();
		ret.providerKey = authUser.getProvider();
		ret.providerUserId = authUser.getId();
		return ret;
	}

	public static UserLinkedAccount create(final UserLinkedAccount acc) {
		final UserLinkedAccount ret = new UserLinkedAccount();
		ret.user = acc.user;
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;
		return ret;
	}

	public static Optional<UserLinkedAccount> findByProviderKey(final User user, String key) {
		return Optional.ofNullable(
				UserLinkedAccount.find.where().and(
						eq("user", user), eq("providerKey", key)
				).findUnique()
		);
	}
}