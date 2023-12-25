package org.acme.authenticationService.services;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.utils.CacheUpdateMode;
import com.acme.authorization.utils.JsonUtils;
import com.acme.authorization.utils.KeyValueCacheUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.acme.authenticationService.dao.RegisterRequestJson;
import org.acme.authenticationService.dao.RegisterResponseJson;
import org.acme.authenticationService.data.repository.ApplicationRepository;
import org.acme.authenticationService.data.repository.RoleRepository;
import org.acme.authenticationService.data.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class RegisterService {
    private static final String REGISTER_USER_DATA = "RXEXGY";
    private final Random random = new Random();

    @Inject
    ReactiveMailer mailer;

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
        return userRepository.find("email=?", requestJson.getEmail()).firstResult()
                .chain(user -> {
                    if (user != null)
                        throw new HttpException(HttpResponseStatus.CONFLICT.code(), "User with same email already exists!");

                    return Uni.combine().all()
                            .unis(
                                    Uni.createFrom().item(requestJson),
                                    appRepository.findById(requestJson.getApplicationCode()),
                                    roleRepository.findById(requestJson.getApplicationCode(), requestJson.getRoleCode())
                            ).combinedWith((req, app, role) -> {
                                if (app == null)
                                    throw new NotFoundException("Application with code:" + req.getApplicationCode() + " not found!");
                                if (role == null)
                                    throw new NotFoundException("Role with code:" + req.getRoleCode() + " in application:" + app.getCode() + " not found!");

                                String otpCode = getOtpCode();
                                String otpKey = UUID.randomUUID().toString();

                                Map<String, Object> regData = new HashMap<>();
                                regData.put("data", req);
                                regData.put("process_id", req.getProcessId());
                                regData.put("otp_code", otpCode);
                                regData.put("otp_key", otpKey);
                                regData.put("app_code", app.getCode());
                                regData.put("role_code", role.getCode());
                                KeyValueCacheUtils.saveCache(REGISTER_USER_DATA, otpKey, JsonUtils.toJson(regData), CacheUpdateMode.ADD);

                                Uni<Void> uni = mailService.createRegisterEmail(lang, otpCode, req.getName(), req.getEmail(), app.getName(), role.getName());
//                                regData.put("mail", mail);
                                return regData;
                            });
                })
                .map(data -> new ResponseJson<>(
                                true,
                                "New user has been registered!",
                                new RegisterResponseJson((String) data.get("otp_key"), (String) data.get("process_id"))
                        ));
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
