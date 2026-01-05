package rmit.saintgiong.paymentservice.common.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rmit.saintgiong.paymentservice.common.config.JweConfig;
import rmit.saintgiong.paymentservice.common.exception.token.InvalidTokenException;
import rmit.saintgiong.paymentservice.common.exception.token.TokenExpiredException;
import rmit.saintgiong.shared.token.TokenClaimsDto;
import rmit.saintgiong.shared.type.Role;
import rmit.saintgiong.shared.type.TokenType;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class JweTokenService {

    private final JweConfig jweConfig;
    private final RsaKeyLoader keyLoader;
    private final TokenStorageService tokenStorageService;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception {
        this.publicKey = keyLoader.loadPublicKey();
        this.privateKey = keyLoader.loadPrivateKey();

        log.info("JWE Token Service initialized with issuer: {}", jweConfig.getIssuer());
        log.info("Access token TTL: {} seconds, Refresh token TTL: {} seconds",
                jweConfig.getAccessTokenTtlSeconds(), jweConfig.getRefreshTokenTtlSeconds());
    }

    /**
     * Validates an access token and returns the claims if valid.
     * Checks if the token is in the blocklist (revoked).
     *
     * @param accessToken The access token to validate
     * @return TokenClaimsDto containing the token claims
     */
    public TokenClaimsDto validateAccessToken(String accessToken) {
        TokenClaimsDto tokenClaimsDto = getTokenClaimsDtoDecryptedFromTokenString(accessToken);
        if (tokenClaimsDto.getType() != TokenType.ACCESS) {
            throw new InvalidTokenException("Invalid token type. Expected TokenType: ACCESS");
        }

        String accessTokenId = tokenClaimsDto.getJti();
        if (tokenStorageService.isAccessTokenBlocked(accessTokenId)) {
            throw new InvalidTokenException("Access token has been revoked.");
        }

        return tokenClaimsDto;
    }

    // Decrypts and validates a JWE token.
    public TokenClaimsDto getTokenClaimsDtoDecryptedFromTokenString(String jweString) {
        try {
            JWEObject jweObject = validateAndConvertTokenStringToJweObject(jweString, null);
            Map<String, Object> tokenPayload = jweObject.getPayload().toJSONObject();

            Number iat = (Number) jweObject.getHeader().getCustomParam("iat");
            Number exp = (Number) jweObject.getHeader().getCustomParam("exp");
            String iss = jweObject.getHeader().getIssuer();

            return TokenClaimsDto.builder()
                    .sub(UUID.fromString((String) tokenPayload.get("sub")))
                    .email((String) tokenPayload.get("email"))
                    .role(Role.valueOf((String) tokenPayload.get("role")))
                    .type(TokenType.valueOf((String) tokenPayload.get("type")))
                    .jti((String) tokenPayload.get("jti"))
                    .iat(iat != null ? iat.longValue() : 0)
                    .exp(exp != null ? exp.longValue() : 0)
                    .iss(iss)
                    .build();

        } catch (TokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            throw new InvalidTokenException("Invalid or malformed token");
        }
    }

    private JWEObject validateAndConvertTokenStringToJweObject(String jweString, TokenType type)
            throws JOSEException, ParseException {
        JWEObject jweObject = JWEObject.parse(jweString);
        jweObject.decrypt(new RSADecrypter(privateKey));
        Map<String, Object> tokenPayload = jweObject.getPayload().toJSONObject();

        Number exp = (Number) jweObject.getHeader().getCustomParam("exp");
        if (exp != null) {
            long now = Instant.now().getEpochSecond();
            if (now > exp.longValue()) {
                throw new TokenExpiredException("Token has expired.");
            }
        }

        String tokenIssuer = jweObject.getHeader().getIssuer();
        if (tokenIssuer == null || !tokenIssuer.equals(jweConfig.getIssuer())) {
            throw new InvalidTokenException(String.format("Invalid token issuer. Issue: %s is UNAUTHORIZED. ", tokenIssuer));
        }

        List<Role> roleList = Arrays.asList(Role.values());
        String role = tokenPayload.get("role") != null ? tokenPayload.get("role").toString() : null;
        if (role == null || roleList.stream().noneMatch(r -> r.name().equals(role))) {
            throw new InvalidTokenException(String.format("Invalid token role. Got: %s is UNAUTHORIZED.", role));
        }

        if (type != null && type.equals(TokenType.TEMP)) {
            Object typeObj = tokenPayload.get("type");
            String typeStr = typeObj != null ? typeObj.toString() : null;
            if (typeStr == null || !typeStr.equalsIgnoreCase(TokenType.TEMP.name())) {
                throw new InvalidTokenException(String.format("Invalid token type. Expected: TEMP, Got: %s", typeStr));
            }
        }

        return jweObject;
    }
}
