package com.threedsoft.test;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

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
                //.deserializers(JsonDeserializer<WMSEvent>.class)
                .modules(new JSR310Module())
                .build();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		return mapper;

    }

}
