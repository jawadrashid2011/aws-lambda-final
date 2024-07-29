package com.keycloak.sessionsapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.s3.S3AsyncClient;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<Object, Object> {
    private final S3AsyncClient s3Client;

    public App() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    @Override
    public Object handleRequest(final Object input, final Context context) {
    	String serverUrl = "http://keycloak_demo:8080";
    	String realmName = "Demo";
    	String mainAdminRealm = "master";
    	
    	LinkedHashMap<String, String> inputHashMap = (LinkedHashMap<String, String>) input; 
    	String user = inputHashMap.get("username");
    	if(inputHashMap.containsKey("realm")) {
    		realmName = inputHashMap.get("realm");
    	}
    	
    	Keycloak keycloak = KeycloakBuilder.builder() //
                .serverUrl(serverUrl) //
                .realm(mainAdminRealm) //
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
                .clientId("demo-client") //
                .clientSecret("CLIENT_SECRET_GOES_HERE") //
                .build();
    	
    	List<UserRepresentation> users = keycloak.realm(realmName)
    		      .users()
    		      .searchByUsername(user, true);
    	
    	
    	
    	
    	if(users.size() == 0) {
    		return "No User Found";
    	}
    	
    	UserRepresentation foundUser = users.get(0);

    	List<UserSessionRepresentation> sessionsList = keycloak.realm(realmName).users().get(foundUser.getId()).getUserSessions();
    	
    	if(sessionsList.size() == 0) {
    		return "No User Session Found";
    	}
    	
    	List <String> userSessions = new ArrayList<String>();
    	for(UserSessionRepresentation session : sessionsList) {
    		String format = String.format("Id: %s, Username: %s, UserId: %s, ipAddress: %s, Start: %d, LastAccess: %d", 
    				session.getId(), session.getUsername(), session.getUserId(), session.getIpAddress(), session.getStart(), session.getLastAccess());
    		System.out.println(format);
    		userSessions.add(format);
    	}
    	System.out.println(userSessions);
    	
    	
    	
        return sessionsList;
    }
    
}
