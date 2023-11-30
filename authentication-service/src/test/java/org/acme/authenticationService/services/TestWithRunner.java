package org.acme.authenticationService.services;

import com.acme.authorization.json.AuthenticationResponse;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.DateTimeUtils;
import com.acme.authorization.utils.PasswordGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;

@QuarkusTest
public class TestWithRunner {

    @Inject
    JWTParser parser;
    @Inject
    ObjectMapper objectMapper;

    private Random random = new Random();

    private Logger logger = Logger.getLogger(TestWithRunner.class.getName());

//    @Test
    void testAccessToken() {
        for (int i = 0; i < 100; i++) {

            String ni = random.nextInt(999) + "";
            String appCode = "SYSTEM_" + ni;
            String subject = "{\"username\":\"wayan_" + ni + "\", \"id\":\"1234\"}";
            String username = "wayan_" + ni;
            String issuer = ni + "2345678";
            Date expiredAt = DateTimeUtils.getExpiredToken();

            try {
                Set<String> permissions = new HashSet<>(Arrays.asList("Admin", "Customer"));
                String secretKey = PasswordGenerator.generatePassword(32, true);
                String refreshKey = PasswordGenerator.generatePassword(32, true);

                String accessToken = TokenUtils.createAccessToken(appCode, subject, username, issuer, expiredAt, permissions, secretKey);
                String refreshToken = TokenUtils.createRefreshToken(appCode, subject, username, issuer, expiredAt, permissions, refreshKey);
                TokenUtils.saveSession(issuer, appCode, secretKey, refreshKey);

                UserPrincipal principal = TokenUtils.verifyAccessToken(accessToken, parser, objectMapper);
                Thread.sleep(200);
                AuthenticationResponse newAuth = TokenUtils.createAccessToken(refreshToken, parser, objectMapper);

                logger.info("result: " + principal);
                logger.info("result: " + newAuth);

            } catch (Exception e) {
                logger.error("error test: " + appCode, e);
            }
        }
    }
}
