package models;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.*;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.mindrot.jbcrypt.BCrypt;
import play.data.validation.Constraints;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Entity
public class User extends Model {

	public static Find<String, User> find = new Find<String, User>() {
	};

	@Id
	@Constraints.Email
	public String email;

	public String name;

	public String firstName;

	public String lastName;

	public String gender;

	public boolean emailValidated;

	public String hashedPassword;

	public Locale locale;

	public String picture;

	public String profileLink;

	public String auth_id;

	public String auth_provider;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public List<UserLinkedAccount> linkedAccounts = new ArrayList<>();

	public String roles;

	private static final Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

	public List<String> getRoleList() {
		return newArrayList(SPLITTER.split(roles));
	}

	public static Optional<User> findByUsernamePasswordIdentity(UsernamePasswordAuthUser identity) {
		User user = User.find.byId(identity.getEmail());
		if (user == null) {
			return Optional.empty();
		}
		if (BCrypt.checkpw(identity.getPassword(), user.hashedPassword)) {
			return Optional.of(user);
		}
		return Optional.empty();
	}

	public static User create(AuthUser authUser) {

		User user = new User();
		user.auth_id = authUser.getId();
		user.auth_provider = authUser.getProvider();
		if (authUser instanceof EmailIdentity) {
			user.email = ((EmailIdentity) authUser).getEmail();
		}
		if (authUser instanceof NameIdentity) {
			user.name = ((NameIdentity) authUser).getName();
		}
		if (authUser instanceof FirstLastNameIdentity) {
			user.firstName = ((FirstLastNameIdentity) authUser).getFirstName();
			user.lastName = ((FirstLastNameIdentity) authUser).getLastName();
		}
		if (authUser instanceof UsernamePasswordAuthUser) {
			user.hashedPassword = ((UsernamePasswordAuthUser) authUser).getHashedPassword();
		}
		if (authUser instanceof ExtendedIdentity) {
			user.gender = ((ExtendedIdentity) authUser).getGender();
		}
		if (authUser instanceof LocaleIdentity) {
			user.locale = ((LocaleIdentity) authUser).getLocale();
		}
		if (authUser instanceof PicturedIdentity) {
			user.picture = ((PicturedIdentity) authUser).getPicture();
		}
		if (authUser instanceof ProfiledIdentity) {
			user.profileLink = ((ProfiledIdentity) authUser).getProfileLink();
		}
		user.roles = "";
		user.linkedAccounts.add(UserLinkedAccount.create(authUser));

		user.save();

		return user;
	}

	public static void merge(AuthUser oldUser, AuthUser newUser) {
		User localOldUser = User.find.byId(oldUser.getId());
		User localNewUser = User.find.byId(newUser.getId());
		localOldUser.merge(localNewUser);
	}

	public static void addLinkedAccount(AuthUser oldUser, AuthUser newUser) {

		final User u = User.find.byId(oldUser.getId());

		if (u == null) {
			throw new RuntimeException("User was not found when trying to add linked account!");
		}

		u.linkedAccounts.add(UserLinkedAccount.create(newUser));
		u.save();
	}

	public void verify() {
		// You might want to wrap this into a transaction
		emailValidated = true;
		save();
	}

	public void resetPassword(final UsernamePasswordAuthUser authUser,
							  final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUserAndType(this, TokenAction.TokenType.PASSWORD_RESET);
	}

	public Optional<UserLinkedAccount> getAccountByProvider(final String providerKey) {
		return UserLinkedAccount.findByProviderKey(this, providerKey);
	}

	public void changePassword(final UsernamePasswordAuthUser authUser,
							   final boolean create) {
		Optional<UserLinkedAccount> account = this.getAccountByProvider(authUser.getProvider());
		if (!account.isPresent()) {
			if (create) {
				account = Optional.of(UserLinkedAccount.create(authUser));
			} else {
				throw new RuntimeException("Account not enabled for password usage");
			}
		}
		account.get().providerUserId = authUser.getHashedPassword();
		account.get().save();
	}

	public void merge(final User other) {
		for (final UserLinkedAccount acc : other.linkedAccounts) {
			this.linkedAccounts.add(UserLinkedAccount.create(acc));
		}
		// TODO do all other merging stuff here - like resources, etc.
		save();
	}

	public Set<String> getProviders() {
		return linkedAccounts.stream().map(acc -> acc.providerKey).collect(Collectors.toSet());
	}

	public void setRolesList(List<String> rolesList) {
		roles = Joiner.on(",").join(rolesList);
	}
}
