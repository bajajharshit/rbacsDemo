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


    //this is sample functin to check
    @RequestMapping("userhome")
    public String userhome(){
        return "user controller working af";
    }

    //this is for user dashboard (user_id, user email, user role)
    @GetMapping("user/dashboard")
    public List<UserDashboard> getAllUsersDashboard(){
        return  userService.getAllUserDashboard();
    }

    //this methord receives a json of user model type and adds user into user_details table and user_to_role table correspondingly
    @PostMapping("user")
    public String addUser(@RequestBody User user){
        return userService.addUser(user);
    }

    //this methord is to get user's list[user_id, remaining details and role_id]
    @GetMapping("user")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    //this methord deletes the user with provided ID, it will delete user from user_details and from user_to_role table
    @DeleteMapping("user/{id}")
    public String deleteUser(@PathVariable int id){
        return userService.deleteUser(id);
    }


    //this methord returns user details for a particular user, pass user_id in URL's end point
    @GetMapping("user/{id}")
    public User getParticularUserById(@PathVariable int id){
        return userService.getParticularUserById(id);
    }

    //this methord is for unassinging a role to a user
    @DeleteMapping("user/{user_id}/role/{role_id}")
    public String deleteUserRole(@PathVariable int user_id, @PathVariable int role_id){
        return userService.unassignUserRole(user_id,role_id);
    }

    //this methord is for updating a user, it does not update user's role. for that another methord is created.
    @PutMapping("user/{id}")
    public String updateUser(@PathVariable int id , @RequestBody User user){
        return userService.updateUser(user,id);
    }

    //this methord is for adding a new role to user.
    @PostMapping("user/role")
    public String addNewRoleToExistingUser(@RequestParam int user_id,@RequestParam int role_id){
        return userService.addNewRoleToExistingUser(user_id,role_id);
    }


    @PostMapping("/newuser")
    public String addNewUser(@RequestBody User user){
        return userService.addNewUser(user);
    }


    @PutMapping("/upduser/{id}")
    public String updateUser2(@RequestBody User user, @PathVariable int id){
        return userService.updateUser2(user,id);
    }


}
