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

import static com.google.common.collect.Lists.newArrayList;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

public class Scenario extends Controller {

    public static Result list() {

        // TODO work async

        List<models.Scenario> scenarios;

        if (request().queryString().containsKey("q") && !"".equals(request().getQueryString("q"))) {

            // user provided a query string, search for it in all fields
            IndexQuery<models.Scenario> query = new IndexQuery<>(models.Scenario.class);
            String qs = request().getQueryString("q");
            query.setBuilder(
                    multiMatchQuery(qs, models.Scenario.TITLE, models.Scenario.SUMMARY, models.Scenario.NARRATIVE)
            );
            IndexResults<models.Scenario> search = finder().search(query);
            if (search.getTotalCount() == 0) {
                scenarios = newArrayList();
            } else {
                scenarios = search.getResults();
            }

        } else {

            // user didn't provide a query string, return all scenarios
            scenarios = finder().all().results;
        }

        if (request().accepts("text/html")) {
            return ok(scenario_list.render(scenarios));
        }

        return ok(Json.toJson(scenarios));
    }

    public static Result get(String id) {

        // TODO work async

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

    private static Index.Finder<models.Scenario> finder() {
        return new Index.Finder<>(models.Scenario.class);
    }
}
