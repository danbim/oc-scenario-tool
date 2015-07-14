package controllers;

import com.avaje.ebean.annotation.Transactional;
import com.feth.play.module.pa.PlayAuthenticate;
import models.Scenario;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.scenario;
import views.html.scenario_created;
import views.html.scenario_list;

import java.util.List;
import java.util.stream.Collectors;

public class Scenarios extends Controller {

	@Transactional(readOnly = true)
	public Result list() {

		List<Scenario> scenarios;

		if (request().queryString().containsKey("q") && !"".equals(request().getQueryString("q"))) {

			// user provided a query string, search for it in all fields
			// TODO do some REAL search ;-)
			String qs = request().getQueryString("q");
			scenarios = Scenario.find.all()
					.stream()
					.filter(scenario -> scenario.creator.name.contains(qs) ||
							scenario.creator.email.contains(qs) ||
							scenario.title.contains(qs) ||
							scenario.summary.contains(qs) ||
							scenario.narrative.contains(qs))
					.collect(Collectors.toList());

		} else {

			// user didn't provide a query string, return all scenarios
			scenarios = Scenario.find.all();
		}

		if (request().accepts("text/html")) {
			return ok(scenario_list.render(scenarios));
		}

		return ok(Json.toJson(scenarios));
	}

	@Transactional(readOnly = true)
	public Result get(Long id) {

		Scenario found = Scenario.find.byId(id);

		if (found == null) {
			return notFound();
		}

		if (request().accepts("text/html")) {
			return ok(scenario.render(found));
		}

		return ok(Json.toJson(found));
	}

	@Transactional
	public Result create() {
		Form<Scenario> form = new Form<>(Scenario.class);
		Scenario scenario = form.bindFromRequest().get();
		scenario.creator = currentUser();
		scenario.insert();
		response().setHeader("Location", controllers.routes.Scenarios.get(scenario.id).url());
		if (request().accepts("text/html")) {
			return created(scenario_created.render(scenario));
		}
		return created();
	}

	private User currentUser() {
		return User.find.byId(PlayAuthenticate.getUser(session()).getId());
	}

	@Transactional
	public Result delete(Long id) {

		Scenario scenario = Scenario.find.byId(id);

		if (scenario == null) {
			return notFound();
		} else {
			scenario.delete();
			return noContent();
		}
	}
}
