package com.techelevator.tenmo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Transfer;

@PreAuthorize("isAuthenticated()")
@RestController
public class TransferController {
	@Autowired
	private TransferDAO transferDAO;
	@Autowired
	private UserDAO userDAO;
	private Transfer transfer;

//	@Autowired
//	public TransferController(TransferDAO transferDAO) {
//		this.transferDAO = transferDAO;
//	}
	
	@RequestMapping(value="/transfers",method=RequestMethod.GET)
    public void viewTransfers() {
    	transferDAO.viewTransfers();
    }
	
	 @RequestMapping(value="/transfers/pending",method=RequestMethod.GET)
	    public void viewPending() {
	    	transferDAO.viewPending();
	    }
	
	 @RequestMapping(value="/transfer", method=RequestMethod.POST)
	    public void newTransfer(@RequestBody Transfer transfer) {
		 
		 	
		 	System.out.println("Here is the JSON I'm deserializing:");
		 	System.out.println(transfer);
		 
		 
//	    	Transfer pending = null;
	    	transferDAO.transfer(transfer);
	    	transferDAO.updateBalance(transfer);
//	    	return pending;
	    }
	 
	  @RequestMapping(value="/transfer", method=RequestMethod.PUT)
	    public void makeTransfer(@RequestBody Transfer transfer) {
	    	transferDAO.updateBalance(transfer);
	    }
	
	
}
