package perfios.rbacs.Model.LoginResponse;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;

@Data
@Builder
public class LoginResponse {
    private int userId;
    private String userEmailId;
    private String userPassword;
    private HashSet<String > userPermissionId;
    private int userRoleId;
}
