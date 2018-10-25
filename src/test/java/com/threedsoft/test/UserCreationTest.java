package com.threedsoft.test;

import java.util.Locale;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.threedsoft.test.service.EventPublisher;
import com.threedsoft.user.dto.requests.UserCreationRequestDTO;
import com.threedsoft.user.dto.requests.UserLoginInRequestDTO;
import com.threedsoft.user.dto.responses.UserResourceDTO;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
		"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
		//"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.JsonDeserializer",
		//"spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
		//"spring.kafka.consumer.JsonDeserializer.VALUE_DEFAULT_TYPE=com.threedsoft.util.dto.events.WMSEvent",
		// "spring.cloud.stream.bindings.inventory-in.producer.headerMode=none",
		//"spring.cloud.stream.bindings.inventory-in.contentType=application/json",
		//"spring.cloud.stream.bindings.inventory-in.group=wmsorder-invn-producer",
		//"spring.cloud.stream.kafka.bindings.inventory-out.consumer.autoCommitOffset=true",
		// "spring.cloud.stream.bindings.inventory-out.consumer.headerMode=none",
		//"spring.cloud.stream.bindings.inventory-out.contentType=application/json",
		// "spring.kafka.consumer.group-id=test",
		//"spring.cloud.stream.default.consumer.headerMode=raw",
		//"spring.cloud.stream.default.producer.headerMode=raw",
		//"spring.cloud.stream.kafka.binder.headers[0]=eventName",
		"spring.cloud.stream.kafka.binder.headers=eventName",
		"spring.cloud.stream.kafka.binder.auto-create-topics=false",
		//"spring.cloud.stream.kafka.binder.brokers=localhost:29092",
		"spring.jackson.serialization.WRITE_DATES_AS_TIMESTAMPS=false",
		"spring.cloud.stream.kafka.binder.brokers=35.236.200.183:9092"},
		classes = { EventPublisher.class,
				WMSStreams.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@EnableBinding(WMSStreams.class)
public class UserCreationTest {
	String SERVICE_NAME="UserCreationTest";
	static {
	    //for localhost testing only for https
	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return true;
	        }
	    });
	}	
	
	String userPort = "9016";
	String userServiceHost = "localhost";
	
	// gcp ports
//	String userServiceHost = "35.221.78.28";
//	String userPort = "8080";*/
	
	@Test
	public void createUser() {
		RestTemplate restTemplate = new RestTemplate();
		String busName ="XYZ";
		Integer locnNbr = 3456;
		String userCreateURL = "https://" +userServiceHost + ":" + userPort + "/users/v1/user";
		System.out.println("user createUser url:" + userCreateURL);
		// create new user
		String authToken = RandomStringUtils.random(6,true, true);
		UserCreationRequestDTO userCreationReq = createUserCreationReq(busName, locnNbr, "john.doe@gmail", "John", "Doe", "user", authToken);
		UserResourceDTO userResourceDTO = restTemplate.postForObject(userCreateURL, userCreationReq, UserResourceDTO.class);
		System.out.println("User Create Reponse:" + userResourceDTO);
		
		//sign in the user
		UserLoginInRequestDTO userSignInReq = new UserLoginInRequestDTO();
		userSignInReq.setUserName(userResourceDTO.getUserName());
		userSignInReq.setAuthToken(authToken);
		
		String userloginInURL = "https://" +userServiceHost + ":" + userPort + "/users/v1/user/signin";
		System.out.println("user signIn url:" + userloginInURL);
		UserResourceDTO userSignInResourceDTO = restTemplate.postForObject(userloginInURL, userSignInReq, UserResourceDTO.class);
		System.out.println("User SignIn Reponse:" + userSignInResourceDTO);
	}
	
	public UserCreationRequestDTO createUserCreationReq(String busName, Integer locnNbr, String userName, String firstName, String lastName, String authType, String authToken) {
		UserCreationRequestDTO userCreationReq = new UserCreationRequestDTO();
		userCreationReq.setUserName(userName);
		userCreationReq.setFirstName(firstName);
		userCreationReq.setLastName(lastName);
		userCreationReq.setMiddleName(RandomStringUtils.random(4,true, false));
		userCreationReq.setAddr1(RandomStringUtils.random(4,false, true));
		userCreationReq.setAddr2(RandomStringUtils.random(10,true, false));
		userCreationReq.setAddr3(RandomStringUtils.random(10,true, false));
		userCreationReq.setBusName(busName);
		userCreationReq.setDefLocnNbr(locnNbr);
		userCreationReq.setAuthType(authType);
		userCreationReq.setAuthToken(authToken);
		userCreationReq.setCity(RandomStringUtils.random(6,true, false));
		userCreationReq.setState(RandomStringUtils.random(2,true, false));
		userCreationReq.setCountry(RandomStringUtils.random(10,true, false));
		userCreationReq.setZipCode(RandomStringUtils.random(5,false, true));
		userCreationReq.setLocale(Locale.getDefault().getDisplayName());
		userCreationReq.setUserId(userCreationReq.getUserName());
		return userCreationReq;
	}
}
