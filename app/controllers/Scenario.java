package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.html.scenario;
import views.html.scenario_list;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

public class Scenario extends Controller {

    private static final List<models.Scenario> scenarios = newArrayList(
            new models.Scenario(1, "First Scenario", "Me first yeah, yeah!", "lorem"),
            new models.Scenario(2, "Second Scenario", "Me second, bäh!", "ipsum"),
            new models.Scenario(3, "Third Scenario", "Me third, fuck it!", "dolor")
    );

    public static Result list() {
        if (request().accepts("text/html")) {
            return ok(scenario_list.render(scenarios));
        }
        return ok(Json.toJson(scenarios));
    }

    public static Result get(Long id) {

        Optional<models.Scenario> found = scenarios.stream().filter((sc) -> sc.id == id).findFirst();

        if (!found.isPresent()) {
            return notFound();
        }
        if (request().accepts("text/html")) {
            return ok(scenario.render(found.get()));
        }
        return ok(Json.toJson(found.get()));
    }
}
