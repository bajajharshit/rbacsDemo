package perfios.rbacs.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import perfios.rbacs.Model.LoginResponse.LoginResponse2;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserDetailsServiceImplementation2 implements UserDetailsService {

    UserDetails userDetails;


    @Autowired
    UserService userService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginResponse2 loginResponse= userService.loadUserByEmailId2(username);
        if (loginResponse == null) {
            return null;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleToAdd = "ROLE_" + loginResponse.getRoleName().toUpperCase();
        authorities.add(new SimpleGrantedAuthority(roleToAdd));

        userDetails = new User(
                loginResponse.getUserEmail(),
                loginResponse.getUserPassword(),
                authorities
        );

        return userDetails;

    }
}
