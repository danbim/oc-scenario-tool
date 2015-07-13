package models;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexQuery;
import exceptions.EntityExistsException;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;
import java.util.Optional;

public abstract class Helpers {

	public static <T extends Index> Optional<T> findSingle(Class<T> clazz, QueryBuilder queryBuilder) {

		List<T> found = find(clazz, queryBuilder);

		if (found.isEmpty()) {
			return Optional.empty();
		}

		if (found.size() > 1) {
			throw new EntityExistsException(clazz);
		}

		return found.stream().findFirst();
	}

	public static <T extends Index> List<T> find(Class<T> clazz, QueryBuilder queryBuilder) {
		Index.Finder<T> finder = new Index.Finder<>(clazz);
		IndexQuery<T> query = new IndexQuery<>(clazz);
		query.setBuilder(queryBuilder);
		return finder.search(query).results;
	}

	public static <T extends Index> Optional<T> findById(Class<T> clazz, String id) {
		Index.Finder<T> finder = new Index.Finder<>(clazz);
		T t = finder.byId(id);
		if (t == null) {
			return Optional.empty();
		}
		return Optional.of(t);
	}
}
