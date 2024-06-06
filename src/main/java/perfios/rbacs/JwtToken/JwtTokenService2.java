package perfios.rbacs.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import perfios.rbacs.Model.LoginResponse.LoginResponse2;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenService2 {

    @Autowired
    UserService userService;

    private final static String secretKey = "070C92AE5948C694C04C7E28419017612402B78E9AAB0C561EBE8DC918BEDE7311184A136A8E4DCFD9E4C3F59855E9EC7191A8BC4767750E12107FCB7D97F1FB";
    private static final long VALIDITY = TimeUnit.MINUTES.toMillis(30); //specify minutes for which token should be valid
    public String generateJwtToken(int userId, String username){
        //format for subject of jwt token is designed in such way that it contains userid, authorities and viewEach permission.
        //reverseOfEmail{userId*1102}
        String jwtToken = Jwts.builder()
                .subject(subjectBuilder(userId, username))
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

    public String subjectBuilder(int userId, String username){
        //user id  = 4, id*1102 = 4408
        //user email = harshit@perfios.com
        //subject = 8044$moc.soifrefs@tihsrah
        StringBuilder subject = new StringBuilder();
        subject.append(username);
        subject.append("$");
        subject.append(userId*1102);
        subject.reverse();
        return subject.toString();
    }

    public int extractUserIdFromJwt(String jwtToken){
        String subject = extractSubjeectFromJwtToken(jwtToken);
        if(subject == null) return -1;
        int i = 0;
        StringBuilder userIdString = new StringBuilder();
        while(subject.charAt(i) != '$') userIdString.append(subject.charAt(i++));
        userIdString.reverse();
        int userId = Integer.valueOf(userIdString.toString())/1102;
        return userId;
    }

    public String extractUserEmailIdFromJwt(String jwtToken){
        String subject = extractSubjeectFromJwtToken(jwtToken);
        if(subject == null) return null;
        String emailId = "";
        int i = subject.length()-1;
        while(subject.charAt(i) != '$') emailId+=subject.charAt(i--);
        return emailId;
    }



    private String extractSubjeectFromJwtToken(String jwtToken){
        Claims claims = extractClaimsFromJwt(jwtToken);
        if(claims == null) return null;
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
        }catch (ExpiredJwtException e ){
            System.err.println(e.getMessage());
            return null;
        }catch (SignatureException f){
            return null;
        }catch (JwtException e){
            System.err.println(e.getMessage());
        }

        return claims;
    }


}
