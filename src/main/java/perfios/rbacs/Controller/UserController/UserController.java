package perfios.rbacs.Controller.UserController;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import perfios.rbacs.Model.LoginDetails.LoginDetails;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.util.List;
import java.util.SimpleTimeZone;


@CrossOrigin
@RestController
@Validated
public class UserController {

    @Autowired
    private UserService userService;


    //this is sample functin to check
    @RequestMapping("userhome")
    public ResponseEntity<String> userhome() {
        return ResponseEntity.ok("user controller working af");
    }

    //this is for user dashboard (user_id, user email, user role)
    @GetMapping("user/dashboard")
    public ResponseEntity<?> getAllUsersDashboard() {
        List<UserDashboard> userDashboardList = userService.getAllUserDashboard();
        if(userDashboardList == null) return ResponseEntity.badRequest().body("You don't have access to all Users");
        return ResponseEntity.ok(userDashboardList);
    }


    @GetMapping("/login")
    public ResponseEntity<?> loginCheck(@RequestParam String userEmail, @RequestParam String userPassword ){
        boolean areParamValid = true;
        StringBuilder validationErrors = new StringBuilder();
        validationErrors.append("STATUS : 400 BAD REQUEST(INVALID CREDENTIALS)\n");
        String regexExpression = "^[\\w.-]+@[a-zA-Z_-]+?\\.[a-zA-Z]{2,3}$";
        if(userPassword.length()<6 || userPassword.length()>20) {
            validationErrors.append("Password should be between 6 to 20 characters\n");
            areParamValid = false;
        }
        if(!userEmail.matches(regexExpression) ) {
            validationErrors.append("Email ID should be of form user@example.com");
            areParamValid = false;
        }
        if(!areParamValid) return ResponseEntity.badRequest().body(validationErrors);
        LoginDetails loginDetails = userService.loginCheck(userEmail, userPassword);
        if(loginDetails == null) return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Already logged in. Logout first");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type","JSON");
        return ResponseEntity.ok(loginDetails);
    }




    @GetMapping("/logot")
    public Boolean logout(){
        return userService.logout();
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
        User user = userService.getParticularUserById(id);
        if(user == null) return ResponseEntity.badRequest().body("THIS REQUEST DOES NOT EXIST");
        return ResponseEntity.ok(user);
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
