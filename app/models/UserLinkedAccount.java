package models;

import com.feth.play.module.pa.user.AuthUser;
import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static models.Helpers.findSingle;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@IndexType(name = "user_linked_account")
public class UserLinkedAccount extends Index {

	public static final String USER_ID = "user_id";
	public static final String PROVIDER_KEY = "provider_key";
	public static final String PROVIDER_USER_ID = "provider_user_id";

	public String email;
	public boolean emailIsVerified;
	public String name;
	public String firstName;
	public String lastName;
	public URL picture;
	public String gender;
	public Locale locale;
	public String profile;

	public String userId;
	public String providerKey;
	public String providerUserId;

	public static UserLinkedAccount create(User user, final AuthUser authUser) {
		final UserLinkedAccount ret = new UserLinkedAccount();
		ret.userId = user.getId();
		ret.providerKey = authUser.getProvider();
		ret.providerUserId = authUser.getId();
		return ret;
	}

	public static UserLinkedAccount create(final UserLinkedAccount acc) {
		final UserLinkedAccount ret = new UserLinkedAccount();
		ret.userId = acc.userId;
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;
		return ret;
	}

	public static Optional<UserLinkedAccount> findByProviderKey(final User user, String key) {
		return findSingle(UserLinkedAccount.class, boolQuery()
						.must(matchQuery(USER_ID, user.getId()))
						.must(matchQuery(PROVIDER_KEY, key))
		);
	}

	@Override
	public Map toIndex() {
		Map<String, Object> map = newHashMap();
		map.put(USER_ID, userId);
		map.put(PROVIDER_KEY, providerKey);
		map.put(PROVIDER_USER_ID, providerUserId);
		return map;
	}

	@Override
	public Indexable fromIndex(Map map) {
		this.userId = (String) map.get(USER_ID);
		this.providerKey = (String) map.get(PROVIDER_KEY);
		this.providerUserId = (String) map.get(PROVIDER_USER_ID);
		return this;
	}
}