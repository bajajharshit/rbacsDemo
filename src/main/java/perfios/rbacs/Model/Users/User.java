package perfios.rbacs.Model.Users;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class User {
    private int userId;
    private String userFirstName;
    private String userLastName;
    private String  userPassword;
    private String userPhoneNumber;
    private String userStatus;
    private String userEmail;
    private String userRoleName;
    private List<String> userRoleNameList = new ArrayList<>();
    private int userRoleId;
}