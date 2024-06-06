package perfios.rbacs.JwtToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse2;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthenticationFilterWithJwt extends OncePerRequestFilter {


    @Autowired
    JwtTokenService2 jwtTokenService;

    @Autowired
    UserService userService;
    UserDetails userDetails;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        if(request.getRequestURI().startsWith("/loginjwt")) {
            RbacsApplication.printString("here");
            filterChain.doFilter(request,response);
            return;
        }

        if(request.getRequestURI().startsWith("/checking")){
            filterChain.doFilter(request,response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        String jwt = authHeader.substring(7);
        RbacsApplication.printString("jwt token = " + jwt);

        if(!jwtTokenService.checkValidityOfJwtToken(jwt)) {
            RbacsApplication.printString("Token is Invalid !!!!!!!!!!!!");
            RbacsApplication.printString(SecurityContextHolder.getContext().toString());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request,response);
            return;
        }

        int userId = jwtTokenService.extractUserIdFromJwt(jwt);
        String userEmailId = jwtTokenService.extractUserEmailIdFromJwt(jwt);

        RbacsApplication.printString("Securitycontextholder = "+SecurityContextHolder.getContext());
        RbacsApplication.printString("userid = " + userId + " email id = " + userEmailId);

        if(userId != -1 && userEmailId != null && SecurityContextHolder.getContext().getAuthentication() == null){
            LoginResponse2 loginResponse2 = userService.fetchUserDetailsFromUserId2(userId ,  userEmailId);
            if(loginResponse2 == null) {
                return;
            }
            userService.resetVerifiedUserId();

            List<GrantedAuthority> authorities = new ArrayList<>();
            String role = "ROLE_" + loginResponse2.getRoleName().toUpperCase();
            RbacsApplication.printString("role_name" + role);
            authorities.add(new SimpleGrantedAuthority(role));
            userDetails = new User(
                    loginResponse2.getUserEmail(),
                    loginResponse2.getUserPassword(),
                    authorities
            );

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, loginResponse2.getUserPassword(), userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            RbacsApplication.printString("printing securitycontextholder after verification of jwt = \n" + SecurityContextHolder.getContext());
            filterChain.doFilter(request,response);
            return;

        }
        filterChain.doFilter(request,response);

    }
}
