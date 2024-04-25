// IMPORTANT: the following dependencies must be added to file pom.xml
// <dependency>
//      <groupId>io.jsonwebtoken</groupId>
//      <artifactId>jjwt-api</artifactId>
//      <version>0.11.2</version>
// </dependency>
// <dependency>
//      <groupId>io.jsonwebtoken</groupId>
//      <artifactId>jjwt-impl</artifactId>
//      <version>0.11.2</version>
//      <scope>runtime</scope>
// </dependency>
// <dependency>
//      <groupId>io.jsonwebtoken</groupId>
//      <artifactId>jjwt-jackson</artifactId>
//      <version>0.11.2</version>
//      <scope>runtime</scope>
// </dependency>

package edu.charlotte.cs.itcs3160;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {
    private String secretKey = "secretsecretsecretsecretsecretsesecretsecretsecretsecretsecretsesecretsecretsecretsecretsecretse";

    private long expirationTime=3600000;

    private static final Logger logger = LoggerFactory.getLogger(DemoProj.class);

    // Generate JWT token
    public String generateTokenJWT(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // Validate JWT token
    public boolean validateTokenJWT(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            logger.error("ERROR:" + e);
            return false;
        }
    }

    //return User
    public String getTokenUsername(String token) {
        
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("ERROR:" + e);
            return null;
        }
    }
}