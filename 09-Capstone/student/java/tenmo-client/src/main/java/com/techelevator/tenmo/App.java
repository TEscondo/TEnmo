package com.techelevator.tenmo;

import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.TransferBack;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;

public class App {

	private static final String API_BASE_URL = "http://localhost:8080/";

	private static final String MENU_OPTION_EXIT = "Exit";
	private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN,
			MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS,
			MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS,
			MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };

	private AuthenticatedUser currentUser;
	private ConsoleService console;
	private AuthenticationService authenticationService;
	public RestTemplate rest = new RestTemplate();

	public static void main(String[] args) throws Exception {
		App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
		app.run();
	}

	public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() throws Exception {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");

		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() throws Exception {
		while (true) {
			String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() throws AuthenticationServiceException {
		double balance = authenticationService.getBalance(currentUser.getToken());
		System.out.println("Your current account balance is $" + balance);
	}

	private void viewTransferHistory() throws AuthenticationServiceException {
		TransferBack[] transfers = authenticationService.viewTransfers(currentUser.getToken(),
				currentUser.getUser().getId());

		System.out.println("-------------------------------------------");
		System.out.println("Transfers");
		System.out.println("ID          From		To           Amount");
		System.out.println("-------------------------------------------");
		try {
			for (TransferBack transfer : transfers) {
				System.out.println(transfer.getTransferId() + "          " + transfer.getUsernameFrom() + "		"
						+ transfer.getUsernameTo() + "		" + transfer.getAmount());
			}
		} catch (NullPointerException e) {
			System.out.println("No previous transfers to view.");
		}
		viewTransferDetails();

	}

	private void viewTransferDetails() throws AuthenticationServiceException {
		TransferBack[] transfers = authenticationService.viewTransfers(currentUser.getToken(),
				currentUser.getUser().getId());
		Integer transferId = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
		for (int i = 0; i < transfers.length; i++) {
			if (transferId != 0 && transferId == transfers[i].getTransferId()) {
				String transferType = "";
				String transferStatus = "";
				if (transfers[i].getTransferTypeId() == 1) {
					transferType = "Request";
				} else if (transfers[i].getTransferTypeId() == 2) {
					transferType = "Send";
				}
				if (transfers[i].getTransferStatusId() == 1) {
					transferStatus = "Pending";
				} else if (transfers[i].getTransferStatusId() == 2) {
					transferStatus = "Approved";
				} else if (transfers[i].getTransferStatusId() == 3) {
					transferStatus = "Rejected";
				}

				System.out.println("-------------------------------------------");
				System.out.println("Transfer Details");
				System.out.println("-------------------------------------------");
				System.out.println("Id: " + transferId);
				System.out.println("From: " + transfers[i].getUsernameFrom());
				System.out.println("To: " + transfers[i].getUsernameTo());
				System.out.println("Type: " + transferType);
				System.out.println("Status: " + transferStatus);
				System.out.println("Amount: $" + transfers[i].getAmount());

			}

		}
	}

	private void viewPendingRequests() throws AuthenticationServiceException {
		TransferBack[] pendingTransfers = null;
		try {
			pendingTransfers = authenticationService.viewPending(currentUser.getToken(), currentUser.getUser().getId());
			System.out.println("-------------PENDING TRANSFERS-------------");
			System.out.println("ID          To                     Amount");
			System.out.println("-------------------------------------------");
			for (TransferBack pendingTransfer : pendingTransfers) {
				System.out.println(pendingTransfer.getTransferId() + "          " + pendingTransfer.getUsernameTo()
						+ "                $" + pendingTransfer.getAmount());
			}
		} catch (NullPointerException e) {
			System.out.println("You have no pending transfers.");
		}
		System.out.println("---------");
		if (pendingTransfers.length == 0) {
			System.out.println("You have no pending transfers. Enter any number to cancel.");
		}

		Integer transferId = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
		Integer choice = null;
		if (transferId != 0 && pendingTransfers.length > 0) {
			System.out.println("1: Approve");
			System.out.println("2: Reject");
			System.out.println("0: Don't approve or reject");
			System.out.println("---------");
			choice = console.getUserInputInteger("Please choose an option");
			if (choice == 1) {
				try {
					authenticationService.updatePendingApprove(currentUser.getToken(), currentUser.getUser().getId(),
							transferId);
				} catch (NullPointerException e) {
					System.out.println("Something went wrong");
				}
			} else if (choice == 2) {
				try {
					authenticationService.updatePendingReject(currentUser.getToken(), currentUser.getUser().getId(),
							transferId);
				} catch (NullPointerException e) {
					System.out.println("Something went wrong");
				}
				System.out.println("Cancelling...");
			} else if (choice == 0) {
				;
			}
		}
	}

	private void sendBucks() throws Exception {
		showUsers();
		double balance = authenticationService.getBalance(currentUser.getToken());
		Integer toUserId = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
		Double amount = console.getUserInputDouble("Enter amount");
		if (toUserId != 0 && amount < balance && toUserId != currentUser.getUser().getId()) {
			Integer fromUserId = currentUser.getUser().getId();
			int transferTypeId = 2;
			int transferStatusId = 2;
			Transfer transferProcess = new Transfer();
			transferProcess.setAccount_from(fromUserId);
			transferProcess.setAccount_to(toUserId);
			transferProcess.setAmount(amount);
			transferProcess.setTransfer_status_id(transferStatusId);
			transferProcess.setTransfer_type_id(transferTypeId);
			authenticationService.transferSend(currentUser.getToken(), transferProcess);
			System.out.println(amount + " TE Bucks were sent to user " + toUserId);
		} else if (amount > balance) {
			System.out.println("Insufficient funds...");
		} else {
			System.out.println("Cancelling transfer...");
		}

	}

	private void requestBucks() throws Exception {
		showUsers();
		int fromUserId = console.getUserInputInteger("Enter ID of user you are requesting from (0 to cancel)");
		double amount = console.getUserInputDouble("Enter amount");
		if (fromUserId != 0 && fromUserId != currentUser.getUser().getId()) {
			Integer toUserId = currentUser.getUser().getId();
			int transferTypeId = 1;
			int transferStatusId = 1;
			Transfer transferProcess = new Transfer();
			transferProcess.setAccount_from(fromUserId);
			transferProcess.setAccount_to(toUserId);
			transferProcess.setAmount(amount);
			transferProcess.setTransfer_status_id(transferStatusId);
			transferProcess.setTransfer_type_id(transferTypeId);
			authenticationService.transferRequest(currentUser.getToken(), transferProcess);
			System.out.println(amount + " TE Bucks were requested from user " + fromUserId);
		} else {
			System.out.println("Cancelling transfer...");
		}

	}

	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while (!isAuthenticated()) {
			String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
		while (!isRegistered) // will keep looping until user is registered
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				authenticationService.register(credentials);
				isRegistered = true;
				System.out.println("Registration successful. You can now login.");
			} catch (AuthenticationServiceException e) {
				System.out.println("REGISTRATION ERROR: " + e.getMessage());
				System.out.println("Please attempt to register again.");
			}
		}
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) // will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: " + e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}

	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}

	public void showUsers() throws Exception {
		System.out.println("--------------------------");
		System.out.println("Users");
		System.out.println("ID     NAME");
		System.out.println("--------------------------");
		User[] users = authenticationService.getUsers(currentUser.getToken());
		for (User u : users) {
			System.out.println(u.getId() + "     " + u.getUsername());
		}
		System.out.println("--------------------------");

	}
}
