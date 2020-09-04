package com.techelevator.tenmo.dao;
import java.util.List;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferBack;

public interface TransferDAO {

	void updatePending(int optionChoice, long transferId);

	void viewTransferDetails(int transferId);
	
	List<TransferBack> viewTransfers(int id);
	
	Transfer transfer(Transfer transfer);
	
	boolean updateBalance(Transfer transfer);
	
	void viewPending();

	boolean updateBalance1(Transfer transfer);
}
