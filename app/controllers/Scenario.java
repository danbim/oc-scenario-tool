package controllers;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import org.elasticsearch.action.index.IndexResponse;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.scenario;
import views.html.scenario_list;

import java.util.List;
import java.util.Optional;

public class Scenario extends Controller {

    public static Result list() {
        List<models.Scenario> scenarios = finder().all().results;
        if (request().accepts("text/html")) {
            return ok(scenario_list.render(scenarios));
        }
        return ok(Json.toJson(scenarios));
    }

    public static Result get(String id) {

        Optional<models.Scenario> found = Optional.ofNullable(finder().byId(String.valueOf(id)));

        if (!found.isPresent()) {
            return notFound();
        }
        if (request().accepts("text/html")) {
            return ok(scenario.render(found.get()));
        }
        return ok(Json.toJson(found.get()));
    }

    public static Result create() {
        // TODO work async
        Form<models.Scenario> form = new Form<>(models.Scenario.class);
        models.Scenario scenario = form.bindFromRequest().get();
        IndexResponse response = scenario.index();
        if (response.isCreated()) {
            scenario.id = response.getId();
            return created();
        } else {
            return internalServerError();
        }
    }

    public static Result find(String searchTerm) {
        // TODO work async
        IndexQuery<models.Scenario> query = new IndexQuery<>(models.Scenario.class);
        query.setQuery("title:("+ searchTerm+")");

        IndexResults<models.Scenario> search = finder().search(query);
        if (search.getTotalCount() == 0) {
            return notFound();
        }
        return ok(Json.toJson(search.results));
    }

    private static Index.Finder<models.Scenario> finder() {
        return new Index.Finder<>(models.Scenario.class);
    }
}
