package dto;

import play.data.format.Formats;
import play.data.validation.Constraints;

public class Accept {

	@Constraints.Required
	@Formats.NonEmpty
	public Boolean accept;

	public Boolean getAccept() {
		return accept;
	}

	public void setAccept(Boolean accept) {
		this.accept = accept;
	}

}
