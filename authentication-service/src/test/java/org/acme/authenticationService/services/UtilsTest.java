package org.acme.authenticationService.services;

import com.acme.authorization.utils.CacheUpdateMode;
import com.acme.authorization.utils.KeyValueCacheUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import java.util.UUID;

@QuarkusTest
public class UtilsTest {

    @Test
    void testKeyCacheUtils() {
        KeyValueCacheUtils.saveCache("APPLICATION", "SYSTEM_192837198731739817923_SEC", UUID.randomUUID().toString(), CacheUpdateMode.ADD);

        String key = KeyValueCacheUtils.findCache("APPLICATION", "SYSTEM_192837198731739817923_SEC");
        System.out.println("key:"+key);
        Assert.assertNotNull(key);
    }
}
