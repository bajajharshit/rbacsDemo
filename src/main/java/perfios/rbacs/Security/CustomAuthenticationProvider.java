package perfios.rbacs.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import perfios.rbacs.RbacsApplication;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsServiceImplementation userDetailsServiceImplementation;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        RbacsApplication.printString("-inside custom authentication "+authentication.toString());
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = userDetailsServiceImplementation.loadUserByUsername(username);
        if (userDetails != null && userDetails.getPassword().equals(password)) {
            RbacsApplication.printString("user verified and = "+userDetails.toString());
            return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        } else {
            RbacsApplication.printString("authentication failed/n"+"entered pass = "+password +"usermaybedisable" );
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
