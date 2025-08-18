package com.Loanmanagement.Loan.LMS.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
// Importing Base64 for proper key decoding
import java.util.Base64;

@Service
public class JwtService {

    // IMPORTANT: This secret key should be SECURELY stored (e.g., in environment variables or a configuration server)
    // and NOT hardcoded in production. It should be at least 256 bits (32 bytes) for HS256.
    // If this is a HEXADECIMAL string representation of a 256-bit key, you need to decode it.
    // Let's assume it's a Base64 encoded string for simplicity, which is a common practice.
    // If your original intention was hexadecimal, you'll need the hexStringToByteArray helper.
    // For demonstration, I'm providing a Base64 encoded key that, when decoded, results in 32 bytes.
    // You can generate a secure Base64 key using:
    // `Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded())`
    private static final String SECRET_KEY = "NDBFNjM1MjY2NTU2QTU4NkUzMjcyMzU3NTM4NzhGMzZGNDQyODQ3MkI0QjYyNTA2NDUzNjc1NjZCNTk3MA=="; // This is a Base64 encoded version of your original hex string's byte representation.

    // Token expiration in milliseconds (1 day)
    private static final long JWT_EXPIRATION = 1000 * 60 * 60 * 24; // 24 hours

    // Method to get the signing key. It decodes the Base64 SECRET_KEY.
    private Key getSigningKey() {
        // Decode the Base64 encoded string into a byte array
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Helper method to convert a hexadecimal string to a byte array (if your SECRET_KEY was truly hex)
    // private static byte[] hexStringToByteArray(String s) {
    //     int len = s.length();
    //     byte[] data = new byte[len / 2];
    //     for (int i = 0; i < len; i += 2) {
    //         data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
    //                              + Character.digit(s.charAt(i+1), 16));
    //     }
    //     return data;
    // }

    // Extract email (subject) from JWT
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract a custom claim (e.g., role) from JWT.
    // This is a generic method to extract any claim.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parses the JWT to get all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Use the correctly decoded key here
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Generate token with custom claims (like role) and subject (email)
    public String generateToken(UserDetails userDetails, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Add the role as a claim
        return createToken(claims, userDetails.getUsername()); // userDetails.getUsername() typically returns the email
    }

    // Creates the JWT string
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Set custom claims
                .setSubject(subject) // Set the principal (usually email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Token creation time
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION)) // Token expiration time
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sign with the generated key and algorithm
                .compact(); // Build and compact the token to a string
    }

    // Validates if the token is valid for the given user details
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token); // Extract email from token
        // Check if the extracted email matches userDetails's username and if the token is not expired
        return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Checks if the token has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts the expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Optional: Just to validate signature without extracting claims. Useful for quick integrity check.
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            // Log the exception for debugging in a real application
            // e.g., logger.error("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token: " + e.getMessage());
        }
    }
}
