import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.github.cleverage.elasticsearch.Index;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import controllers.routes;
import formatters.CommaSeparated;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.data.format.Formatters;
import play.libs.Json;
import play.mvc.Call;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class Global extends GlobalSettings {

	public static final Splitter SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

	public void onStart(final Application app) {

		Json.setObjectMapper(new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT));

		Formatters.AnnotationFormatter<CommaSeparated, List> commaSeparatedFormatter = new Formatters.AnnotationFormatter<CommaSeparated, List>() {

			@Override
			public List parse(CommaSeparated annotation, String s, Locale locale) throws ParseException {
				return newArrayList(SPLITTER.split(s)).stream().map(String::toLowerCase).collect(Collectors.toList());
			}

			@Override
			public String print(CommaSeparated userRoleSet, List roles, Locale locale) {
				return Joiner.on(",").join(roles);
			}
		};

		Formatters.register(List.class, commaSeparatedFormatter);

		Formatters.register(User.class, new Formatters.SimpleFormatter<User>() {
			@Override
			public User parse(String s, Locale locale) throws ParseException {
				return new Index.Finder<>(User.class).byId(s);
			}

			@Override
			public String print(User user, Locale locale) {
				return user.id;
			}
		});

		PlayAuthenticate.setResolver(new Resolver() {

			@Override
			public Call login() {
				// Your login page
				return routes.Application.index();
			}

			@Override
			public Call afterAuth() {
				// The user will be redirected to this page after authentication
				// if no original URL was saved
				return routes.Application.index();
			}

			@Override
			public Call afterLogout() {
				return routes.Application.index();
			}

			@Override
			public Call auth(final String provider) {
				// You can provide your own authentication implementation,
				// however the default should be sufficient for most cases
				return com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider);
			}

			@Override
			public Call onException(final AuthException e) {
				if (e instanceof AccessDeniedException) {
					AccessDeniedException ex = (AccessDeniedException) e;
					return controllers.routes.Application.oAuthDenied(ex.getProviderKey());
				}
				// more custom problem handling here...
				return super.onException(e);
			}

			@Override
			public Call askLink() {
				// We don't support moderated account linking in this sample.
				// See the play-authenticate-usage project for an example
				return null;
			}

			@Override
			public Call askMerge() {
				// We don't support moderated account merging in this sample.
				// See the play-authenticate-usage project for an example
				return null;
			}
		});
	}

}