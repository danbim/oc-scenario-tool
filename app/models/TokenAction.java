package models;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import play.data.format.Formats;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static models.Helpers.findSingle;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@IndexType(name = "token_action")
public class TokenAction extends Index {

	public static final String TOKEN = "token";
	public static final String TOKEN_TYPE = "token_type";
	public static final String USER = "user";
	public static final String CREATED = "created";
	public static final String EXPIRES = "expires";

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static Duration VERIFICATION_DURATION = Duration.ofDays(7);

	public String token;

	public String user;

	public TokenType tokenType;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime created;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime expires;

	public static Optional<TokenAction> findByToken(final String token, final TokenType tokenType) {
		return findSingle(TokenAction.class, boolQuery()
						.must(matchQuery(TOKEN, token))
						.must(matchQuery(TOKEN_TYPE, tokenType.toString()))
		);
	}

	public static void deleteByUserAndType(final User u, final TokenType tokenType) {

		BoolQueryBuilder query = boolQuery()
				.must(matchQuery("user", u.id))
				.must(matchQuery("type", tokenType.toString()));

		boolean allRemoved = Helpers.find(TokenAction.class,
				query).stream()
				.map(Index::delete)
				.allMatch(DeleteResponse::isFound);

		if (!allRemoved) {
			throw new IllegalStateException("Could not delete one or more tokens when asked to delete token for user " +
					"\"" + u.id + " and token type \"" + tokenType + "\".");
		}
	}

	public static TokenAction create(final TokenType tokenType, final String token, final User user) {
		final LocalDateTime created = LocalDateTime.now();
		final TokenAction ua = new TokenAction();
		ua.user = user.id;
		ua.token = token;
		ua.tokenType = tokenType;
		ua.created = created;
		ua.expires = created.plus(VERIFICATION_DURATION);
		IndexResponse response = ua.index();
		if (!response.isCreated()) {
			throw new RuntimeException("TokenAction could not be created");
		}
		return ua;
	}

	@Override
	public Map toIndex() {
		Map<String, Object> map = new HashMap<>();
		map.put(USER, user);
		map.put(TOKEN, token);
		map.put(TOKEN_TYPE, tokenType.toString());
		map.put(CREATED, created.toString());
		map.put(EXPIRES, expires.toString());
		return map;
	}

	@Override
	public Indexable fromIndex(Map map) {
		user = (String) map.get(USER);
		token = (String) map.get(TOKEN);
		tokenType = TokenType.valueOf((String) map.get(TOKEN_TYPE));
		created = LocalDateTime.parse((String) map.get(CREATED));
		expires = LocalDateTime.parse((String) map.get(EXPIRES));
		return this;
	}

	public boolean isValid() {
		return LocalDateTime.now().isBefore(expires);
	}

	public enum TokenType {
		EMAIL_VERIFICATION,
		PASSWORD_RESET
	}
}