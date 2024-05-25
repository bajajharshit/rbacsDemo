package perfios.rbacs.Model.LoginDetails;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoginDetails {
    Boolean isUserExist;
    List<Integer> permissionList = new ArrayList<>();
    String anyMessage;
    Boolean isEnabledToLogin;
}
