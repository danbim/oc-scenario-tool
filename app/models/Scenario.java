package models;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Scenario extends Model {

	public static Find<Long, Scenario> find = new Find<Long, Scenario>() {
	};

	@Id
	public Long id;

	@Constraints.Required
	public String title;

	@Constraints.Required
	public String summary;

	@Constraints.Required
	public String narrative;

	public User creator;

}
