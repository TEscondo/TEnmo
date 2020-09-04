package com.techelevator.tenmo.dao;
import java.util.List;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferBack;

public interface TransferDAO {

	void updatePending(int optionChoice, int transferId);

	Transfer viewTransferDetails(int transferId);
	
	List<TransferBack> viewTransfers(int id);
	
	Transfer transfer(Transfer transfer);
	
	boolean updateBalance(Transfer transfer);
	
	List<TransferBack> viewPending (int id);

	boolean updateBalance1(Transfer transfer);
}
