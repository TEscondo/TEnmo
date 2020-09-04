package com.techelevator.tenmo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferBack;

@PreAuthorize("isAuthenticated()")
@RestController
public class TransferController {
	@Autowired
	private TransferDAO transferDAO;
	@Autowired
	private UserDAO userDAO;
	private Transfer transfer;

	@Autowired
	public TransferController(TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}
	
	@RequestMapping(value="/transfers/{id}/viewDetails",method=RequestMethod.GET)
    public Transfer viewTransferDetails(@PathVariable int id) {
    	return transferDAO.viewTransferDetails(id);
    }
	
	@RequestMapping(value="/transfers/{id}/viewAll",method=RequestMethod.GET)
    public List<TransferBack> viewTransfers(@PathVariable int id) {
    	return transferDAO.viewTransfers(id);
    }
	
	 @RequestMapping(value="/transfers/{id}/pending",method=RequestMethod.GET)
	    public void viewPending(@PathVariable int id) {
		 	transferDAO.viewPending(id);
	    }
	 
	 @RequestMapping(value="transfers/{id}/pending/{option}", method=RequestMethod.GET)
	 public void updatePending(@RequestParam int transferId, @PathVariable int id, @PathVariable int option) {
		 transferDAO.updatePending(option, transferId);
	 }
	
	 @RequestMapping(value="/transfer", method=RequestMethod.POST)
	    public void newTransfer(@RequestBody Transfer transfer) {		 
	    	transferDAO.transfer(transfer);
	    	transferDAO.updateBalance(transfer);
	    	transferDAO.updateBalance1(transfer);
	    }
	 
	  @RequestMapping(value="/transfer", method=RequestMethod.PUT)
	    public void makeTransfer(@RequestBody Transfer transfer) {
	    	transferDAO.updateBalance(transfer);
	    }
	
	
}
