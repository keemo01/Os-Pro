package TCP;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;

class Transaction {
    String date;
    double amount;
    String type; // "LODGE" or "TRANSFER"
    String senderId;
    String recipientId;
}

public class Server { 
    private static final int SERVER_PORT = 4000;
    private static HashMap<String, String[]> accounts = new HashMap<>();
    private static HashMap<Socket, String> loggedInUsers = new HashMap<>();
    private static HashMap<String, ArrayList<Transaction>> accountTransactions = new HashMap<>();
    private static final String DATABASE_FILE = "TCP/database.txt";

    public static void main(String[] args) {
        loadAccounts(); // Load user accounts from a file
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started. Listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming connections
                System.out.println("Client connected: " + clientSocket);

                new Thread(() -> handleClient(clientSocket)).start(); //This Handles each client in a separate thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                String option = (String) in.readObject();  // This will read the details and register the user
                if (option.equals("REGISTER")) {
                    String id = (String) in.readObject();
                    String name = (String) in.readObject();
                    String email = (String) in.readObject();
                    String password = (String) in.readObject();
                    String address = (String) in.readObject();
                    String balance = (String) in.readObject();

                    if (accounts.containsKey(id)) {
                        out.writeObject("DUPLICATE_ID");
                    } else if (checkDuplicateEmail(email)) {
                        out.writeObject("DUPLICATE_EMAIL");
                    } else {
                        String[] userDetails = {name, email, password, address, balance};
                        accounts.put(id, userDetails);
                        out.writeObject("Registration successful!");
                        saveAccounts();
                    }
                } else if (option.equals("LOGIN")) {
                    String id = (String) in.readObject();
                    String password = (String) in.readObject();
                
                    if (accounts.containsKey(id) && accounts.get(id)[2].equals(password)) {
                        loggedInUsers.put(clientSocket, id);
                        out.writeObject("LOGIN_SUCCESS");
                    } else {
                        out.writeObject("LOGIN_FAILURE");
                    }
                } else if (option.equals("LODGE")) {
                    String userId = (String) in.readObject();
                    double amount = Double.parseDouble((String) in.readObject());
                
                    if (accounts.containsKey(userId)) {
                        String[] userDetails = accounts.get(userId);
                        double currentBalance = Double.parseDouble(userDetails[userDetails.length - 1]);
                        double newBalance = currentBalance + amount;
                        userDetails[userDetails.length - 1] = String.valueOf(newBalance);
                        accounts.put(userId, userDetails);
                
                        // Send the updated balance back to the client
                        out.writeObject("UPDATED_BALANCE");
                        out.writeDouble(newBalance);
                        out.writeObject("LODGE_SUCCESS");
                        saveAccounts(); // Save the updated account details
                    } else {
                        out.writeObject("USER_NOT_FOUND");
                    }
                }  else if (option.equals("UPDATE_PASSWORD")) {
                    String userId = loggedInUsers.get(clientSocket);
                    String newPassword = (String) in.readObject();
                    String confirmPassword = (String) in.readObject();
                
                    if (newPassword.equals(confirmPassword)) {
                        String[] userDetails = accounts.get(userId);
                        userDetails[2] = newPassword; // Update password in the account details
                        accounts.put(userId, userDetails);
                        out.writeObject("PASSWORD_UPDATED");
                        saveAccounts(); // Save the updated account details
                    } else {
                        out.writeObject("PASSWORD_MISMATCH");
                    }
                } else if (option.equals("RETRIEVE_USERS")) {
                    out.writeObject("USERS_LIST");
                    out.writeObject(accounts.size()); // Sending the number of users
                    
                    for (String userId : accounts.keySet()) {
                        String[] userDetails = accounts.get(userId);
                        out.writeObject(userId); // Sending user ID
                        for (String detail : userDetails) {
                            out.writeObject(detail); // Sending user details
                        }
                    }
                } 
                // Server code, inside the 'TRANSFER' block
                else if (option.equals("TRANSFER")) {
                    String senderId = (String) in.readObject();
                    String recipientDetails = (String) in.readObject(); // Email or ID
                    double amountToTransfer = Double.parseDouble((String) in.readObject());

                    // Transaction handling code integrated here
                    Transaction senderTransaction = new Transaction();
                    senderTransaction.date = LocalDate.now().toString();
                    senderTransaction.amount = amountToTransfer; // Setting the transferred amount
                    senderTransaction.type = "TRANSFER"; // Identifying the transaction as a transfer
                    senderTransaction.senderId = senderId; // Assigning sender's ID for reference
                    senderTransaction.recipientId = recipientDetails; // Assigning recipient's ID

                    ArrayList<Transaction> senderTransactions = accountTransactions.getOrDefault(senderId, new ArrayList<>());
                    senderTransactions.add(senderTransaction);
                    accountTransactions.put(senderId, senderTransactions);

                    Transaction recipientTransaction = new Transaction();
                    recipientTransaction.date = LocalDate.now().toString();
                    recipientTransaction.amount = amountToTransfer; // Setting the received amount
                    recipientTransaction.type = "TRANSFER"; // Identifys the type of transaction made
                    recipientTransaction.senderId = senderId; 
                    recipientTransaction.recipientId = recipientDetails; 

                    ArrayList<Transaction> recipientTransactions = accountTransactions.getOrDefault(recipientDetails, new ArrayList<>());
                    recipientTransactions.add(recipientTransaction);
                    accountTransactions.put(recipientDetails, recipientTransactions);
                
                    if (accounts.containsKey(senderId)) {
                        String[] senderDetails = accounts.get(senderId);
                        double senderBalance = Double.parseDouble(senderDetails[senderDetails.length - 1]);
                
                        // Check if sender has the amount of money needed to transfer 
                        if (senderBalance >= amountToTransfer) {
                            String recipientId = ""; // Find recipient ID from email or ID provided
                            boolean recipientFound = false;
                
                            // Search for recipient based on email or ID
                            for (String id : accounts.keySet()) {
                                String[] details = accounts.get(id);
                                if (details[1].equals(recipientDetails) || id.equals(recipientDetails)) {
                                    recipientId = id;
                                    recipientFound = true;
                                    break;
                                }
                            }
                
                            if (recipientFound) {
                                // Update sender's balance
                                double newSenderBalance = senderBalance - amountToTransfer;
                                senderDetails[senderDetails.length - 1] = String.valueOf(newSenderBalance);
                                accounts.put(senderId, senderDetails);
                
                                // Update recipient's balance
                                String[] recipientAccount = accounts.get(recipientId);
                                double recipientBalance = Double.parseDouble(recipientAccount[recipientAccount.length - 1]);
                                double newRecipientBalance = recipientBalance + amountToTransfer;
                                recipientAccount[recipientAccount.length - 1] = String.valueOf(newRecipientBalance);
                                accounts.put(recipientId, recipientAccount);
                
                                out.writeObject("TRANSFER_SUCCESS");
                                saveAccounts(); // Save the updated account details
                            } else {
                                out.writeObject("RECIPIENT_NOT_FOUND");
                            }
                        } else {
                            out.writeObject("INSUFFICIENT_BALANCE");
                        }
                    } else {
                        out.writeObject("SENDER_NOT_FOUND");
                    }
                } else if (option.equals("VIEW_TRANSACTIONS")) {
                    String userId = loggedInUsers.get(clientSocket);
                    ArrayList<Transaction> transactions = accountTransactions.get(userId);
                
                    if (transactions != null) {
                        out.writeObject("TRANSACTIONS_FOUND");
                        out.writeObject(transactions.size()); // Sending the number of transactions
                
                        for (Transaction transaction : transactions) {
                            // Send transaction details to the client
                            out.writeObject(transaction.date);
                            out.writeObject(transaction.amount);
                            out.writeObject(transaction.type);
                            out.writeObject(transaction.senderId);
                            out.writeObject(transaction.recipientId);
                        }
                    } else {
                        out.writeObject("NO_TRANSACTIONS");
                    }
                }
            }             
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Function to check for duplicate emails
    private static boolean checkDuplicateEmail(String email) {
        for (String[] userDetails : accounts.values()) {
            if (userDetails[1].equals(email)) {
                return true;
            }
        }
        return false;
    }

    private static void loadAccounts() {
        File file = new File(DATABASE_FILE); // Creating a File object for validation

        if (!file.exists()) {
            System.err.println("Database file does not exist!");
            return; 
        }
        try (BufferedReader br = new BufferedReader(new FileReader(DATABASE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Reading each line of user info
                String[] userInfo = line.split(",");
                String userId = userInfo[0]; // Extracting user ID
                String[] userDetails = new String[userInfo.length - 1]; // Getting user details
                System.arraycopy(userInfo, 1, userDetails, 0, userDetails.length);
                accounts.put(userId, userDetails); // Putting info into the accounts map
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    private static void saveAccounts() {
        File file = new File(DATABASE_FILE); // Creates a file

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String userId : accounts.keySet()) {
                String[] userDetails = accounts.get(userId); // gets user details
                StringBuilder line = new StringBuilder(userId);
                for (String detail : userDetails) {
                    line.append(",").append(detail); // adds the user details to the line
                }
                bw.write(line.toString()); // Writing the user info to the file
                bw.newLine(); // Moving it to the next line
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

}
