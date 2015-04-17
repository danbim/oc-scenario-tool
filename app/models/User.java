package models;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.github.cleverage.elasticsearch.*;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import org.elasticsearch.action.index.IndexResponse;
import play.data.validation.Constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@IndexType(name = "user")
public class User extends Index {

    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String AUTH_ID = "auth_id";
    public static final String AUTH_PROVIDER = "auth_provider";
    public static final String LINKED_ACCOUNTS = "linked_accounts";

    public String name;

    @Constraints.Email
    public String email;

    public String auth_id;

    public String auth_provider;

    public List<UserLinkedAccount> linkedAccounts = newArrayList();

    public static Optional<User> findByAuthUserIdentity(AuthUserIdentity identity) {

        Finder<User> finder = new Finder<>(User.class);
        IndexQuery<User> query = new IndexQuery<>(User.class);
        if (identity instanceof EmailIdentity) {
            query.setBuilder(matchQuery(User.EMAIL, ((EmailIdentity) identity).getEmail()));
        } else {
            query.setBuilder(matchQuery(User.AUTH_ID, identity.getId()));
        }
        IndexResults<User> search = finder.search(query);

        if (search.getTotalCount() == 0) {
            return Optional.empty();
        }

        return search.getResults().stream().findFirst();
    }

    public static boolean existsByAuthUserIdentity(AuthUser authUser) {
        return findByAuthUserIdentity(authUser).isPresent();
    }

    public static User create(AuthUser authUser) {

        User user = new User();
        user.auth_id = authUser.getId();
        user.auth_provider = authUser.getProvider();

        user.linkedAccounts.add(UserLinkedAccount.create(user, authUser));

        if (authUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) authUser;
            // TODO validate email
            // Remember, even when getting them from FB & Co., emails should be
            // verified within the application as a security breach there might
            // break your security as well!
            // user.emailValidated = false;
            user.email = identity.getEmail();
        }

        if (authUser instanceof NameIdentity) {
            final NameIdentity identity = (NameIdentity) authUser;
            final String name = identity.getName();
            if (name != null) {
                user.name = name;
            }
        }

        IndexResponse response = user.index();

        if (!response.isCreated()) {
            throw new RuntimeException("User could not be created");
        }

        user.id = response.getId();

        return user;
    }

    public void merge(final User otherUser) {
        for (final UserLinkedAccount acc : otherUser.linkedAccounts) {
            this.linkedAccounts.add(UserLinkedAccount.create(acc));
        }
        // TODO do all other merging stuff here - like resources, etc.
        index();
    }

    public static void merge(AuthUser oldUser, AuthUser newUser) {
        User localOldUser = User.findByAuthUserIdentity(oldUser).get();
        User localNewUser = User.findByAuthUserIdentity(newUser).get();
        localOldUser.merge(localNewUser);
    }

    public static void addLinkedAccount(AuthUser oldUser, AuthUser newUser) {

        final Optional<User> u = User.findByAuthUserIdentity(oldUser);

        if (!u.isPresent()) {
            throw new RuntimeException("User was not found when trying to add linked account!");
        }

        User user = u.get();

        user.linkedAccounts.add(UserLinkedAccount.create(user, newUser));
        user.index();
    }

    @Override
    public Map toIndex() {

        HashMap<String, Object> map = newHashMap();

        map.put(NAME, name);
        map.put(EMAIL, email);
        map.put(AUTH_ID, auth_id);
        map.put(AUTH_PROVIDER, auth_provider);
        map.put(LINKED_ACCOUNTS, IndexUtils.toIndex(linkedAccounts));

        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {

        if (map == null) {
            return null;
        }

        this.name = (String) map.get(NAME);
        this.email = (String) map.get(EMAIL);
        this.auth_id = (String) map.get(AUTH_ID);
        this.auth_provider = (String) map.get(AUTH_PROVIDER);
        this.linkedAccounts = IndexUtils.getIndexables(map, LINKED_ACCOUNTS, UserLinkedAccount.class);

        return this;
    }
}
