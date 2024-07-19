package perfios.rbacs.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import perfios.rbacs.JwtToken.AuthenticationFilterWithJwt;
import perfios.rbacs.JwtToken.JwtAuthenticationFilter;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {




    HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES));




    @Autowired
    private AuthenticationFilterWithJwt authenticationFilterWithJwt;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .logout(logout ->
                        logout
                                .addLogoutHandler(clearSiteData)
                                .logoutUrl("/logout")
                                .clearAuthentication(true)
                                .invalidateHttpSession(true)
                                .logoutSuccessUrl("/homepage"))
//                .exceptionHandling(exceptionHandling -> exceptionHandling
//                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/loginjwt")))
                .addFilterBefore(authenticationFilterWithJwt, UsernamePasswordAuthenticationFilter.class)
//                .authenticationProvider(authenticationProvider())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                        .userDetailsService(userDetailsService())
                ; //this semicolon represents end http

        return http.build();
    }




}