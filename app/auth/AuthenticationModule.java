package auth;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class AuthenticationModule extends Module {
	@Override
	public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
		return seq(bind(UsernamePasswordAuthProvider.class).to(MyUsernamePasswordAuthProvider.class));
	}
}
