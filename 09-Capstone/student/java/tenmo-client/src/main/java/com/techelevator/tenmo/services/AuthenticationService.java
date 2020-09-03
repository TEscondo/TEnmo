package com.techelevator.tenmo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;

public class AuthenticationService {

	public static String AUTH_TOKEN = "";
    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    

    public AuthenticationService(String url) {
        this.BASE_URL = url;
    }

    public AuthenticatedUser login(UserCredentials credentials) throws AuthenticationServiceException {
        HttpEntity<UserCredentials> entity = createRequestEntity(credentials);
        return sendLoginRequest(entity);
    }

    public void register(UserCredentials credentials) throws AuthenticationServiceException {
    	HttpEntity<UserCredentials> entity = createRequestEntity(credentials);
        sendRegistrationRequest(entity);
    }
    
	private HttpEntity<UserCredentials> createRequestEntity(UserCredentials credentials) {
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	HttpEntity<UserCredentials> entity = new HttpEntity<>(credentials, headers);
    	return entity;
    }

	private AuthenticatedUser sendLoginRequest(HttpEntity<UserCredentials> entity) throws AuthenticationServiceException {
		try {	
			ResponseEntity<AuthenticatedUser> response = restTemplate.exchange(BASE_URL + "login", HttpMethod.POST, entity, AuthenticatedUser.class);
			return response.getBody(); 
		} catch(RestClientResponseException ex) {
			String message = createLoginExceptionMessage(ex);
			throw new AuthenticationServiceException(message);
        }
	}

    private ResponseEntity<Map> sendRegistrationRequest(HttpEntity<UserCredentials> entity) throws AuthenticationServiceException {
    	try {
			return restTemplate.exchange(BASE_URL + "register", HttpMethod.POST, entity, Map.class);
		} catch(RestClientResponseException ex) {
			String message = createRegisterExceptionMessage(ex);
			throw new AuthenticationServiceException(message);
        }
	}

	private String createLoginExceptionMessage(RestClientResponseException ex) {
		String message = null;
		if (ex.getRawStatusCode() == 401 && ex.getResponseBodyAsString().length() == 0) {
		    message = ex.getRawStatusCode() + " : {\"timestamp\":\"" + LocalDateTime.now() + "+00:00\",\"status\":401,\"error\":\"Invalid credentials\",\"message\":\"Login failed: Invalid username or password\",\"path\":\"/login\"}";
		}
		else {
		    message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
		}
		return message;
	}
	
	private String createRegisterExceptionMessage(RestClientResponseException ex) {
		String message = null;
		if (ex.getRawStatusCode() == 400 && ex.getResponseBodyAsString().length() == 0) {
		    message = ex.getRawStatusCode() + " : {\"timestamp\":\"" + LocalDateTime.now() + "+00:00\",\"status\":400,\"error\":\"Invalid credentials\",\"message\":\"Registration failed: Invalid username or password\",\"path\":\"/register\"}";
		}
		else {
		    message = ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString();
		}
		return message;
	}
	
	  public double getBalance(String token) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  double balance = 0.0;
			try {
				balance = restTemplate.exchange(BASE_URL + "balance", HttpMethod.GET, makeAuthEntity(), double.class).getBody();
			} catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
			return balance;
	    }
	
	  private HttpEntity makeAuthEntity() {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setBearerAuth(AUTH_TOKEN);
	        HttpEntity entity = new HttpEntity<>(headers);
	        return entity;
	    }
	  
	  public Transfer newTransfer(String token) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  Transfer tran;
		  try {
			 tran = restTemplate.exchange(BASE_URL + "transfer", HttpMethod.POST, makeAuthEntity(), Transfer.class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
		  return tran;
	  }
	  

	  public void viewTransfers(String token) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  try {
			  restTemplate.exchange(BASE_URL + "transfers", HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
	  
	  }
	  
	  public void viewPending(String token) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  try {
			  restTemplate.exchange(BASE_URL + "transfers/pending", HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
	  }

	  public User[] getUsers(String token) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  User[] users;
		  try {
			  users = restTemplate.exchange(BASE_URL + "account/allaccounts", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();			  
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
		  return users;
	  }
	  
	  
}
