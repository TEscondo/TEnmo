package com.techelevator.tenmo.dao;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

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
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }

        return users;
    }

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        for (User user : this.findAll()) {
            if( user.getUsername().toLowerCase().equals(username.toLowerCase())) {
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
                    PreparedStatement ps = con.prepareStatement(insertUser, new String[]{id_column});
                    ps.setString(1, username);
                    ps.setString(2,password_hash);
                    return ps;
                }
                , keyHolder) == 1;
        int newUserId = (int) keyHolder.getKeys().get(id_column);

        // create account
        String insertAccount = "insert into accounts (user_id,balance) values(?,?)";
        accountCreated = jdbcTemplate.update(insertAccount,newUserId,STARTING_BALANCE) == 1;

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
	
	private Transfer mapRowToTransfer(SqlRowSet rs) {
		int transferId = (rs.getInt("transfer_id"));
		int transferTypeId = (rs.getInt("transfer_type_id"));
		int transferStatusId = (rs.getInt("transfer_status_id"));
		int accountFrom = (rs.getInt("account_from"));
		int accountTo = (rs.getInt("account_to"));
		double amount = (rs.getDouble("amount"));
		Transfer transfer = new Transfer(transferId, transferTypeId,transferStatusId,accountFrom,accountTo,amount);
		return transfer;
	}

	@Override
	public boolean updateBalance(Transfer transfer) {
		boolean result = false;
		String sql = "BEGIN TRANSACTION;"
		+ "UPDATE accounts" +
		"SET balance = balance +" +
		"(SELECT amount FROM transfers WHERE transfer_id = ? AND transfer_status = 1)" +
		"WHERE account_id =" +
		"(SELECT account_to FROM transfers WHERE transfer_id = ? AND transfer_status = 1);" +
		"UPDATE accounts" +
		"SET balance = balance -" + 
		"(SELECT amount FROM transfers WHERE transfer_id = ? AND transfer_status = 1)" +
		"WHERE account_id =" +
		"(SELECT account_from FROM transfers WHERE transfer_id = ? AND transfer_status = 1);" +
		"UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?;"
		+ "COMMIT;";
		
		int updates = jdbcTemplate.update(sql, transfer.getTransfer_id(), transfer.getTransfer_id(), transfer.getTransfer_id(), transfer.getTransfer_id());
		if (updates == 3) {
			result = true;
		}
		return result;
	}
	
	

}
