package com.company.usermanagement.security.service;

import com.company.usermanagement.configuration.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties, SecretKey secretKey) {
        //        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtProperties.secret())
        );

    }

    @PostConstruct
    public void validateKey() {
        int bits = secretKey.getEncoded().length * 8;
        if (bits < 512) {
            throw new IllegalStateException(
                    "JWT secret troppo corta: " + bits + " bits. HS512 richiede >= 512 bits."
            );
        }
    }


    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 ora di validità

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Estrae lo username (il "subject") dal token JWT.
     * @param token il token JWT
     * @return lo username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Controlla se un token JWT è valido.
     * La validità è data da due condizioni:
     * 1. Lo username nel token corrisponde a quello dell'utente.
     * 2. Il token non è scaduto.
     * @param token il token JWT
     * @param userDetails i dettagli dell'utente da confrontare
     * @return true se il token è valido, altrimenti false
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    // --- METODI DI SUPPORTO (HELPERS) ---

    /**
     * Controlla se un token è scaduto.
     * @param token il token JWT
     * @return true se il token è scaduto, altrimenti false
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Estrae la data di scadenza dal token.
     * @param token il token JWT
     * @return la data di scadenza
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Metodo generico per estrarre una singola "claim" (informazione) dal token.
     * @param token il token JWT
     * @param claimsResolver una funzione che specifica quale claim estrarre
     * @return la claim richiesta
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Estrae tutte le "claims" dal token. Questo metodo è il cuore della decodifica.
     * Verifica anche che la firma del token sia valida usando la chiave segreta.
     * Se la firma non è valida o il token è malformato, lancia un'eccezione.
     * @param token il token JWT
     * @return l'oggetto Claims contenente tutte le informazioni
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser() // 1. Si inizia con .parser()
                .verifyWith((SecretKey) this.secretKey) // 2. Si imposta la chiave di verifica
                .build()
                .parseSignedClaims(token) // 3. Si esegue il parsing del token firmato
                .getPayload(); // 4. Si ottiene il corpo (le claims)
    }
}
