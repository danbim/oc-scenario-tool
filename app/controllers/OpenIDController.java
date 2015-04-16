package controllers;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.F.Promise;
import play.libs.openid.OpenID;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.HashMap;
import java.util.Map;

public class OpenIDController extends Controller {

    public static Result login() {
        return ok(views.html.login.render(""));
    }

    public static Promise<Result> loginPost() {

        final DynamicForm requestData = Form.form().bindFromRequest();
        final String openID = requestData.get("openID");

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", "http://schema.openid.net/contact/email");

        final Promise<String> redirectUrlPromise = OpenID.redirectURL(
                openID,
                controllers.routes.OpenIDController.openIDCallback().absoluteURL(request()),
                attributes
        );

        return redirectUrlPromise
                .map(Results::redirect)
                .recover(throwable -> badRequest(views.html.login.render(throwable.getMessage())));
    }

    public static Promise<Result> openIDCallback() {

        return OpenID.verifiedId()
                .map(userInfo -> (Result) ok(userInfo.id + "\n" + userInfo.attributes))
                .recover(throwable -> badRequest(views.html.login.render(throwable.getMessage())));
    }

}