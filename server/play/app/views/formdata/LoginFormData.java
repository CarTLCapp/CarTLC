package views.formdata;

import play.data.validation.ValidationError;
import java.util.ArrayList;
import java.util.List;
import models.UserInfo;

/**
 * Backing class for the login form.
 */
public class LoginFormData {

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
  public List<ValidationError> validate() {

    List<ValidationError> errors = new ArrayList<>();
    
    if (!UserInfo.isValid(username, password)) {
      errors.add(new ValidationError("username", ""));
      errors.add(new ValidationError("password", ""));      
    }

    return (errors.size() > 0) ? errors : null;
  }

}
