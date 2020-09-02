package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.security.Principal;
import java.util.List;

public interface UserDAO {

	Transfer transfer(Transfer transfer);
	
	boolean updateBalance(Transfer transfer);
	
	double getBalance(Principal principal);
	
    List<User> findAll();

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean create(String username, String password);
}
