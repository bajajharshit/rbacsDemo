package perfios.rbacs.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserDetailsServiceImplementation implements UserDetailsService {


    @Autowired
    private UserService userService;

    UserDetails userDetails;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        RbacsApplication.printString("username received from form is "+username);
        LoginResponse loginResponse = userService.loadUserByEmailId(username);
        if (loginResponse == null) {
            return null;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String permissionId : loginResponse.getUserPermissionId()) {
            authorities.add(new SimpleGrantedAuthority(permissionId));
        }

        RbacsApplication.printString("code reach till this point");
        userDetails = new User(
                loginResponse.getUserEmailId(),
                loginResponse.getUserPassword(),
                authorities
        );
        return userDetails;
    }




}
