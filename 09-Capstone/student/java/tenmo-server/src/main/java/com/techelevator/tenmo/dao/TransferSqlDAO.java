package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferBack;
import com.techelevator.tenmo.model.User;

@Service
public class TransferSqlDAO implements TransferDAO {

	private JdbcTemplate jdbcTemplate;
	private User user = new User();

	public TransferSqlDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void updatePending(int optionChoice, int transferId) {
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
	public Transfer viewTransferDetails(int transferId) {
		Transfer transfer = null;
		int transferTypeId = 0;
		int transferStatusId = 0;
		int accountFrom = 0;
		int accountTo = 0;
		double amount = 0.0;
		String sql = "SELECT * FROM transfers WHERE transfer_id = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, transferId);
		while (row.next()) {
			transferTypeId = row.getInt("transfer_type_id");
			transferStatusId = row.getInt("transfer_status_id");
			accountFrom = row.getInt("account_from");
			accountTo = row.getInt("account_to");
			amount = row.getDouble("amount");

		}
		return transfer;
	}

	@Override
	public List<TransferBack> viewTransfers(int id) {
		List<TransferBack> transferList = new ArrayList<TransferBack>();
		int transferId = 0;
		double amount = 0.0;
		Long accountIdToUser = null;
		Long accountIdFromUser = null;
		String usernameToUser = "";
		String usernameFromUser = "";
		int transferType = 0;
		int transferStatus = 0;
		

		String sql = "SELECT * FROM transfers WHERE account_from = ? OR account_to = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, id, id);
		while (row.next()) {
			transferId = row.getInt("transfer_id");
			amount = row.getDouble("amount");
			accountIdToUser = row.getLong("account_to");
			accountIdFromUser = row.getLong("account_from");
			transferType = row.getInt("transfer_type_id");
			transferStatus = row.getInt("transfer_status_id");

			String sql2 = "SELECT username FROM users WHERE user_id = ?";
			SqlRowSet rowset1 = jdbcTemplate.queryForRowSet(sql2, accountIdToUser);
			while (rowset1.next()) {
				usernameToUser = rowset1.getString("username");
			}

			String sql3 = "SELECT username FROM users WHERE user_id = ?";
			SqlRowSet rowset2 = jdbcTemplate.queryForRowSet(sql3, accountIdFromUser);
			while (rowset2.next()) {
				usernameFromUser = rowset2.getString("username");
			}

			TransferBack transfer = new TransferBack(transferId, usernameToUser, usernameFromUser, amount, transferType, transferStatus);
			transferList.add(transfer);
		}

		return transferList;
	}

	@Override
	public Transfer transfer(Transfer transfer) {
		Double balance = 0.0;
		String sqlBalance = "SELECT balance FROM accounts WHERE account_id = ?;";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlBalance, transfer.getAccount_from());
		while (rs.next()) {
			balance = rs.getDouble("balance");
		}
		Double transferAmount = transfer.getAmount();
		if (transferAmount < balance) {
			String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount)"
					+ "VALUES (2,1,?,?,?) RETURNING transfer_id";
			SqlRowSet rs2 = jdbcTemplate.queryForRowSet(sql, transfer.getAccount_from(), transfer.getAccount_to(),
					transfer.getAmount());
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

		Double amount = transfer.getAmount();
		int accountTo = transfer.getAccount_to();

		try {
			// update balance for accountTo
			Double orgBalance = 0.0;
			String sql = "SELECT balance FROM accounts WHERE account_id = ?";
			SqlRowSet row = jdbcTemplate.queryForRowSet(sql, accountTo);
			while (row.next()) {
				orgBalance = row.getDouble("balance");
			}
			Double newAmt = orgBalance + amount;
			String sql1 = "UPDATE accounts SET balance = ? WHERE account_id = ?";
			SqlRowSet row1 = jdbcTemplate.queryForRowSet(sql1, newAmt, accountTo);
		} catch (Exception e) {
			itWorked = false;
		}
		return itWorked;
	}

	@Override
	public boolean updateBalance1(Transfer transfer) {
		boolean itWorked = true;
		int transferId = transfer.getTransfer_id();
		Double amount = transfer.getAmount();
		int accountFrom = transfer.getAccount_from();
		int accountTo = transfer.getAccount_to();

		try {

			// update balance for accountFrom
			Double orgBalance1 = 0.0;
			String sql2 = "SELECT balance FROM accounts WHERE account_id = ?";
			SqlRowSet row2 = jdbcTemplate.queryForRowSet(sql2, accountFrom);
			while (row2.next()) {
				orgBalance1 = row2.getDouble("balance");
			}
			Double newAmt1 = orgBalance1 - amount;

			String sql3 = "UPDATE accounts SET balance = ? WHERE account_id = ?";
			SqlRowSet row3 = jdbcTemplate.queryForRowSet(sql3, newAmt1, accountFrom);

			// update transfer status
			String sql4 = "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?";
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
		Transfer transfer = new Transfer(transferId, transferTypeId, transferStatusId, accountFrom, accountTo, amount);
		return transfer;
	}

	@Override
	public List<TransferBack> viewPending(int id) {
		List<TransferBack> pendingList = new ArrayList<TransferBack>();
		int transferId = 0;
		double amount = 0.0;
		Long accountIdToUser = null;
		Long accountIdFromUser = null;
		String usernameToUser = "";
		String usernameFromUser = "";
		int transferType = 0;
		int transferStatus = 0;

		String sql = "SELECT transfer_id, account_to, account_from, amount FROM transfers WHERE account_from = ? OR account_to = ? AND transfer_status_id = 1";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, id, id);
		while (row.next()) {
			transferId = row.getInt("transfer_id");
			amount = row.getDouble("amount");
			accountIdToUser = row.getLong("account_to");
			accountIdFromUser = row.getLong("account_from");

			String sql2 = "SELECT username FROM users WHERE user_id = ?";
			SqlRowSet rowset1 = jdbcTemplate.queryForRowSet(sql2, accountIdToUser);
			while (rowset1.next()) {
				usernameToUser = rowset1.getString("username");
			}

			String sql3 = "SELECT username FROM users WHERE user_id = ?";
			SqlRowSet rowset2 = jdbcTemplate.queryForRowSet(sql3, accountIdFromUser);
			while (rowset2.next()) {
				usernameFromUser = rowset2.getString("username");
			}

			TransferBack transfer = new TransferBack(transferId, usernameToUser, usernameFromUser, amount);
			pendingList.add(transfer);
		}

		return pendingList;
	}

}