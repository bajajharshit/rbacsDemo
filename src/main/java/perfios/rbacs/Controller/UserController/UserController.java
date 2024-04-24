package perfios.rbacs.Controller.UserController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private  UserService  userService;


    @RequestMapping("userhome")
    public String userhome(){
        return "user controller working af";
    }

    @GetMapping("user/dashboard")
    public List<UserDashboard> getAllUsersDashboard(){
        return  userService.getAllUserDashboard();
    }

    @PostMapping("user")
    public String addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    @GetMapping("user")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @DeleteMapping("user/{id}")
    public String deleteUser(@PathVariable int id){
        return userService.deleteUser(id);
    }

    @DeleteMapping("user/{user_id}/role/{role_id}")
    public String deleteUserRole(@PathVariable int user_id, @PathVariable int role_id){
        return userService.unassignUserRole(user_id,role_id);
    }

    @PutMapping("user/{id}")
    public String updateUser(@PathVariable int id , @RequestBody User user){
        return userService.updateUser(user,id);
    }

    @PostMapping("user/role")
    public String addNewRoleToExistingUser(@RequestParam int user_id,@RequestParam int role_id){
        return userService.addNewRoleToExistingUser(user_id,role_id);
    }

}
