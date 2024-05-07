package perfios.rbacs.Repository.UserRepository;

import org.springframework.stereotype.Service;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;

import java.util.List;

@Service
public interface UserService {
    List<UserDashboard> getAllUserDashboard();
    List<User> getAllUsers();
    String addUser(User user);
    String updateUser(User user,int id);
    String deleteUser(int id);
    String unassignUserRole(int user_id, int role_id);
    String addNewRoleToExistingUser(int user_id, int role_id);
    String updateUser2(User user, int id);
    User getParticularUserById(int id);
    String addUser2(User user);
}
