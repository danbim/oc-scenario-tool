package exceptions;

public class EntityExistsException extends RuntimeException {

	private final Class clazz;

	public EntityExistsException(Class clazz) {
		this.clazz = clazz;
	}

	@Override
	public String getMessage() {
		return "Entity of type " + clazz.getCanonicalName() + " already exists.";
	}
}
