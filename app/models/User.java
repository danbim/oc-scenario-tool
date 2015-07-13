package models;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.*;
import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexUtils;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import formatters.CommaSeparated;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.util.LocaleUtils;
import play.data.validation.Constraints;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static models.Helpers.findSingle;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@IndexType(name = "user")
public class User extends Index {

	public static final String NAME = "name";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String GENDER = "gender";
	public static final String LOCALE = "locale";
	public static final String PROFILE_LINK = "profile_link";
	public static final String EMAIL = "email";
	public static final String EMAIL_VALIDATED = "email_validated";
	public static final String HASHED_PASSWORD = "hashed_password";
	public static final String AUTH_ID = "auth_id";
	public static final String AUTH_PROVIDER = "auth_provider";
	public static final String LINKED_ACCOUNTS = "linked_accounts";
	public static final String ROLES = "roles";

	public String name;

	public String firstName;

	public String lastName;

	public String gender;

	@Constraints.Email
	public String email;

	public boolean emailValidated;

	public String hashedPassword;

	public Locale locale;

	public String picture;

	public String profileLink;

	public String auth_id;

	public String auth_provider;

	public List<UserLinkedAccount> linkedAccounts = new ArrayList<>();
	@CommaSeparated
	public List<String> roles = new ArrayList<>();

	public static Optional<User> findById(String id) {
		return Helpers.findById(User.class, id);
	}

	public static Optional<User> findByEmail(String email) {
		return findSingle(User.class, matchQuery(User.EMAIL, email));
	}

	public static Optional<User> findByUsernamePasswordIdentity(UsernamePasswordAuthUser identity) {
		return findSingle(User.class, boolQuery()
						.must(matchQuery(EMAIL, identity.getEmail()))
						.must(matchQuery(HASHED_PASSWORD, identity.getHashedPassword()))
		);
	}

	public static Optional<User> findByAuthUserIdentity(AuthUserIdentity identity) {
		if (identity == null) {
			return Optional.empty();
		}
		return findSingle(User.class, matchQuery(User.AUTH_ID, identity.getId()));
	}

	public static boolean existsByAuthUserIdentity(AuthUser authUser) {
		return findByAuthUserIdentity(authUser).isPresent();
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
		user.roles = new ArrayList<>();
		user.linkedAccounts.add(UserLinkedAccount.create(user, authUser));

		IndexResponse response = user.index();

		if (!response.isCreated()) {
			throw new RuntimeException("User could not be created");
		}


		return user;
	}

	public static void merge(AuthUser oldUser, AuthUser newUser) {
		User localOldUser = User.findByAuthUserIdentity(oldUser).get();
		User localNewUser = User.findByAuthUserIdentity(newUser).get();
		localOldUser.merge(localNewUser);
	}

	public static void addLinkedAccount(AuthUser oldUser, AuthUser newUser) {

		final Optional<User> u = User.findByAuthUserIdentity(oldUser);

		if (!u.isPresent()) {
			throw new RuntimeException("User was not found when trying to add linked account!");
		}

		User user = u.get();

		user.linkedAccounts.add(UserLinkedAccount.create(user, newUser));
		user.index();
	}

	public static void verify(final User unverified) {
		// You might want to wrap this into a transaction
		unverified.emailValidated = true;
		unverified.index();
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
				account = Optional.of(UserLinkedAccount.create(this, authUser));
			} else {
				throw new RuntimeException("Account not enabled for password usage");
			}
		}
		account.get().providerUserId = authUser.getHashedPassword();
		account.get().index();
	}

	public void merge(final User other) {
		for (final UserLinkedAccount acc : other.linkedAccounts) {
			this.linkedAccounts.add(UserLinkedAccount.create(acc));
		}
		// TODO do all other merging stuff here - like resources, etc.
		index();
	}

	public Set<String> getProviders() {
		return linkedAccounts.stream().map(acc -> acc.providerKey).collect(Collectors.toSet());
	}

	@Override
	public Map toIndex() {

		HashMap<String, Object> map = newHashMap();

		map.put(NAME, name);
		map.put(FIRST_NAME, firstName);
		map.put(LAST_NAME, lastName);
		map.put(GENDER, gender);
		map.put(LOCALE, locale == null ? null : LocaleUtils.toString(locale));
		map.put(PROFILE_LINK, profileLink);
		map.put(EMAIL, email);
		map.put(EMAIL_VALIDATED, emailValidated);
		map.put(AUTH_ID, auth_id);
		map.put(AUTH_PROVIDER, auth_provider);
		map.put(LINKED_ACCOUNTS, IndexUtils.toIndex(linkedAccounts));
		map.put(ROLES, roles);

		return map;
	}

	@Override
	public Indexable fromIndex(Map map) {

		if (map == null) {
			return null;
		}

		name = (String) map.get(NAME);
		firstName = (String) map.get(FIRST_NAME);
		lastName = (String) map.get(LAST_NAME);
		gender = (String) map.get(GENDER);
		if (map.get(LOCALE) == null) {
			locale = Locale.getDefault();
		} else {
			locale = LocaleUtils.parse((String) map.get(LOCALE));
		}
		profileLink = (String) map.get(profileLink);
		email = (String) map.get(EMAIL);
		if (map.get(EMAIL_VALIDATED) == null) {
			emailValidated = false;
		} else {
			emailValidated = (boolean) map.get(EMAIL_VALIDATED);
		}
		auth_id = (String) map.get(AUTH_ID);
		auth_provider = (String) map.get(AUTH_PROVIDER);
		linkedAccounts = IndexUtils.getIndexables(map, LINKED_ACCOUNTS, UserLinkedAccount.class);
		//noinspection unchecked
		roles = map.containsKey(ROLES) ? (List<String>) map.get(ROLES) : new ArrayList<>();

		return this;
	}
}
