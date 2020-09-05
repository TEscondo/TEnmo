package com.techelevator.tenmo.dao;
import java.util.List;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferBack;

public interface TransferDAO {

	void updatePendingApprove(int transferId);
	
	void updatePendingReject(int transferId);

	Transfer viewTransferDetails(int transferId);
	
	List<TransferBack> viewTransfers(int id);
	
	Transfer transferSend(Transfer transfer);
	
	Transfer transferRequest(Transfer transfer);
	
	boolean updateBalance(Transfer transfer);
	
	List<TransferBack> viewPending (int id);

	boolean updateBalance1(Transfer transfer);
	
	boolean updateBalance2(Transfer transfer);
	
	boolean updateBalance3(Transfer transfer);
}
