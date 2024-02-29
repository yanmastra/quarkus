package org.acme.authenticationService.services;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.utils.CacheUpdateMode;
import com.acme.authorization.utils.DateTimeUtils;
import com.acme.authorization.utils.JsonUtils;
import com.acme.authorization.utils.KeyValueCacheUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.acme.authenticationService.dao.RegisterRequestJson;
import org.acme.authenticationService.dao.RegisterResponseJson;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.data.repository.ApplicationRepository;
import org.acme.authenticationService.data.repository.RoleRepository;
import org.acme.authenticationService.data.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class RegisterService {
    private static final String REGISTER_USER_DATA = "RXEXGY";
    private static final String REGISTER_OTP_DATA = "RXEXGZ";;
    private static final String REGISTER_OTP_EXP = "RXEXGEXP";
    private final Random random = new Random();

    @Inject
    Logger logger;

    @Inject
    ApplicationRepository appRepository;
    @Inject
    RoleRepository roleRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    MailService mailService;

    @WithTransaction
    public Uni<ResponseJson<RegisterResponseJson>> saveRegistration(RegisterRequestJson requestJson, String lang) {
        return userRepository.find("email=?1", requestJson.getEmail()).firstResult()
                .chain(user -> {
                    if (user != null)
                        throw new HttpException(HttpResponseStatus.CONFLICT.code(), "User with same email already exists!");

                    String sameReg = KeyValueCacheUtils.findCache(REGISTER_OTP_DATA, KeyValueCacheUtils.hide(requestJson.getContact()));
                    if (StringUtils.isNotBlank(sameReg))
                        throw new HttpException(HttpResponseStatus.CONFLICT.code(), "User with same email already exists!");

                    return appRepository.findById(requestJson.getApplicationCode())
                            .chain(app -> {
                                if (app == null)
                                    throw new NotFoundException("Application with code:" + requestJson.getApplicationCode() + " not found!");

                                return roleRepository.findById(requestJson.getApplicationCode(), requestJson.getRoleCode())
                                        .chain(role -> {
                                            if (role == null)
                                                throw new NotFoundException("Role with code:" + requestJson.getRoleCode() + " in application:" + app.getCode() + " not found!");

                                            String otpCode = getOtpCode();
                                            String otpKey = UUID.randomUUID().toString();

                                            Map<String, String> regData = new HashMap<>();
                                            regData.put("data", JsonUtils.toJson(requestJson));
                                            regData.put("process_id", requestJson.getProcessId());
                                            regData.put("otp_code", otpCode);
                                            regData.put("otp_key", otpKey);
                                            regData.put("app_code", app.getCode());
                                            regData.put("role_code", role.getCode());

                                            String keyOtpData = KeyValueCacheUtils.hide(requestJson.getContact());
                                            String valOtpData = KeyValueCacheUtils.hide(otpCode);
                                            logger.error(keyOtpData+" --> "+valOtpData+" --> "+KeyValueCacheUtils.showHiddenString(valOtpData));

                                            saveOtpSession(keyOtpData, valOtpData, otpKey, regData);

                                            return mailService.createRegisterEmail(lang, otpCode, requestJson.getName(), requestJson.getEmail(), app.getName(), role.getName())
                                                    .map(r -> regData);
                                        });
                            }).map(data -> new ResponseJson<>(
                                    true,
                                    "New user has been registered!",
                                    new RegisterResponseJson((String) data.get("otp_key"), (String) data.get("process_id"))
                            ));
                });
    }

    private synchronized void saveOtpSession(String keyOtpData, String valOtpData, String otpKey, Map<String, String> regData) {
        KeyValueCacheUtils.saveCache(REGISTER_OTP_EXP, otpKey, DateTimeUtils.formattedUtcDate(DateUtils.addMinutes(new Date(), 10)), CacheUpdateMode.ADD);
        KeyValueCacheUtils.saveCache(REGISTER_OTP_DATA, keyOtpData, valOtpData, CacheUpdateMode.ADD);
        KeyValueCacheUtils.saveCache(REGISTER_USER_DATA, KeyValueCacheUtils.hide(otpKey), KeyValueCacheUtils.hide(JsonUtils.toJson(regData)), CacheUpdateMode.ADD);
    }

    private synchronized void removeOtpSession(String keyOtpData, String otpKey) {
        KeyValueCacheUtils.saveCache(REGISTER_OTP_EXP, otpKey, null, CacheUpdateMode.REMOVE);
        KeyValueCacheUtils.saveCache(REGISTER_OTP_DATA, keyOtpData, null, CacheUpdateMode.REMOVE);
        KeyValueCacheUtils.saveCache(REGISTER_USER_DATA, KeyValueCacheUtils.hide(otpKey), null, CacheUpdateMode.REMOVE);
    }

    @WithTransaction
    public Uni<ResponseJson<RegisterResponseJson>> verifyOtp(String otpKey, String otpCode, ContainerRequestContext context) {
        if (StringUtils.isBlank(otpKey) || StringUtils.isBlank(otpCode))
            throw new IllegalArgumentException("Please send the correct OTP Key and OTP Code!");

        String sExpOtp = KeyValueCacheUtils.findCache(REGISTER_OTP_EXP, otpKey);
        if (StringUtils.isBlank(sExpOtp) || (new Date()).after(DateTimeUtils.fromUtc(sExpOtp)))
            throw new SecurityException("OTP Expired!");

        String hiddenRegData = KeyValueCacheUtils.findCache(REGISTER_USER_DATA, KeyValueCacheUtils.hide(otpKey));
        String regData = KeyValueCacheUtils.showHiddenString(hiddenRegData);
        Map<String, String> data = JsonUtils.fromJson(regData, new TypeReference<>() {
        });
        if (otpKey.equals(data.get("otp_key")) && otpCode.equals(data.get("otp_code"))) {
            RegisterRequestJson requestJson = JsonUtils.fromJson(data.get("data"), RegisterRequestJson.class);
            UserOnly userOnly = new UserOnly();
            userOnly.setId(null);
            userOnly.setName(requestJson.getName());
            userOnly.setUsername(requestJson.getContact());
            userOnly.setEmail(requestJson.getEmail());
            userOnly.setRolesIds(Map.of(requestJson.getApplicationCode(), Collections.singletonList(requestJson.getRoleCode())));
            return appRepository.findById(requestJson.getApplicationCode())
                    .map(app -> {
                        String tokenId = UUID.randomUUID().toString();
                        String secretKey = app.getSecretKey();
                        Set<String> permission = Set.of("CREATE_OWN_USER", "CHANGE_OWN_PASSWORD");

                        String issuer = TokenUtils.getIssuer(context);
                        String temporaryToken = TokenUtils.createAccessToken(issuer, requestJson.getApplicationCode(), JsonUtils.toJson(userOnly), DateUtils.addMinutes(new Date(), 30), permission, secretKey);
                        TokenUtils.saveSession(tokenId, app.getCode(), secretKey, "");

                        ResponseJson<RegisterResponseJson> responseJson = new ResponseJson<>(true, null);
                        RegisterResponseJson responseData = new RegisterResponseJson();
                        responseData.setUserData(requestJson);
                        responseData.setTemporaryAccessToken(temporaryToken);
                        responseData.setProcessId(data.get("process_id"));

                        responseJson.setData(responseData);
                        return responseJson;
                    })
                    .onItem().invoke(r -> {
                        removeOtpSession(KeyValueCacheUtils.hide(requestJson.getContact()), data.get("otp_key"));
                    });
        } else {
            return Uni.createFrom().item(new ResponseJson<>(false, "Incorrect OTP Code!"));
        }
    }

    private String getOtpCode() {
        int[] codes = new int[3];
        codes[0] = random.nextInt(99);
        codes[1] = random.nextInt(99);
        codes[2] = random.nextInt(99);
        StringBuilder sCode = new StringBuilder();
        for (int code : codes) {
            String ssCode = String.valueOf(code);
            if (ssCode.length() == 1) ssCode = '0' + ssCode;
            sCode.append(ssCode);
        }
        return sCode.toString();
    }

}
