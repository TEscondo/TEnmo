package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

@Service
public class TransferSqlDAO implements TransferDAO{

	private JdbcTemplate jdbcTemplate;
	private User user = new User();

	public TransferSqlDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	
	@Override
	public void updatePending(int optionChoice, long transferId) {
		if (optionChoice == 1) {
			String sql = "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?";
			jdbcTemplate.update(sql, transferId);
			System.out.println("The request has been approved.");
		}
		if (optionChoice == 2) {
			String sql = "UPDATE transfers SET transfer_status_id = 3 WHERE transfer_id = ?";
			jdbcTemplate.update(sql, transferId);
			System.out.println("The request has been rejected.");
		}
	}

	@Override
	public void viewTransferDetails(int transferId) {
		int transferTypeId = 0;
		int transferStatusId = 0;
		int accountFrom = 0;
		int accountTo = 0;
		double amount = 0.0;
		String sql = "SELECT * FROM transfers WHERE transfer_id = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, transferId);
		while(row.next()) {
			transferTypeId = row.getInt("transfer_type_id");
			transferStatusId = row.getInt("transfer_status_id");
			accountFrom = row.getInt("account_from");
			accountTo = row.getInt("account_to");
			amount = row.getDouble("amount");
		}
		
		//get FROM username
		int userIdFrom = 0;
		String sql1 = "SELECT user_id FROM accounts WHERE account_id = ?";
		SqlRowSet row1 = jdbcTemplate.queryForRowSet(sql1, accountFrom);
		while(row1.next()) {
			userIdFrom = row1.getInt("user_id");
		}
		
		String usernameFrom = "";
		String sql2 = "SELECT username FROM users WHERE user_id = ?";
		SqlRowSet row2 = jdbcTemplate.queryForRowSet(sql2, userIdFrom);
		while(row2.next()) {
			usernameFrom = row2.getNString("username");
		}
		
		//get TO username
		int userIdTo = 0;
		String sql3 = "SELECT user_id FROM accounts WHERE account_id = ?";
		SqlRowSet row3 = jdbcTemplate.queryForRowSet(sql3, accountTo);
		while(row3.next()) {
			userIdTo = row3.getInt("user_id");
		}
		
		String usernameTo = "";
		String sql4 = "SELECT username FROM users WHERE user_id = ?";
		SqlRowSet row4 = jdbcTemplate.queryForRowSet(sql4, userIdTo);
		while(row4.next()) {
			usernameTo = row4.getNString("username");
		}
		
		//get TRANSFER TYPE
		
		
		//get TRANSFER STATUS
		
		System.out.println("--------------------------------------------");
		System.out.println("Transfer Details");
		System.out.println("--------------------------------------------");
		
		System.out.println("Id: " + transferId);
		System.out.println("From: " + usernameFrom);
		System.out.println("To: " + usernameTo);
		System.out.println("");
		
	}

	@Override
public void viewTransfers() {
		
		System.out.println("-----------------TRANSFERS-----------------");
		System.out.println("ID          From/To                 Amount");
		System.out.println("-------------------------------------------");

		System.out.println("here");
		
		// GET TRANSFERS FROM YOUR ACCOUNT TO ANOTHER ACCOUNT
		int accountId = 0;
		Long userId = user.getId();
		int transferId = 0;
		double amount = 0.0;
		int accountIdToUser = 0;
		int userIdToUser = 0;
		String usernameToUser = "";
		int accountIdFromUser = 0;
		int userIdFromUser = 0;
		String usernameFromUser = "";
		
		try {	
		
			String sqlForAccountId = "SELECT account_id FROM accounts WHERE user_id = ?";
			SqlRowSet rowsetForAccountId = jdbcTemplate.queryForRowSet(sqlForAccountId, userId);
			while (rowsetForAccountId.next()) {
				accountId = rowsetForAccountId.getInt("account_id");
			}
			String sql = "SELECT transfer_id, account_to, amount FROM transfers WHERE account_from = ?";
			SqlRowSet row = jdbcTemplate.queryForRowSet(sql, accountId);
			while (row.next()) {
				transferId = row.getInt("transfer_id");
				amount = row.getDouble("amount");
				accountIdToUser = row.getInt("account_to");
			}

			String sql1 = "SELECT user_id FROM accounts WHERE account_id = ?";
			SqlRowSet rowset = jdbcTemplate.queryForRowSet(sql1, accountIdToUser);
			while (rowset.next()) {
				userIdToUser = rowset.getInt("user_id");
			}

			String sql2 = "SELECT username FROM users WHERE user_id = ?";
			SqlRowSet rowset1 = jdbcTemplate.queryForRowSet(sql2, userIdToUser);
			while (rowset1.next()) {
				usernameToUser = rowset1.getNString("username");
			}

			System.out.println(transferId + "          To: " + usernameToUser + "          $ " + amount);

			// GET TRANSFERS TO YOUR ACCOUNT FROM ANOTHER ACCOUNT
			String sql3 = "SELECT transfer_id, account_from, amount FROM transfers WHERE account_to = ?";
			SqlRowSet row3 = jdbcTemplate.queryForRowSet(sql3, accountId);
			while (row3.next()) {
				transferId = row.getInt("transfer_id");
				amount = row.getDouble("amount");
				accountIdFromUser = row.getInt("account_from");
			}
			
			String sql4 = "SELECT user_id FROM accounts WHERE account_id = ?";
			SqlRowSet row4 = jdbcTemplate.queryForRowSet(sql4, accountIdFromUser);
			while (row4.next()) {
				userIdFromUser = row4.getInt("user_id");
			}
			
			String sql5 = "SELECT username FROM users WHERE user_id = ?";
			SqlRowSet row5 = jdbcTemplate.queryForRowSet(sql5, userIdFromUser);
			while (row5.next()) {
				usernameFromUser = row5.getNString("username");
			}

			System.out.println(transferId + "          From: " + usernameFromUser + "          $ " + amount);

		} catch (NullPointerException e) {
			System.out.println("Hey we caught the error here!");
		}
			
	}


	@Override
	public Transfer transfer(Transfer transfer) {
		Double balance = 0.0;
		String sqlBalance = "SELECT balance FROM accounts WHERE account_id = ?;";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlBalance, transfer.getAccount_from());
		while(rs.next()) {
			balance = rs.getDouble("balance");
		}
		Double transferAmount = transfer.getAmount();
		if (transferAmount < balance ) {
			String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
					"VALUES (2,1,?,?,?) RETURNING transfer_id";
			SqlRowSet rs2 = jdbcTemplate.queryForRowSet(sql, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
			if (rs2.next()) {
				transfer.setTransfer_id(rs2.getInt("transfer_id"));
			}
			
			return transfer;
			
		} else {
			System.out.println("Insufficient funds");
			return null;
		}
	
	}

	@Override
	public boolean updateBalance(Transfer transfer) {
		
		boolean itWorked = true;
		
		int transferId = transfer.getTransfer_id();
		Double amount = transfer.getAmount();
		int accountFrom = transfer.getAccount_from();
		int accountTo = transfer.getAccount_to();
		
		try {
		// update balance for accountTo
		Double orgBalance = 0.0;
		String sql = "SELECT balance FROM accounts WHERE account_id = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, accountTo);
		while(row.next()){
			orgBalance = row.getDouble("balance");
		}
		
		Double newAmt = orgBalance + amount;
		
		String sql1 = "UPDATE accounts SET balance = ? WHERE account_id = ?";
		SqlRowSet row1 = jdbcTemplate.queryForRowSet(sql1, newAmt, accountTo);
		
		// update balance for accountFrom
		Double orgBalance1 = 0.0;
		String sql2 = "SELECT balance FROM accounts WHERE account_id = ?";
		SqlRowSet row2 = jdbcTemplate.queryForRowSet(sql2, accountFrom);
		while(row2.next()) {
			orgBalance1 = row.getDouble("balance");
		}
		Double newAmt1 = orgBalance1 - amount;
		
		String sql3 = "UPDATE accounts SET balance = ? WHERE account_id = ?";
		SqlRowSet row3 = jdbcTemplate.queryForRowSet(sql3, newAmt1, accountFrom);
		
		// update transfer status
		String  sql4 = "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?";
		SqlRowSet row4 = jdbcTemplate.queryForRowSet(sql4, transferId);
		
		} catch (Exception e) {
			itWorked = false;
		}
		return itWorked;
	}
	
	private Transfer mapRowToTransfer(SqlRowSet rs) {
		int transferId = (rs.getInt("transfer_id"));
		int transferTypeId = (rs.getInt("transfer_type_id"));
		int transferStatusId = (rs.getInt("transfer_status_id"));
		int accountFrom = (rs.getInt("account_from"));
		int accountTo = (rs.getInt("account_to"));
		Double amount = (rs.getDouble("amount"));
		Transfer transfer = new Transfer(transferId, transferTypeId,transferStatusId,accountFrom,accountTo,amount);
		return transfer;
	}
	
	@Override
	public void viewPending() {
		System.out.println("-------------PENDING TRANSFERS-------------");
		System.out.println("ID          To                     Amount");
		System.out.println("-------------------------------------------");

		long transferId = 0;
		try {
			long accountIdTo = 0;
			Double amount = 0.0;
			long userId = user.getId();
			String sql = "SELECT transfer_id, account_to, amount FROM transfers WHERE account_from = ? AND transfer_status_id = 1";
			SqlRowSet row = jdbcTemplate.queryForRowSet(sql, userId);
			while (row.next()) {
				transferId = row.getLong("transfer_id");
				accountIdTo = row.getLong("account_to");
				amount = row.getDouble("amount");
			}

			long userIdTo = 0;
			String sql1 = "SELECT user_id FROM accounts WHERE account_id = ?";
			SqlRowSet row1 = jdbcTemplate.queryForRowSet(sql1, accountIdTo);
			while (row1.next()) {
				userIdTo = row1.getLong("user_id");
			}

			String usernameTo = "";
			String sql2 = "SELECT username FROM users WHERE user_id = ?";
			SqlRowSet row2 = jdbcTemplate.queryForRowSet(sql2, userIdTo);
			while (row2.next()) {
				usernameTo = row2.getNString("username");
			}

			System.out.println(transferId + "          " + usernameTo + "                $" + amount);
			System.out.println("---------");
			System.out.println("Please enter transfer ID to approve/reject (0 to cancel): ");
		} catch (NullPointerException e) {
		} catch (NoSuchElementException e) {
		}
		try {
		Scanner scanner = new Scanner(System.in);
		String inputTransferId = scanner.nextLine();
		int transferIdChoice = 0;
		try {
			transferIdChoice = Integer.parseInt(inputTransferId);
		} catch (NumberFormatException e) {
			System.out.println("Invalid input.");
		}
		if (transferIdChoice != 0) {
			System.out.println("1: Approve");
			System.out.println("2: Reject");
			System.out.println("0: Don't approve or reject");
			System.out.println("---------");
			System.out.println("Please choose an option: ");
			String inputOption = scanner.nextLine();
			int optionChoice = 0;
			try {
				optionChoice = Integer.parseInt(inputOption);
			} catch (NumberFormatException e) {
				System.out.println("Invalid input.");
			}
			updatePending(optionChoice, transferId);
		} else {
			System.out.println("Cancelling...");
		}
		scanner.close();
		} catch (NoSuchElementException e) {
			
		}
	}

	
	
	
}
