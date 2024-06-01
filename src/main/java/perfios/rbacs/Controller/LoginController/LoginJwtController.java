package perfios.rbacs.Controller.LoginController;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.JwtToken.JwtTokenService;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;
import perfios.rbacs.Security.SecurityConfig;

@CrossOrigin
@RestController
public class LoginJwtController {


    //if you want session less authentication and authorisation features, use loginjwt url
    //instead of /login. a token id will sent as response in http header on verification of user.



    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    UserService userService;


    @GetMapping("/loginjwt")
    public ModelAndView showLoginForm(Model model, HttpServletRequest request) {
        if (request.getHeader("Authorization") == null || !jwtTokenService.checkValidityOfJwtToken(request.getHeader("Authorization").substring(7))) {
//            RbacsApplication.printString("authorization is improper");
//            RbacsApplication.printString(request.getHeader("Authorization"));
            model.addAttribute("loginPostOb", new LoginPostOb());
            return new ModelAndView("loginjwt");
        }
        return new ModelAndView("homepage");
    }


    @PostMapping("/loginjwt")
    public ResponseEntity<?> loginResponseJwt(HttpServletResponse response, HttpServletRequest request, @ModelAttribute("loginPostOb") LoginPostOb loginPostOb) {
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
        LoginResponse loginResponse = userService.loadUserByEmailId(userEmail);
        if(loginResponse == null) ResponseEntity.badRequest().body("Bad credentials");
        String jwtToken = jwtTokenService.generateJwtToken(loginResponse);
        RbacsApplication.printString("jwt token = " + jwtToken);
        // Create a new HttpHeaders object
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        RbacsApplication.printString(SecurityContextHolder.getContext().toString());
        return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
    }


    @GetMapping("/logoutjwt")
    public String logoutUsingJwt() {
        return "logout succesfully";

    }

}

