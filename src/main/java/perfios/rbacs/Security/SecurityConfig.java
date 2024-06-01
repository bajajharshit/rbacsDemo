package perfios.rbacs.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.web.bind.annotation.RequestBody;
import perfios.rbacs.JwtToken.JwtAuthenticationFilter;
import perfios.rbacs.RbacsApplication;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImplementation myUserDetailsService;

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;


    @Bean
    public UserDetailsService userDetailsService(){
        return myUserDetailsService;
    }

    HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES));



    @Bean
    public AuthenticationProvider authenticationProvider(){
        RbacsApplication.printString("inside personal authentication service");
        return customAuthenticationProvider;
    }

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RbacsApplication.printString("inside security filter chain");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                                .requestMatchers(HttpMethod.POST,"loginpostman").permitAll()
                                .requestMatchers(HttpMethod.GET, "/login").permitAll()
                                .requestMatchers(HttpMethod.GET, "/homepage").permitAll()
                                .requestMatchers(HttpMethod.GET, "/userhome").permitAll()
                                .requestMatchers(HttpMethod.GET,"/loginjwt").permitAll()
                                .requestMatchers(HttpMethod.POST,"/loginjwt").permitAll()
                                .requestMatchers(HttpMethod.GET,"/logoutjwt").permitAll()
                                .requestMatchers(HttpMethod.GET,"/checking").permitAll()
                                .requestMatchers(HttpMethod.POST, "/user").hasAuthority("1")
                                .requestMatchers(HttpMethod.GET, "/user/dashboard").hasAuthority("6")
                                .requestMatchers(HttpMethod.POST, "/user/{id}").hasAuthority("3")
                                .requestMatchers(HttpMethod.GET, "/user/{id}").hasAuthority( "7")
                                .requestMatchers(HttpMethod.GET,"user/self/{userId}").hasAuthority("5")
                                .requestMatchers(HttpMethod.GET, "/user").hasAuthority("2")
                                .anyRequest().authenticated()
                ).logout(logout ->
                        logout
                                .permitAll()
                                .addLogoutHandler(clearSiteData)
                                .logoutUrl("/logout")
                                .clearAuthentication(true)
                                .invalidateHttpSession(true)
                                .logoutSuccessUrl("/homepage"))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                        .userDetailsService(userDetailsService());


        return http.build();
    }




}