package perfios.rbacs.Controller.LoginController;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
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

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController

public class LoginJwtController {


    //if you want session less authentication and authorisation features, use loginjwt url
    //instead of /login. a token will sent as response in http header on verification of user.



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
//        RbacsApplication.printString("Inside login jwt");
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
//admin = eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwOTU5NCRtb2MuZWxwbWF4ZUBlbm9yZXN1IiwiaWF0IjoxNzE5NDAzNjE2LCJleHAiOjE4OTk0MDM2MTZ9.PKmtcSSmvcg4fZp-UaEd2ZbPS3d9FhzyBhHYPlazxIuvqAGoT2sduvUVnzd6dqaCJmZSkZ58J-nezol6zKm4uQ
//BO = eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2Njc2NTIkbW9jLmVscG1heGVAdHRvY3NhaWxlbWEiLCJpYXQiOjE3MTk0MDM2NzYsImV4cCI6MTg5OTQwMzY3Nn0.e0doQol5JHskG_GKc6rp0Xm4X_Pva9plxTJGxYS7YOK34leAuo5H5GkXKUFkohx_2dLBusJrKI5qInVm_yEQfA



    @PostMapping("/loginjwt")
    public ResponseEntity<?> loginResponseJwt( @RequestBody LoginPostOb loginPostOb) {
//        RbacsApplication.printString("post /loginjwt hit on pressing button");
        String userEmail = loginPostOb.getUserEmail();
        String userPassword = loginPostOb.getUserPassword();
//        RbacsApplication.printString("api hit and " + userEmail);
        boolean areParamValid = true;
        StringBuilder validationErrors = new StringBuilder();
        validationErrors.append("STATUS : 400 BAD REQUEST(INVALID CREDENTIALS)\n");
        String emailPatternMatcherExpression = "^[\\w.-]+@[a-zA-Z_-]+?\\.[a-zA-Z]{2,3}$";
        if(userPassword == null || userPassword.isEmpty() || userPassword.isBlank()){
            validationErrors.append("Password should be between 8 to 20 characters\n");
            areParamValid = false;
        }else if( userPassword.length()<8 || userPassword.length()>20) {
            validationErrors.append("Password should be between 8 to 20 characters\n");
            areParamValid = false;
        }
        if(userEmail == null || userEmail.isBlank() || userEmail.isEmpty()){
            validationErrors.append("Email ID should be of form user@example.com");
            areParamValid = false;
        }else if (!userEmail.matches(emailPatternMatcherExpression)) {
            validationErrors.append("Email ID should be of form user@example.com");
            areParamValid = false;
        }
        if(areParamValid == false) RbacsApplication.printString("parameter Invalid found");
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
//        RbacsApplication.printString(SecurityContextHolder.getContext().toString());
        List<Object> responseList = new ArrayList<>();
        responseList.add(authentication.getAuthorities());
        responseList.add(jwtToken);
        return new ResponseEntity<>(responseList, headers, HttpStatus.OK);
    }


    @GetMapping("/logoutjwt")
    public String logoutUsingJwt() {
        return "logout succesfully";

    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<String> handleHttpMessageConversionException(HttpMessageConversionException ex) {
        return ResponseEntity.badRequest().body("INVALID DATA ENTERED, TRY AGAIN WITH CORRECT DATA");
    }

}

