package perfios.rbacs.Model.Users;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data

public class User {
    private int userId;

    @NotBlank(message = "FirstName cannot be blank")
    @Size(max = 20, message = "FirstName can't be more than 20 letters")
    private String userFirstName;

    @NotBlank(message = "LastName cannot be blank")
    @Size(max = 15, message = "LastName can't be more than 15 letters")
    private String userLastName;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 6, message = "password must be atleast 6 characters")
    private String userPassword;

    @NotBlank(message = "Phone number can't be empty")
    @Pattern(regexp = "^\\d+$", message = "only digits are allowed in phone number")
    @Size(min = 10, max = 10, message = "Phone number should be of 10 digits")
    private String userPhoneNumber;

    @NotBlank(message = "alternateUsername can't be empty")
    @Size(min = 1, max = 30)
    private String alternateUsername;

    @NotBlank(message = "Select a valid status")
    private String userStatus;

    @Email(message = "Email ID should be valid")
    private String userEmail;

    private String userRoleName;
    private List<String> userRoleNameList = new ArrayList<>();

    @NotNull(message = "Select a Valid Role")
    @Min(value = 1, message = "Select a Valid Role")
    private int userRoleId;

    Boolean enabled;
    Boolean isSuperAdmin;
    Boolean shouldLoanAutoApply;


       /*
    {
"userFirstName": "userSeven",
"userLastName": "userSevenLastName",
"userPassword": "userSevenPassword",
"userPhoneNumber": "9889487778",
"userStatus": "Active",
"userEmail": "userseven@example.com",
"userRoleId": 5,
"enabled": true,
"isSuperAdmin": false,
"shouldLoanAutoApply": 0
}

     */


    public Boolean setFeildsFromMapForCsvFile(Map<String, String> individualUser) {
        try {
            setUserFirstName(individualUser.get("userFirstName"));
            setUserLastName(individualUser.get("userLastName"));
            setUserPassword(individualUser.get("userPassword"));
            setUserPhoneNumber(individualUser.get("userPhoneNumber"));
            setUserStatus(individualUser.get("userStatus"));
            setUserEmail(individualUser.get("userEmail"));
            setUserRoleId(Integer.parseInt(individualUser.get("userRoleId")));
            setEnabled(Boolean.parseBoolean(individualUser.get("enabled")));
            setIsSuperAdmin(Boolean.parseBoolean(individualUser.get("isSuperAdmin")));
            setShouldLoanAutoApply(Boolean.parseBoolean(individualUser.get("shouldLoanAutoApply")));
            setAlternateUsername(individualUser.get("alternateUsername"));
        } catch (NullPointerException | NumberFormatException | TypeMismatchException e) {
            return false;
        }
        return true;
    }
}