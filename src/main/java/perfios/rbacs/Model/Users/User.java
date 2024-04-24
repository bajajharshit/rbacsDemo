package perfios.rbacs.Model.Users;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String userFirstName;
    private String userLastName;
    private String  userPassword;
    private String userPhoneNumber;
    private String userStatus;
    private String userEmail;
    private int userRoleId;
}