package views.formdata;

import play.data.validation.Constraints;
import play.data.validation.Constraints.Validate;
import play.data.validation.Constraints.Validatable;
import java.util.ArrayList;
import java.util.List;
import models.Client;

/**
 * Backing class for the login form.
 */
@Validate
public class LoginFormData implements Validatable<String> {

  /** The submitted username. */
  public String username = "";
  /** The submitted password. */
  public String password = "";

  /** Required for form instantiation. */
  public LoginFormData() {
  }

  /**
   * Validates Form<LoginFormData>.
   * Called automatically in the controller by bindFromRequest().
   * Checks to see that email and password are valid credentials.
   * @return Null if valid, or a List[ValidationError] if problems found.
   */
  public String validate() {
    if (!Client.isValid(username, password)) {
      return "Invalid username or password";
    }
    return null;
  }

}
