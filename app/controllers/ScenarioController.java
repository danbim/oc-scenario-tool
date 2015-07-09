package controllers;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import models.Scenario;
import models.User;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.scenario;
import views.html.scenario_list;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

public class ScenarioController extends Controller {

	public Result list() {

		// TODO work async

		List<Scenario> scenarios;

		if (request().queryString().containsKey("q") && !"".equals(request().getQueryString("q"))) {

			// user provided a query string, search for it in all fields
			IndexQuery<Scenario> query = new IndexQuery<>(Scenario.class);
			String qs = request().getQueryString("q");
			query.setBuilder(
					multiMatchQuery(qs, Scenario.TITLE, Scenario.SUMMARY, Scenario.NARRATIVE)
			).from(0).size(Integer.MAX_VALUE);
			IndexResults<Scenario> search = finder().search(query);
			if (search.totalCount == 0) {
				scenarios = newArrayList();
			} else {
				scenarios = search.results;
			}

		} else {

			// user didn't provide a query string, return all scenarios
			scenarios = finder().all().results;
		}

		if (request().accepts("text/html")) {
			List<User> users;
			IndexResults<User> result = new Index.Finder<>(User.class).all();
			if (result.totalCount == 0) {
				users = newArrayList();
			} else {
				users = result.results;
			}
			return ok(scenario_list.render(scenarios, users));
		}

		return ok(Json.toJson(scenarios));
	}

	public Result get(String id) {

		// TODO work async

		Optional<Scenario> found = Optional.ofNullable(finder().byId(String.valueOf(id)));

		if (!found.isPresent()) {
			return notFound();
		}
		if (request().accepts("text/html")) {
			return ok(scenario.render(found.get()));
		}
		return ok(Json.toJson(found.get()));
	}

	public Result create() {

		// TODO work async

		Form<Scenario> form = new Form<>(Scenario.class);
		Scenario scenario = form.bindFromRequest().get();
		IndexResponse response = scenario.index();

		if (response.isCreated()) {
			scenario.id = response.getId();
			response().setHeader("Location", controllers.routes.ScenarioController.get(scenario.id).url());
			return created();
		} else {
			return internalServerError();
		}
	}

	public Result delete(String id) {

		Scenario scenario = finder().byId(id);

		if (scenario == null) {

			return notFound();

		} else {

			DeleteResponse response = scenario.delete();
			if (response.isFound()) {
				return noContent();
			} else {
				return internalServerError();
			}
		}
	}

	private Index.Finder<Scenario> finder() {
		return new Index.Finder<>(Scenario.class);
	}
}
