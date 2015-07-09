package auth;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;


public class MyHandlerCache implements HandlerCache {

	private final DeadboltHandler authorizationHandler = new OrganiCityAuthorizationHandler();

	@Override
	public DeadboltHandler apply(String s) {
		return authorizationHandler;
	}

	@Override
	public DeadboltHandler get() {
		return authorizationHandler;
	}
}
