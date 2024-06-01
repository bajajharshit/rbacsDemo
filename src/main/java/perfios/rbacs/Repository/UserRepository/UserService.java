package perfios.rbacs.Repository.UserRepository;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;

import java.util.List;
import java.util.Set;

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
}
