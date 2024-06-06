package perfios.rbacs.Controller.LoginController;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;
import perfios.rbacs.Security.CustomAuthenticationProvider;
import perfios.rbacs.Security.UserDetailsServiceImplementation;

@CrossOrigin
@RestController
public class LoginSessionController {

    @Autowired
    private UserService userService;

    @Autowired
    UserDetailsServiceImplementation userDetailsServiceImplementation;

    @Autowired
    CustomAuthenticationProvider customAuthenticationProvider;

    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    private SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();


    @GetMapping("/login")
    public ModelAndView showLoginForm(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("id") != null)
            return new ModelAndView("homepage");

        model.addAttribute("loginPostOb", new LoginPostOb());
        return new ModelAndView("login");
    }



    @PostMapping(value = "/login")
    public ResponseEntity<?> loginCheck(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("loginPostOb") LoginPostOb loginPostOb){
        RbacsApplication.printString("post /login hit on pressing button");
        String userEmail = loginPostOb.getUserEmail();
        String userPassword = loginPostOb.getUserPassword();
        RbacsApplication.printString("api hit and " + userEmail);
        boolean areParamValid = true;
        StringBuilder validationErrors = new StringBuilder();
        validationErrors.append("STATUS : 400 BAD REQUEST(INVALID CREDENTIALS)\n");
        String emailPatternMatcherExpression = "^[\\w.-]+@[a-zA-Z_-]+?\\.[a-zA-Z]{2,3}$";
        if(userPassword.length()<8 || userPassword.length()>20) {
            validationErrors.append("Password should be between 8 to 20 characters\n");
            areParamValid = false;
        }
        if(!userEmail.matches(emailPatternMatcherExpression) ) {
            validationErrors.append("Email ID should be of form user@example.com");
            areParamValid = false;
        }
        if(areParamValid == false) RbacsApplication.printString("parameter valid found");
        if(!areParamValid) return ResponseEntity.badRequest().body(validationErrors);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userEmail, userPassword);

        Authentication authentication = customAuthenticationProvider.authenticate(authenticationToken);

        if(authentication == null) return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Bad credentials");

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        HttpSession session = request.getSession();
        RbacsApplication.printString(session.getId());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type","JSON");

        LoginResponse loginResponse = userService.getUserLogin();

        session.setAttribute("id",loginResponse.getUserId());

//        if(loginResponse.getUserPermissionId().contains("7")) session.setAttribute("viewAll",true);

        RbacsApplication.printString(loginResponse.toString());
        RbacsApplication.printString(authentication.toString());
        RbacsApplication.printString("--->" +SecurityContextHolder.getContext().getAuthentication().toString());

        return ResponseEntity.ok(loginResponse);

    }

    @PostMapping(value = "/loginpostman")
    public ResponseEntity<?> loginCheckPostman(@RequestBody LoginPostOb loginPostOb,HttpServletRequest request, HttpServletResponse response){
        RbacsApplication.printString("post /login hit on pressing button");
        String userEmail = loginPostOb.getUserEmail();
        String userPassword = loginPostOb.getUserPassword();
        RbacsApplication.printString("api hit and " + userEmail);
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
        if(areParamValid == false) RbacsApplication.printString("parameter valid found");
        if(!areParamValid) return ResponseEntity.badRequest().body(validationErrors);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userEmail, userPassword);

        Authentication authentication = customAuthenticationProvider.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        if(authentication == null) return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Bad credentials");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        HttpSession session = request.getSession();
        RbacsApplication.printString(session.getId());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type","JSON");
        LoginResponse loginResponse = userService.getUserLogin();
        session.setAttribute("id",loginResponse.getUserId());
        if(loginResponse.getUserPermissionId().contains("7")) session.setAttribute("viewAll",true);
        RbacsApplication.printString(loginResponse.toString());
        RbacsApplication.printString(authentication.toString());
        RbacsApplication.printString("--->" +SecurityContextHolder.getContext().getAuthentication().toString());
        return ResponseEntity.ok(loginResponse);

    }

    @GetMapping("/logout")
    public String performLogout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        this.logoutHandler.logout(request, response, authentication);
        HttpSession session = request.getSession();
        session.removeAttribute("id");
        session.removeAttribute("viewAll");
        return "logout successful";
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
