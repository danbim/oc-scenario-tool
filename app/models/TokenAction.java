package models;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.index.query.QueryBuilders;
import play.data.format.Formats;

import javax.persistence.Column;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@IndexType(name = "token_action")
public class TokenAction extends Index {

	public static final Finder<TokenAction> find = new Finder<>(TokenAction.class);
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static Duration VERIFICATION_TIME = Duration.ofDays(7);
	@Column(unique = true)
	public String token;
	public String user;
	public Type type;
	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime created;
	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime expires;

	public static Optional<TokenAction> findByToken(final String token) {

		IndexQuery<TokenAction> query = new IndexQuery<>(TokenAction.class);
		query.setBuilder(QueryBuilders.matchQuery("token", token));
		IndexResults<TokenAction> results = find.search(query);

		if (results.getTotalCount() == 0) {
			return Optional.empty();
		}

		if (results.getTotalCount() > 1) {
			throw new IllegalStateException(
					"More than one TokenAction for token \"" + token + "\" found. This should not be possible."
			);
		}

		return results.getResults().stream().findFirst();
	}

	public static void deleteByUser(final User u, final Type type) {
		IndexQuery<TokenAction> query = new IndexQuery<>(TokenAction.class);
		query.setBuilder(QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("user", u.getId()))
				.must(QueryBuilders.matchQuery("type", type.toString())));
		IndexResults<TokenAction> results = find.search(query);
		boolean allRemoved = results.getResults().stream()
				.map(Index::delete)
				.allMatch(DeleteResponse::isFound);
		if (!allRemoved) {
			throw new IllegalStateException("Could not delete one or more tokens when asked to delete token for user " +
					"\"" + u.id + " and token type \"" + type + "\".");
		}
	}

	public static TokenAction create(final Type type, final String token, final User user) {
		final LocalDateTime created = LocalDateTime.now();
		final TokenAction ua = new TokenAction();
		ua.user = user.getId();
		ua.token = token;
		ua.type = type;
		ua.created = created;
		ua.expires = created.plus(VERIFICATION_TIME);
		IndexResponse response = ua.index();
		if (!response.isCreated()) {
			throw new RuntimeException("TokenAction could not be created");
		}
		return ua;
	}

	@Override
	public Map toIndex() {
		return null;
	}

	@Override
	public Indexable fromIndex(Map map) {
		return null;
	}

	public boolean isValid() {
		return LocalDateTime.now().isBefore(expires);
	}

	public enum Type {
		EMAIL_VERIFICATION,
		PASSWORD_RESET
	}
}