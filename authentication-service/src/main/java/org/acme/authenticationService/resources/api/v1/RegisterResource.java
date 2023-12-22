package org.acme.authenticationService.resources.api.v1;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.utils.CacheUpdateMode;
import com.acme.authorization.utils.DateTimeUtils;
import com.acme.authorization.utils.KeyValueCacheUtils;
import com.acme.authorization.utils.ValidationUtils;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.acme.authenticationService.dao.RegisterRequestJson;
import org.acme.authenticationService.dao.RegisterResponseJson;
import org.acme.authenticationService.services.RegisterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jboss.logging.Logger;

import javax.security.auth.login.CredentialExpiredException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Path("api/v1/register")
@Produces(MediaType.APPLICATION_JSON)
public class RegisterResource {

    @Inject
    RegisterService service;

    @Inject
    Logger logger;

    private static final String REGISTER_PROCESS_ID="RXEXGX";

    @GET
    @PermitAll
    public Uni<ResponseJson<Map<String, String>>> getRegisterProcessId(@Context ContainerRequestContext context) {
        logger.info("get register id from:"+context.getUriInfo().getRequestUri().getHost()+", agent:"+context.getHeaderString("Agent"));
        String registerID = UUID.randomUUID().toString();
        KeyValueCacheUtils.saveCache(REGISTER_PROCESS_ID, registerID, DateTimeUtils.formattedUtcDate(DateUtils.addMinutes(new Date(), 20)), CacheUpdateMode.ADD);
        ResponseJson<Map<String, String>> response = new ResponseJson<>();
        response.setSuccess(true);
        response.setData(Collections.singletonMap("process_id", registerID));
        return Uni.createFrom().item(response);
    }

    @POST
    @PermitAll
    public Uni<ResponseJson<RegisterResponseJson>> register(RegisterRequestJson requestJson, @Context ContainerRequestContext context) throws CredentialExpiredException {
        logger.info("register from:"+context.getUriInfo().getRequestUri().getHost()+", agent:"+context.getHeaderString("Agent")+", content:"+requestJson);

        if (!validateProcessId(requestJson.getProcessId()))
            throw new CredentialExpiredException("process_id expired!");

        if (!ValidationUtils.isEmail(requestJson.getEmail())) {
            throw new IllegalArgumentException("Incorrect email address!");
        }

        String lang = "EN";
        if (context.hasProperty("lang")) {
            lang = context.getProperty("lang")+"";
        }

        return service.saveRegistration(requestJson, lang);
    }

    private synchronized boolean validateProcessId(String processId) {
        if (StringUtils.isBlank(processId)) return false;

        try {
            String sExp = KeyValueCacheUtils.findCache(REGISTER_PROCESS_ID, processId);
            if (StringUtils.isBlank(sExp)) {
                KeyValueCacheUtils.saveCache(REGISTER_PROCESS_ID, processId, null, CacheUpdateMode.REMOVE);
            }
            Date date = DateTimeUtils.fromUtc(sExp);
            return date != null && date.before(new Date());
        }catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return false;
        }
    }
}
