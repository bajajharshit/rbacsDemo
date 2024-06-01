package perfios.rbacs.Model.LoginPost;


import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class LoginPostOb {
    @Email
    String userEmail;
    String userPassword;
}
