package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.google.common.base.Splitter;
import models.User;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.user_list;

import java.util.List;
import java.util.stream.Collectors;

@Restrict(@Group("admin"))
public class Users extends Controller {

	public Result list() {

		List<User> users;

		String q = request().getQueryString("q");
		if (q != null && !"".equals(q)) {

			// user provided a query string, search for it in all fields
			// TODO do some REAL search
			users = User.find.all()
					.stream()
					.filter(user -> user.email.contains(q) || user.name.contains(q))
					.collect(Collectors.toList());

		} else {
			users = User.find.all();
		}

		if (request().accepts("text/html")) {
			return ok(user_list.render(users));
		}

		return ok(Json.toJson(users));
	}

	public Result setRoles(String email) {

		User user = User.find.byId(email);
		if (user == null) {
			return notFound();
		}

		Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
		DynamicForm form = new DynamicForm().bindFromRequest();

		user.setRolesList(splitter.splitToList(form.get("roles"))
				.stream()
				.map(String::toLowerCase)
				.collect(Collectors.toList())
		);

		user.save();

		return ok(Json.toJson(user));
	}

	public Result get(String email) {
		User user = User.find.byId(email);
		if (user == null) {
			return notFound();
		}
		return ok(Json.toJson(user));
	}

	public Result create() {
		Form<User> form = new Form<>(User.class);
		User user = form.bindFromRequest().get();
		user.insert();
		response().setHeader("Location", controllers.routes.Users.get(user.email).url());
		return created();
	}

	public Result delete(String email) {
		User user = User.find.byId(email);
		if (user == null) {
			return notFound();
		} else {
			user.delete();
			return noContent();
		}
	}
}
