package perfios.rbacs.Controller.LoginController;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.JwtToken.JwtTokenService;
import perfios.rbacs.JwtToken.JwtTokenService2;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.Model.LoginResponse.LoginResponse2;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;
import perfios.rbacs.Security.CustomAuthenticationProvider;
import perfios.rbacs.Security.CustomAuthenticatorProvider2;
import perfios.rbacs.Security.SecurityConfig;

@CrossOrigin
@RestController
public class LoginJwtController {


    //if you want session less authentication and authorisation features, use loginjwt url
    //instead of /login. a token id will sent as response in http header on verification of user.



    @Autowired
    CustomAuthenticatorProvider2 customAuthenticationProvider;

    @Autowired
    JwtTokenService2 jwtTokenService;

    @Autowired
    UserService userService;


    //expired jwt token for testing:
    //eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJoYXJzaDUzOTk4aXQ1YmFqYWowIiwiaWF0IjoxNzE3NDAzNjg5LCJleHAiOjE3MTc0MDU0ODl9.e2Bpcix6YEVvTkzOzs5CTi3s0qSO7OyWY38UBO7UsvoTiSxFMNm3gXIt3L9d1HSSQDmGEOqoFDs-ax43g5i5SQ


    @GetMapping("/loginjwt")
    public ModelAndView showLoginForm(Model model, HttpServletRequest request) {
        RbacsApplication.printString("Inside login jwt");
        if (request.getHeader("Authorization") == null ){
            RbacsApplication.printString("Wrong token provided!!!!!!! redirecting to Login page");
            model.addAttribute("loginPostOb", new LoginPostOb());
            return new ModelAndView("loginjwt");
        }
            if( !jwtTokenService.checkValidityOfJwtToken(request.getHeader("Authorization").substring(7))) {
                RbacsApplication.printString("Invalid token !!!!!! redirecting to login page.");
            model.addAttribute("loginPostOb", new LoginPostOb());
            return new ModelAndView("loginjwt");
        }
        return new ModelAndView("homepage");
    }


    @PostMapping("/loginjwt")
    public ResponseEntity<?> loginResponseJwt( @ModelAttribute("loginPostOb") LoginPostOb loginPostOb) {
        RbacsApplication.printString("post /loginjwt hit on pressing button");
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




        int userId = userService.getVerifiedUserId();
        String jwtToken = jwtTokenService.generateJwtToken(userId,userEmail);
        RbacsApplication.printString("jwt token = " + jwtToken);
        // Create a new HttpHeaders object
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        RbacsApplication.printString(SecurityContextHolder.getContext().toString());
        return new ResponseEntity<>(authentication.getAuthorities(), headers, HttpStatus.OK);
    }


    @GetMapping("/logoutjwt")
    public String logoutUsingJwt() {
        return "logout succesfully";

    }

}

