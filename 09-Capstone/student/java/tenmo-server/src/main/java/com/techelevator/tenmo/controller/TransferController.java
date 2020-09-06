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
	public TransferController(TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	@RequestMapping(value = "/transfers/{id}/viewDetails", method = RequestMethod.GET)
	public Transfer viewTransferDetails(@PathVariable int id) {
		return transferDAO.viewTransferDetails(id);
	}

	@RequestMapping(value = "/transfers/{id}/viewAll", method = RequestMethod.GET)
	public List<TransferBack> viewTransfers(@PathVariable int id) {
		return transferDAO.viewTransfers(id);
	}

	@RequestMapping(value = "/transfers/{id}/pending", method = RequestMethod.GET)
	public List<TransferBack> viewPending(@PathVariable int id) {
		return transferDAO.viewPending(id);
	}

	@RequestMapping(value = "transfers/{id}/pending/2/{transferId}", method = RequestMethod.PUT)
	public void updatePendingApprove(@PathVariable int id, @PathVariable int transferId) {
		transferDAO.updatePendingApprove(transferId);
	}

	@RequestMapping(value = "transfers/{id}/pending/3/{transferId}", method = RequestMethod.PUT)
	public void updatePendingReject(@PathVariable int id, @PathVariable int transferId) {
		transferDAO.updatePendingReject(transferId);
	}

	@RequestMapping(value = "/transfer/send", method = RequestMethod.POST)
	public void transferSend(@RequestBody Transfer transfer) {
		transferDAO.transferSend(transfer);
		transferDAO.updateBalance(transfer);
		transferDAO.updateBalance1(transfer);
		transferDAO.updateBalance2(transfer);
	}

	@RequestMapping(value = "/transfer/request", method = RequestMethod.POST)
	public void transferRequest(@RequestBody Transfer transfer) {
		transferDAO.transferRequest(transfer);
	}

	@RequestMapping(value = "/transfer", method = RequestMethod.PUT)
	public void makeTransfer(@RequestBody Transfer transfer) {
		transferDAO.updateBalance(transfer);
		transferDAO.updateBalance1(transfer);
		transferDAO.updateBalance2(transfer);
	}

}
