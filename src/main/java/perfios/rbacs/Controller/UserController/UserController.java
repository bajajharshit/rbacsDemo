package perfios.rbacs.Controller.UserController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.JwtToken.JwtTokenService;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;
import perfios.rbacs.Security.CustomAuthenticationProvider;
import perfios.rbacs.Security.UserDetailsServiceImplementation;

import java.util.List;


@CrossOrigin
@RestController
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    UserDetailsServiceImplementation userDetailsServiceImplementation;

    @Autowired
    CustomAuthenticationProvider customAuthenticationProvider;


    //this is sample functin to check
    @RequestMapping("userhome")
    public ResponseEntity<String> userhome() {
        return ResponseEntity.ok("user controller working and this link is not protected");
    }

    //this is for user dashboard (user_id, user email, user role)
    @GetMapping("user/dashboard")
    public ResponseEntity<?> getAllUsersDashboard() {
        List<UserDashboard> userDashboardList = userService.getAllUserDashboard();
        if(userDashboardList == null) return ResponseEntity.badRequest().body("You don't have access to all Users");
        return ResponseEntity.ok(userDashboardList);
    }



    @GetMapping("/checking")
    public void check(){
        RbacsApplication.printString("-----------this are session permissions----------- ");
        RbacsApplication.printString(SecurityContextHolder.getContext().toString());
    }


    @GetMapping("/homepage")
    public ModelAndView homepage(){
        return new ModelAndView("homepage");
    }


    //this methord receives a json of user model type and adds user into user_details table and user_to_role table correspondingly
    @PostMapping("user")
    public ResponseEntity<String> addNewUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Validation errors occurred:\n");
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessage.append("- ");
                errorMessage.append(error.getDefaultMessage());
                errorMessage.append("\n");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }

        String result = userService.addNewUser(user);
        return ResponseEntity.ok(result);
    }



    //this methord is to get user's list[user_id, remaining details and role_id]
    @GetMapping("user")
    public ResponseEntity<?> getAllUsers(){
        List<User> userList = userService.getAllUsers();
        if(userList == null) return ResponseEntity.badRequest().body("BAD REQUEST 400: THIS PAGE DOESNOT EXIST");
        return ResponseEntity.ok(userList);
    }

    //this methord deletes the user with provided ID, it will delete user from user_details and from user_to_role table
    @DeleteMapping("user/{id}")
    public String deleteUser(@PathVariable int id){
        return userService.deleteUser(id);
    }


    //this methord returns user details for a particular user, pass user_id in URL's end point
    @GetMapping("user/{id}")
    public ResponseEntity<?> getParticularUserById(@PathVariable int id){
        RbacsApplication.printString("inside view all controller");
        User user = userService.getParticularUserById(id);
        if(user == null) return ResponseEntity.badRequest().body("THIS REQUEST DOES NOT EXIST");
        return ResponseEntity.ok(user);
    }

    @GetMapping("user/self/{userId}")
    public ResponseEntity<?> getOnlySelfDetails(@PathVariable int userId){
        User user = userService.getParticularUserById(userId);
        if(user == null) return ResponseEntity.badRequest().body("THIS REQUEST DOES NOT EXIST");
        return ResponseEntity.ok(user);
    }


    //this methord is for unassinging a role to a user
    @DeleteMapping("user/{user_id}/role/{role_id}")
    public String deleteUserRole(@PathVariable int user_id, @PathVariable int role_id){
        return userService.unassignUserRole(user_id,role_id);
    }

    //this methord is for updating a user
    @PutMapping("user/{id}")
    public ResponseEntity<String> updateUser(@PathVariable int id, @Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("400 BAD REQUEST\nThis request cannot be fulfilled due to validation errors:\n");
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessage.append(error.getDefaultMessage());
                errorMessage.append(".\n");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }

        String result = userService.updateUser(user, id);
        return ResponseEntity.ok(result);
    }


    //this methord is for adding a new role to user.
    @PostMapping("user/role")
    public String addNewRoleToExistingUser(@RequestParam int user_id,@RequestParam int role_id){
        return userService.addNewRoleToExistingUser(user_id,role_id);
    }



    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(" STATUS: 400 BAD REQUEST (SOME INPUTS ARE INVALID)\n");
        ex.getConstraintViolations().forEach(violation -> {
            errorMessage.append("- ");
            errorMessage.append(violation.getMessage());
            errorMessage.append("\n");
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
    }



}
