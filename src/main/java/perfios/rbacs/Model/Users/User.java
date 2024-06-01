package perfios.rbacs.Model.Users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data

public class User {
    private int userId;

    @NotBlank(message = "FirstName cannot be blank")
    @Size(max = 50, message = "FirstName can't be more than 50 letters")
    private String userFirstName;

    @NotBlank(message =  "LastName cannot be blank")
    @Size(max = 30, message =  "LastName can't be nore than 30 letters")
    private String userLastName;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 6, message =  "password atleast 6 characters")
    private String  userPassword;

    @NotBlank(message = "Phone number can't be empty")
    @Pattern(regexp = "^\\d+$", message = "only digits are allowed in phone number")
    @Size(min = 10, max =  10, message = "number should be of 10 digits")
    private String userPhoneNumber;

    private String userStatus;

    @Email(message =  "Email ID should be valid")
    private String userEmail;

    @NotBlank
    private String userRoleName;
    private List<String> userRoleNameList = new ArrayList<>();

//    @NotBlank(message = "RoleId cannot be NULL")
    private int userRoleId;

    Boolean enabled;
    Boolean isSuperAdmin;
    Boolean shouldLoanAutoApply;

}