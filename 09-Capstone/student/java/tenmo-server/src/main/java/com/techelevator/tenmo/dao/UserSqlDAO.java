package com.techelevator.tenmo.dao;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

@Service
public class UserSqlDAO implements UserDAO {

	private static final double STARTING_BALANCE = 1000;
	private JdbcTemplate jdbcTemplate;
	private User user = new User();

	public UserSqlDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public double getBalance(Principal principal) {
		String username = principal.getName();
		int userId = findIdByUsername(username);
		double balance = 0.0;

		String sql = "Select balance from accounts where user_id = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, userId);
		while (row.next()) {
			balance = row.getDouble("balance");

		}

		return balance;
	}

	@Override
	public int findIdByUsername(String username) {
		return jdbcTemplate.queryForObject("select user_id from users where username = ?", int.class, username);
	}

	@Override
	public List<User> findAll() {
		List<User> users = new ArrayList<>();
		String sql = "select * from users";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
		while (results.next()) {
			User user = mapRowToUser(results);
			users.add(user);
		}

		return users;
	}

	@Override
	public User findByUsername(String username) throws UsernameNotFoundException {
		for (User user : this.findAll()) {
			if (user.getUsername().toLowerCase().equals(username.toLowerCase())) {
				return user;
			}
		}
		throw new UsernameNotFoundException("User " + username + " was not found.");
	}

	@Override
	public boolean create(String username, String password) {
		boolean userCreated = false;
		boolean accountCreated = false;

		// create user
		String insertUser = "insert into users (username,password_hash) values(?,?)";
		String password_hash = new BCryptPasswordEncoder().encode(password);

		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String id_column = "user_id";
		userCreated = jdbcTemplate.update(con -> {
			PreparedStatement ps = con.prepareStatement(insertUser, new String[] { id_column });
			ps.setString(1, username);
			ps.setString(2, password_hash);
			return ps;
		}, keyHolder) == 1;
		int newUserId = (int) keyHolder.getKeys().get(id_column);

		// create account
		String insertAccount = "insert into accounts (user_id,balance) values(?,?)";
		accountCreated = jdbcTemplate.update(insertAccount, newUserId, STARTING_BALANCE) == 1;

		return userCreated && accountCreated;
	}

	private User mapRowToUser(SqlRowSet rs) {
		User user = new User();
		user.setId(rs.getLong("user_id"));
		user.setUsername(rs.getString("username"));
		user.setPassword(rs.getString("password_hash"));
		user.setActivated(true);
		user.setAuthorities("ROLE_USER");
		return user;
	}

	@Override
	public Transfer transfer(Transfer transfer) {
		String sqlBalance = "SELECT balance FROM accounts WHERE account_id = ?";
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlBalance, transfer.getAccount_from());
		double transferAmount = transfer.getAmount();
		double compare = 0.0;
		if (rs.next()) {
			compare = new Double(rs.getDouble("balance"));
		}
		if (transferAmount < compare) {
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

	private Transfer mapRowToTransfer(SqlRowSet rs) {
		int transferId = (rs.getInt("transfer_id"));
		int transferTypeId = (rs.getInt("transfer_type_id"));
		int transferStatusId = (rs.getInt("transfer_status_id"));
		int accountFrom = (rs.getInt("account_from"));
		int accountTo = (rs.getInt("account_to"));
		double amount = (rs.getDouble("amount"));
		Transfer transfer = new Transfer(transferId, transferTypeId, transferStatusId, accountFrom, accountTo, amount);
		return transfer;
	}

	@Override
	public void viewTransfers() {

		System.out.println("-----------------TRANSFERS-----------------");
		System.out.println("ID          From/To                 Amount");
		System.out.println("-------------------------------------------");

		// GET TRANSFERS FROM YOUR ACCOUNT TO ANOTHER ACCOUNT
		try {
		int accountId = 0;
		long userId = user.getId();
		String sqlForAccountId = "SELECT account_id FROM accounts WHERE user_id = ?";
		SqlRowSet rowsetForAccountId = jdbcTemplate.queryForRowSet(sqlForAccountId, userId);
		while (rowsetForAccountId.next()) {
			accountId = rowsetForAccountId.getInt("account_id");
		}

		int transferId = 0;
		double amount = 0.0;
		int accountIdToUser = 0;
		String sql = "SELECT transfer_id, account_to, amount FROM transfers WHERE account_from = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, accountId);
		while (row.next()) {
			transferId = row.getInt("transfer_id");
			amount = row.getDouble("amount");
			accountIdToUser = row.getInt("account_to");
		}

		int userIdToUser = 0;
		String sql1 = "SELECT user_id FROM accounts WHERE account_id = ?";
		SqlRowSet rowset = jdbcTemplate.queryForRowSet(sql1, accountIdToUser);
		while (rowset.next()) {
			userIdToUser = rowset.getInt("user_id");
		}

		String usernameToUser = "";
		String sql2 = "SELECT username FROM users WHERE user_id = ?";
		SqlRowSet rowset1 = jdbcTemplate.queryForRowSet(sql2, userIdToUser);
		while (rowset1.next()) {
			usernameToUser = rowset1.getNString("username");
		}

		System.out.println(transferId + "          To: " + usernameToUser + "          $ " + amount);

		// GET TRANSFERS TO YOUR ACCOUNT FROM ANOTHER ACCOUNT
		int accountIdFromUser = 0;
		String sql3 = "SELECT transfer_id, account_from, amount FROM transfers WHERE account_to = ?";
		SqlRowSet row3 = jdbcTemplate.queryForRowSet(sql3, accountId);
		while (row3.next()) {
			transferId = row.getInt("transfer_id");
			amount = row.getDouble("amount");
			accountIdFromUser = row.getInt("account_from");
		}

		int userIdFromUser = 0;
		String sql4 = "SELECT user_id FROM accounts WHERE account_id = ?";
		SqlRowSet row4 = jdbcTemplate.queryForRowSet(sql4, accountIdFromUser);
		while (row4.next()) {
			userIdFromUser = row4.getInt("user_id");
		}

		String usernameFromUser = "";
		String sql5 = "SELECT username FROM users WHERE user_id = ?";
		SqlRowSet row5 = jdbcTemplate.queryForRowSet(sql5, userIdFromUser);
		while (row5.next()) {
			usernameFromUser = row5.getNString("username");
		}

		System.out.println(transferId + "          From: " + usernameFromUser + "          $ " + amount);
		} catch (NullPointerException e) {
			System.out.println("No transfers to view.");
		}
	}

	public void viewTransferDetails(int transferId) {
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

		// get FROM username
		int userIdFrom = 0;
		String sql1 = "SELECT user_id FROM accounts WHERE account_id = ?";
		SqlRowSet row1 = jdbcTemplate.queryForRowSet(sql1, accountFrom);
		while (row1.next()) {
			userIdFrom = row1.getInt("user_id");
		}

		String usernameFrom = "";
		String sql2 = "SELECT username FROM users WHERE user_id = ?";
		SqlRowSet row2 = jdbcTemplate.queryForRowSet(sql2, userIdFrom);
		while (row2.next()) {
			usernameFrom = row2.getNString("username");
		}

		// get TO username
		int userIdTo = 0;
		String sql3 = "SELECT user_id FROM accounts WHERE account_id = ?";
		SqlRowSet row3 = jdbcTemplate.queryForRowSet(sql3, accountTo);
		while (row3.next()) {
			userIdTo = row3.getInt("user_id");
		}

		String usernameTo = "";
		String sql4 = "SELECT username FROM users WHERE user_id = ?";
		SqlRowSet row4 = jdbcTemplate.queryForRowSet(sql4, userIdTo);
		while (row4.next()) {
			usernameTo = row4.getNString("username");
		}

		// get TRANSFER TYPE

		// get TRANSFER STATUS

		System.out.println("--------------------------------------------");
		System.out.println("Transfer Details");
		System.out.println("--------------------------------------------");

		System.out.println("Id: " + transferId);
		System.out.println("From: " + usernameFrom);
		System.out.println("To: " + usernameTo);
		System.out.println("");

	}

	@Override
	public void viewPending() {
		System.out.println("-------------PENDING TRANSFERS-------------");
		System.out.println("ID          To                     Amount");
		System.out.println("-------------------------------------------");

		try {
		int transferId = 0;
		int accountIdTo = 0;
		double amount = 0.0;
		Long userId = user.getId();
		String sql = "SELECT transfer_id, account_to, amount FROM transfers WHERE account_from = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, userId);
		while (row.next()) {
			transferId = row.getInt("transfer_id");
			accountIdTo = row.getInt("account_to");
			amount = row.getDouble("amount");
		}

		int userIdTo = 0;
		String sql1 = "SELECT user_id FROM accounts WHERE account_id = ?";
		SqlRowSet row1 = jdbcTemplate.queryForRowSet(sql1, accountIdTo);
		while (row1.next()) {
			userIdTo = row1.getInt("user_id");
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
		}catch (NoSuchElementException e) {
		}
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

	public boolean updateBalance(Transfer transfer) {
		boolean result = false;
		String sql = "BEGIN TRANSACTION;" + "UPDATE accounts" + "SET balance = balance +"
				+ "(SELECT amount FROM transfers WHERE transfer_id = ? AND transfer_status = 1)" + "WHERE account_id ="
				+ "(SELECT account_to FROM transfers WHERE transfer_id = ? AND transfer_status = 1);"
				+ "UPDATE accounts" + "SET balance = balance -"
				+ "(SELECT amount FROM transfers WHERE transfer_id = ? AND transfer_status = 1)" + "WHERE account_id ="
				+ "(SELECT account_from FROM transfers WHERE transfer_id = ? AND transfer_status = 1);"
				+ "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?;" + "COMMIT;";

		int updates = jdbcTemplate.update(sql, transfer.getTransfer_id(), transfer.getTransfer_id(),
				transfer.getTransfer_id(), transfer.getTransfer_id());
		if (updates == 3) {
			result = true;
		}
		return result;
	}

}
