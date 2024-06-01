package perfios.rbacs.JwtToken;

import ch.qos.logback.core.joran.sanity.Pair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.KeyPair;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import perfios.rbacs.Model.LoginPost.LoginPostOb;
import perfios.rbacs.Model.LoginResponse.LoginResponse;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenService {

    @Autowired
    UserService userService;

    private final static String secretKey = "070C92AE5948C694C04C7E28419017612402B78E9AAB0C561EBE8DC918BEDE7311184A136A8E4DCFD9E4C3F59855E9EC7191A8BC4767750E12107FCB7D97F1FB";
    private static final long VALIDITY = TimeUnit.MINUTES.toMillis(30); //specify minutes for which token should be valid
    public String generateJwtToken(LoginResponse loginResponse){
        //format for subject of jwt token is designed in such way that it contains userid, authorities and viewEach permission.
        //harsh{id*1102}it{granted_authorities}bajaj{1 or 0}
        //for ex, if a user has id 55 nd permissions 1,3,4 and can view all user's details then,
        //the subject for jwt token for that particular user is harsh(55*1102)it(134)bajaj1
        //ie, jwtToken.Subject = harsh60610it134bajaj1
        //i am using id, because it is easy to retrive a user from db using its id.
        //permission to viewEach is stored in token bcz same endpoint is used for this purpose.
        //so needed something to diffrentiate between viewEach and viewSelf req on same endpoint.
        //--UPDATE--> now end points to viewEach and viewSelf are changed.
        String jwtToken = Jwts.builder()
                .subject(subjectBuilder(loginResponse.getUserId(), loginResponse.getUserPermissionId()))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(VALIDITY)))
                .signWith(generateKey())
                .compact();
        return jwtToken;
    }

    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String subjectBuilder(int userId, Set<String> userPermissions){
        StringBuilder subject = new StringBuilder();
        subject.append("harsh")
                .append(userId*1102)
                .append("it");
        String userPermissionString = "";
        Boolean viewAllPermission = false;
        for(String permission : userPermissions) {
            if(permission.equals("7")) viewAllPermission = true;
            userPermissionString+=permission;
        }
                subject.append(userPermissionString)
                        .append("bajaj");
        if(viewAllPermission) subject.append("1");
        else subject.append("0");
        return subject.toString();

    }


    private List<GrantedAuthority> extractAuthoritiesFromSubject(String subject){
        String reverseAuthority = extractAuthoritiesStringFromSubject(subject);
        List<GrantedAuthority> authorities = new ArrayList<>();
        int i = reverseAuthority.length()-1;
        while(i>=0){
            RbacsApplication.printString("inside this while and " + reverseAuthority.charAt(i) );
            authorities.add(new SimpleGrantedAuthority(String.valueOf(reverseAuthority.charAt(i))));
            i--;
        }
        RbacsApplication.printString("printint authoritires inside service = " + authorities.toString());
        return authorities;
    }

    public boolean checkViewAllAuthorityFromToken(String jwtToken) {
        String subject = extractSubjeectFromJwtToken(jwtToken);
        RbacsApplication.printString(String.valueOf(subject.charAt(subject.length())));
        if(subject.charAt(subject.length()) == '1') return  true;
        else return false;
    }


    public int extractUserIdFromJwtToken(String jwtToken){
        RbacsApplication.printString("inside extract id from jwt ");
        String subject = extractSubjeectFromJwtToken(jwtToken);
        RbacsApplication.printString("subject = " + subject);
        if(subject == null) return -1;
        int i=5;
        StringBuilder id = new StringBuilder();
        while(subject.charAt(i) != 'i') id.append(subject.charAt(i++));
        RbacsApplication.printString(id.toString());
        int userId = Integer.valueOf(id.toString())/1102;
        return userId;
    }

    public String extractAuthoritiesStringFromSubject(String subject){
        if(subject == null) return null;
        int i = subject.length()-7;
        StringBuilder reversedAuthority = new StringBuilder();
        while(subject.charAt(i) != 't') reversedAuthority.append(subject.charAt(i--));
        RbacsApplication.printString("printing reverseauthority srting = " + reversedAuthority.toString());
        return reversedAuthority.toString();

    }
    public List <GrantedAuthority>  getAllUserAuthorities(String jwtToken){
        String subject = extractSubjeectFromJwtToken(jwtToken);
        List <GrantedAuthority> authorities = extractAuthoritiesFromSubject(subject);
        return authorities;
    }

    private String extractSubjeectFromJwtToken(String jwtToken){
        Claims claims = extractClaimsFromJwt(jwtToken);
        return claims.getSubject();
    }

    public boolean checkValidityOfJwtToken(String jwtToken){
        Claims claims = extractClaimsFromJwt(jwtToken);
        if(claims == null) {
            RbacsApplication.printString("--------------------------claims are null");
            return false;
        }
        else return claims.getExpiration().after(Date.from(Instant.now()));
    }



    private Claims extractClaimsFromJwt(String jwtToken){
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
        }catch (ExpiredJwtException e){
            return null;
        }

        return claims;
    }


}
