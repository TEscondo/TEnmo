package com.techelevator.tenmo.models;



public class Transfer {

	private int transfer_id;
	private int transfer_type_id;
	private int transfer_status_id;
	private Double amount;
	private int account_to;
	private int account_from;

//	public Transfer(int transfer_type_id, Double amount, int account_to, int account_from) {
//		this.transfer_type_id = transfer_type_id;
//		this.transfer_status_id = transfer_status_id;
//		this.amount = amount;
//		this.account_to = account_to;
//		this.account_from = account_from;
//	}
//
//	public Transfer(int transfer_id, int transfer_type_id, Double amount, int account_to, int account_from) {
//		super();
//		this.transfer_id = transfer_id;
//		this.transfer_type_id = transfer_type_id;
//		this.transfer_status_id = transfer_status_id;
//		this.amount = amount;
//		this.account_to = account_to;
//		this.account_from = account_from;
//	}
//
//	public Transfer(int account_from, int account_to, Double amount) {
//		this.amount = amount;
//		this.account_to = account_to;
//		this.account_from = account_from;
//	}

	public int getAccount_from() {
		return account_from;
	}

	public void setAccount_from(int account_from) {
		this.account_from = account_from;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public int getAccount_to() {
		return account_to;
	}

	public void setAccount_to(int account_to) {
		this.account_to = account_to;
	}

	public int getTransfer_id() {
		return transfer_id;
	}

	public void setTransfer_id(int transfer_id) {
		this.transfer_id = transfer_id;
	}

	public int getTransfer_type_id() {
		return transfer_type_id;
	}

	public void setTransfer_type_id(int transfer_type_id) {
		this.transfer_type_id = transfer_type_id;
	}

	public int getTransfer_status_id() {
		return transfer_status_id;
	}

	public void setTransfer_status_id(int transfer_status_id) {
		this.transfer_status_id = transfer_status_id;
	}

}
