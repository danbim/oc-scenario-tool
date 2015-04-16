package controllers;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import models.User;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.user_list;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

public class UserController extends Controller {

    public static Result list() {

        List<User> users;

        String q = request().getQueryString("q");
        if (q != null && !"".equals(q)) {

            // user provided a query string, search for it in all fields
            IndexQuery<User> query = new IndexQuery<>(User.class);
            query.setBuilder(multiMatchQuery(q, User.NAME));
            IndexResults<User> search = finder().search(query);

            if (search.getTotalCount() == 0) {
                users = newArrayList();
            } else {
                users = search.getResults();
            }

        } else {
            users = finder().all().getResults();
        }

        if (request().accepts("text/html")) {
            return ok(user_list.render(users));
        }

        return ok(Json.toJson(users));
    }

    public static Result get(String id) {
        User user = finder().byId(id);
        if (user == null) {
            return notFound();
        }
        return ok(Json.toJson(user));
    }

    public static Result create() {
        Form<User> form = new Form<>(User.class);
        User user = form.bindFromRequest().get();
        IndexResponse indexResponse = user.index();
        if (indexResponse.isCreated()) {
            user.id = indexResponse.getId();
            response().setHeader("Location", controllers.routes.UserController.get(user.id).url());
            return created();
        }
        return internalServerError();
    }

    private static Index.Finder<User> finder() {
        return new Index.Finder<>(User.class);
    }

    public static Result delete(String id) {

        User user = finder().byId(id);

        if (user == null) {

            return notFound();

        } else {

            DeleteResponse response = user.delete();
            if (response.isFound()) {
                return noContent();
            } else {
                return internalServerError();
            }
        }
    }
}
