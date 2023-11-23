package org.acme.microservices.common.messaging;

import com.acme.authorization.utils.JsonUtils;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;

public class Serializer implements org.apache.kafka.common.serialization.Serializer<Object> {

    private final StringSerializer stringSerializer;


    public Serializer() {
        this.stringSerializer = new StringSerializer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        stringSerializer.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, Object object) {
        String sData = JsonUtils.toJson(object, true);
        return stringSerializer.serialize(s, sData);
    }
}
