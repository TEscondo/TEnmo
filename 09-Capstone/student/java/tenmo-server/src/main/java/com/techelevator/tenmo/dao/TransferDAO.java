package com.techelevator.tenmo.dao;
import com.techelevator.tenmo.model.Transfer;
public interface TransferDAO {

	void updatePending(int optionChoice, long transferId);

	void viewTransferDetails(int transferId);
	
	void viewTransfers();

	Transfer transfer(Transfer transfer);
	
	boolean updateBalance(Transfer transfer);
	
	void viewPending();
}
