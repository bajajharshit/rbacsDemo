package perfios.rbacs.Controller.UserController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.Component.DataInitializer;
import perfios.rbacs.JwtToken.JwtTokenService;
import perfios.rbacs.JwtToken.JwtTokenService2;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.Model.Users.UserSearch;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.Redis.Access;
import perfios.rbacs.Repository.Redis.RedisDataService;
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

    @Autowired
    JwtTokenService2 jwtTokenService;

    @Autowired
    DataInitializer dataInitializer;

    @Autowired
    RedisDataService redisDataService;


    //this is a simple methord that extracts corresponding roleId for current Authenticated user
    //based on their role.
    //for example authenticated user has role as officer, so it will return roleId for it
    protected String roleIdFromRoleName(){
        if(SecurityContextHolder.getContext() == null) return null;
        String roleName = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        RbacsApplication.printString(roleName + "  " + roleName.length());
        roleName = roleName.substring(1,roleName.length()-1);
        RbacsApplication.printString(roleName + "  " + roleName.length());
        String roleId  = userService.getRoleIdFromRole(roleName);
        return roleId;
    }


    //this is sample functin to check
    @RequestMapping("userhome")
    public ResponseEntity<String> userhome() {
        return ResponseEntity.ok("user controller working and this link is not protected");
    }


    @PostMapping("/user-search")
    public List<User> getUsersBasedOnSearch(@RequestBody UserSearch userSearch){
        return userService.findUserByDifferentFeilds(userSearch);
    }


    @PostMapping("/user-dashboard-searched")
    public List<UserDashboard> getUsersDashboardBasedOnSearch(@RequestBody UserSearch userSearch){
        return userService.dashboardFindUserByDifferentFeilds(userSearch);
    }


    //this is for user dashboard (user_id, user email, user role)
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_BANK-OFFICER')")
    @GetMapping("user/dashboard")
    public ResponseEntity<?> getAllUsersDashboard() {
        String permission_type = "dashboard";  //by default
        String permissionId = redisDataService.getPermissionId(permission_type);
        String roleId = roleIdFromRoleName();
        if(roleId == null) return ResponseEntity.badRequest().body("Page Not Available");
        Access access = redisDataService.getPermissionAccessFromRedis(roleId,permissionId);
        if(access == null) return ResponseEntity.badRequest().body("Page Not Available");
        Boolean allow = false;
        if(access.isCanView()) allow = true;
        if(allow == false) return ResponseEntity.badRequest().body("You don't have access to this page :(");
        List<UserDashboard> userDashboardList = userService.getAllUserDashboard();
        if(userDashboardList == null) return ResponseEntity.badRequest().body("You don't have access to all Users");
        return ResponseEntity.ok(userDashboardList);
    }




    @GetMapping("/homepage")
    public ModelAndView homepage(){
        return new ModelAndView("homepage");
    }


    //this methord receives a json of user model type and adds user into user_details table and user_to_role table correspondingly
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
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



    //this methord is to see all the users existing.
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR')")
    @GetMapping("user")
    public ResponseEntity<?> getAllUsers(){
        String permission_type = "all_users";
        String  permission_id= redisDataService.getPermissionId(permission_type);
        String roleId = roleIdFromRoleName();
        Access access = redisDataService.getPermissionAccessFromRedis(roleId,permission_id);
        if(access.isCanView() == false) return ResponseEntity.unprocessableEntity().body("403 FORBIDDEN : YOU ARE NOT AUTHORISED TO VIEW THIS");
        List<User> userList = userService.getAllUsers();
        if(userList == null) return ResponseEntity.badRequest().body("BAD REQUEST 400: THIS PAGE DOESNOT EXIST");
        return ResponseEntity.ok(userList);
    }


    //this methord deletes the user with provided ID, it will delete user from user_details and from user_to_role table
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("user/{id}")
    public String deleteUser(@PathVariable int id){
        return userService.deleteUser(id);
    }


    //this methord returns user details for a particular user, pass user_id in URL's end point
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_BANK-OFFICER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> particularUserDetails(@PathVariable int userId, HttpServletRequest request){
        String permissionType = "user_details";
        String permissionId = redisDataService.getPermissionId(permissionType);
        String roleId = roleIdFromRoleName();
        Access access = redisDataService.getPermissionAccessFromRedis(roleId,permissionId);
        if(access.isCanView() == false) return ResponseEntity.unprocessableEntity().body("403 FORBIDDEN : YOU ARE NOT AUTHORISED TO VIEW THIS");
        if(roleId.equals('1') == false) {
            String jwt = request.getHeader("Authorization").substring(7);
            int userIdFromRequest = jwtTokenService.extractUserIdFromJwt(jwt);
            if(userIdFromRequest != userId) return ResponseEntity.unprocessableEntity().body("403 FORBIDDEN : YOU ARE NOT AUTHORISED TO VIEW THIS");
        }
        User user = userService.getParticularUserById(userId);
        if(user == null) return ResponseEntity.badRequest().body("THIS REQUEST DOES NOT EXIST");
        return ResponseEntity.ok(user);
    }


    //this methord is for updating a user details
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @PutMapping("user/{id}")
    public ResponseEntity<String> updateUser(@PathVariable int id, @Valid @RequestBody User user, BindingResult bindingResult) {
        String permissionType = "user_details";
        String permissionId = redisDataService.getPermissionId(permissionType);
        String roleId = roleIdFromRoleName();
        Access access = redisDataService.getPermissionAccessFromRedis(roleId,permissionId);
        if(access.isCanEdit() == false) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("YOU ARE UNAUTHORIZED TO PERFORM THIS OPERATION");
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



    //this function catches all the validation errors based on validation done in respective models.
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



    //this function is to check current authenticated user for testing purpose.
    @GetMapping("/checking")
    public void check(){
        RbacsApplication.printString("-----------this are session permissions----------- ");
        RbacsApplication.printString(SecurityContextHolder.getContext().getAuthentication().toString());
        RbacsApplication.printString(String.valueOf(userService.getVerifiedUserId()));
    }



}
