package com.threedsoft.test;

import java.time.Instant;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class EventResourceConverter {
	
	public static <T> T getObject(Object obj, Class<T> cls) {
		//T resourceObj = getObjectMapper().convertValue(obj, cls);
		T resourceObj = getObjectMapper().convertValue(obj, cls);
		return resourceObj;
	}
    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper =  Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_NULL) // Don’t include null values
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
                //.registerModule(new Jdk8Module())
                //.registerModule(new JavaTimeModule())
                //.deserializers(JsonDeserializer<WMSEvent>.class)
                //.modules(new JSR310Module(), new JavaTimeModule())
                //.modules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule())
//                .registerModule(new ParameterNamesModule())
//                .registerModule(new Jdk8Module())
//                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        		.build();
       mapper.findAndRegisterModules();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		//javaTimeModule.addSerializer(Instant.class, JSR310DateTimeSerializer.INSTANCE);
		return mapper;

    }

}
