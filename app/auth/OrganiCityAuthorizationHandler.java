package auth;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import models.User;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class OrganiCityAuthorizationHandler extends AbstractDeadboltHandler {

    @Override
    public F.Promise<Result> beforeAuthCheck(Http.Context context) {
        return F.Promise.pure(null);
    }

    @Override
    public Subject getSubject(Http.Context context) {
        AuthUser user = PlayAuthenticate.getUser(context.session());
        if (user == null) {
            return null;
        }
        Optional<User> localUser = User.findByAuthUserIdentity(user);
        if (!localUser.isPresent()) {
            return null;
        }
        return new LocalSubject(localUser.get());
    }

    @Override
    public F.Promise<Result> onAuthFailure(Http.Context context, String s) {
        return F.Promise.pure(unauthorized());
    }

    @Override
    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context) {
        return null;
    }

    public static class LocalRole implements Role {

        private final String role;

        public LocalRole(String role) {
            this.role = role;
        }

        @Override
        public String getName() {
            return role;
        }
    }

    public static class LocalSubject implements Subject {

        private final User user;

        public LocalSubject(User user) {
            this.user = user;
        }

        @Override
        public List<? extends Role> getRoles() {
            if (user.roles == null) {
                return new ArrayList<>();
            }
            return user.roles.stream()
                    .map(LocalRole::new)
                    .collect(Collectors.toList());
        }

        @Override
        public List<? extends Permission> getPermissions() {
            return newArrayList();
        }

        @Override
        public String getIdentifier() {
            return user.id;
        }
    }
}
