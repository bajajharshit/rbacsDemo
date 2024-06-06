package perfios.rbacs.Repository.UserRepository;


import org.springframework.stereotype.Service;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.Model.LoginResponse.LoginResponse2;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;

import java.util.List;

@Service
public interface UserService {


    List<UserDashboard> getAllUserDashboard();
    List<User> getAllUsers();
    String deleteUser(int id);
    String unassignUserRole(int user_id, int role_id);
    String addNewRoleToExistingUser(int user_id, int role_id);
    String updateUser(User user, int id);
    User getParticularUserById(int id);
    Boolean checkEmailAlreadyExist(String emailId);
    String addNewUser(User user);
    LoginResponse loadUserByEmailId(String emailId);
    LoginResponse getUserLogin();
    LoginPostOb fetchUserDetailFromUserId(int userId);
    LoginResponse2 fetchUserDetailsFromUserId2(int userId , String userEmail);
    void fillAdminPermissions();
    void printAdminPermissionMap();
    String getRoleName(int roleId);
    String fillRoleDetails();
    boolean getPermission(String uuid, String type);
    LoginResponse2 loadUserByEmailId2(String emailId);
    void printRoleDetails();

    public int getVerifiedUserId() ;
    public void resetVerifiedUserId();
}
