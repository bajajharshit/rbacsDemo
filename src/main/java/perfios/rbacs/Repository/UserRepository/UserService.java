package perfios.rbacs.Repository.UserRepository;


import org.springframework.stereotype.Service;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.Model.LoginResponse.LoginResponse2;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.Model.Users.UserSearch;

import java.util.HashMap;
import java.util.List;

@Service
public interface UserService {


    List<UserDashboard> getAllUserDashboard();
    List<User> getAllUsers();
    String deleteUser(int id);
    String updateUser(User user, int id);
    User getParticularUserById(int id);
    Boolean checkEmailAlreadyExist(String emailId);
    String addNewUser(User user);
    LoginResponse loadUserByEmailId(String emailId);
    LoginResponse getUserLogin();
    LoginPostOb fetchUserDetailFromUserId(int userId);
    LoginResponse2 fetchUserDetailsFromUserId2(int userId , String userEmail);
    String getRoleName(int roleId);
    void fillRoleDetails();
    String getRoleIdFromRole(String roleName);
    LoginResponse2 loadUserByEmailId2(String emailId);
    void printRoleDetails();
    int getVerifiedUserId() ;
    void resetVerifiedUserId();
    List<User> findUserByDifferentFeilds(UserSearch userSearch);
    List<UserDashboard> dashboardFindUserByDifferentFeilds(UserSearch userSearch);
}
