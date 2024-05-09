package perfios.rbacs.Model.LoginDetails;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoginDetails {
    Boolean isEmailIdCorrect = false;
    Boolean isPasswordCorrect = false;
    List<Integer> permissionList = new ArrayList<>();
    String anyMessage;
}
