package perfios.rbacs.Controller.UserController;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.util.List;
import java.util.SimpleTimeZone;

@RestController
public class UserController {

    @Autowired
    private  UserService  userService;


    //this is sample functin to check
    @RequestMapping("userhome")
    public ResponseEntity<String> userhome(){
        return ResponseEntity.ok("user controller working af");
    }

    //this is for user dashboard (user_id, user email, user role)
    @GetMapping("user/dashboard")
    public List<UserDashboard> getAllUsersDashboard(){
        return  userService.getAllUserDashboard();
    }

    //this methord receives a json of user model type and adds user into user_details table and user_to_role table correspondingly
    @PostMapping("user")
    public ResponseEntity<String> addNewUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("400 BAD REQUEST\nthis request cannot be fullfilled");
            for(ObjectError error : bindingResult.getAllErrors()){
                errorMessage.append(error.getDefaultMessage());
                errorMessage.append(".\n");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }

        String result = userService.addNewUser(user);
        return ResponseEntity.ok(result);
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
    public ResponseEntity<String> updateUser2(@PathVariable int id, @Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("400 BAD REQUEST\nThis request cannot be fulfilled due to validation errors:\n");
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessage.append(error.getDefaultMessage());
                errorMessage.append(".\n");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }

        String result = userService.updateUser2(user, id);
        return ResponseEntity.ok(result);
    }


    //this methord is for adding a new role to user.
    @PostMapping("user/role")
    public String addNewRoleToExistingUser(@RequestParam int user_id,@RequestParam int role_id){
        return userService.addNewRoleToExistingUser(user_id,role_id);
    }



}
