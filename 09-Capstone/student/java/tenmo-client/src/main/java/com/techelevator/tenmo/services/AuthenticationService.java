package com.techelevator.tenmo.services;


import java.time.LocalDateTime;
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
import com.techelevator.tenmo.models.TransferBack;
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
		  double balance = 0.00;
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
	  private HttpEntity makeJSONEntity(String token, Transfer jsonBody) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(AUTH_TOKEN);
			HttpEntity entity = new HttpEntity<>(jsonBody, headers);
				return entity;
			}
	  
	  public void transferSend(String token, Transfer transferBody) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  try {
			 restTemplate.exchange(BASE_URL + "transfer/send", HttpMethod.POST, makeJSONEntity(AUTH_TOKEN, transferBody), Transfer.class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
	  }
	  
	  public void transferRequest(String token, Transfer transferBody) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  try {
			 restTemplate.exchange(BASE_URL + "transfer/request", HttpMethod.POST, makeJSONEntity(AUTH_TOKEN, transferBody), Transfer.class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
	  }

	  public TransferBack[] viewTransfers(String token, int userId) throws AuthenticationServiceException {
		  TransferBack[] transfers = null;
		  AUTH_TOKEN = token;
		  try {
			  transfers = restTemplate.exchange(BASE_URL + "transfers/" + userId + "/viewAll", HttpMethod.GET, makeAuthEntity(), TransferBack[].class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
		  return transfers;
	  }
	  
	  public Transfer viewTransferDetails(String token, int userId) throws AuthenticationServiceException {
		Transfer transfer = null;
		AUTH_TOKEN = token;
		 try {
			  transfer = restTemplate.exchange(BASE_URL + "transfers/" + userId + "/viewDetails", HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
		  return transfer;
	  }
	  
	  public TransferBack[] viewPending(String token, int userId) throws AuthenticationServiceException {
		  TransferBack[] pending = null;
		  AUTH_TOKEN = token;
		  try {
			  pending = restTemplate.exchange(BASE_URL + "transfers/" + userId + "/pending", HttpMethod.GET, makeAuthEntity(), TransferBack[].class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
		  return pending;
	  }
	  
	  public void updatePendingApprove(String token, int userId, int transferId) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  try {
			  restTemplate.exchange(BASE_URL+"transfers/" + userId + "/pending/2/" + transferId, HttpMethod.PUT, makeAuthEntity(), Transfer.class).getBody();
		  } catch (RestClientResponseException ex) {
	            throw new AuthenticationServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        }
	  }
	  
	  public void updatePendingReject(String token, int userId, int transferId) throws AuthenticationServiceException {
		  AUTH_TOKEN = token;
		  try {
			  restTemplate.exchange(BASE_URL+"transfers/" + userId + "/pending/3/" + transferId, HttpMethod.PUT, makeAuthEntity(), Transfer.class).getBody();
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
