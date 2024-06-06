package perfios.rbacs.Model.LoginResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse2 {
    private int userRoleId;
    private int userId;
    private String RoleName;
    private String userEmail;
    private String userPassword;
}
