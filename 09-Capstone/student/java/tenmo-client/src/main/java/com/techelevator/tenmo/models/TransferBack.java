package com.techelevator.tenmo.models;

public class TransferBack {
	private int transferId;
	private String usernameTo;
	private String usernameFrom;
	private Double amount;
	int transferTypeId;
	int transferStatusId;
	
//	public TransferBack(int transferId, String usernameTo, String usernameFrom, Double amount) {
//		this.transferId = transferId;
//		this.usernameTo = usernameTo;
//		this.usernameFrom = usernameFrom;
//		this.amount = amount;
//	}

	public int getTransferId() {
		return transferId;
	}

	public int getTransferTypeId() {
		return transferTypeId;
	}

	public void setTransferTypeId(int transferTypeId) {
		this.transferTypeId = transferTypeId;
	}

	public int getTransferStatusId() {
		return transferStatusId;
	}

	public void setTransferStatusId(int transferStatusId) {
		this.transferStatusId = transferStatusId;
	}

	public void setTransferId(int transferId) {
		this.transferId = transferId;
	}

	public String getUsernameTo() {
		return usernameTo;
	}

	public void setUsernameTo(String usernameTo) {
		this.usernameTo = usernameTo;
	}

	public String getUsernameFrom() {
		return usernameFrom;
	}

	public void setUsernameFrom(String usernameFrom) {
		this.usernameFrom = usernameFrom;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	
	
}
