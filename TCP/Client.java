package TCP;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 4000;

    public static void main(String[] args) {
        boolean continueSession = true; // Declaration of continueSession

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            boolean loggedIn = false;
            String userId = ""; // Store logged-in user ID

            while (continueSession) {
                if (!loggedIn) {
                    System.out.println("Welcome! Choose an option:");
                    System.out.println("1. Register an account");
                    System.out.println("2. Login");

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (choice == 1) {
                        // Registration
                        System.out.print("Enter PPSN: ");
                        String id = scanner.nextLine();
                        System.out.print("Enter Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Enter Password: ");
                        String password = scanner.nextLine();
                        System.out.print("Enter Address: ");
                        String address = scanner.nextLine();
                        System.out.print("Enter Balance: ");
                        String balance = scanner.nextLine();

                        // Send registration data to server
                        out.writeObject("REGISTER");
                        out.writeObject(id);
                        out.writeObject(name);
                        out.writeObject(email);
                        out.writeObject(password);
                        out.writeObject(address);
                        out.writeObject(balance);

                        String response = (String) in.readObject();
                        System.out.println("Server: " + response);
                    } else if (choice == 2) {
                        // Login
                        System.out.print("Enter PPSN: ");
                        String id = scanner.nextLine();
                        System.out.print("Enter Password: ");
                        String password = scanner.nextLine();

                        // Send login data to server
                        out.writeObject("LOGIN");
                        out.writeObject(id);
                        out.writeObject(password);

                        String response = (String) in.readObject();
                        if (response.equals("LOGIN_SUCCESS")) {
                            loggedIn = true;
                            System.out.println("Login successful!");
                            userId = id;
                        } else {
                            System.out.println("Login failed. Try again.");
                        }
                    }
                } else {
                    System.out.println("Logged in! Options:");
                    System.out.println("1. Lodge money to the user account.");
                    System.out.println("2. Retrieve all registered users listing.");
                    System.out.println("3. Transfer money to another account.");
                    System.out.println("4. View all transactions on your bank account.");
                    System.out.println("5. Update your password.");
                    System.out.println("0. Log out.");

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (choice) {
                        case 1: // Inside the case 1 block in the client code
                            System.out.print("Enter the amount you want to lodge: ");
                            double amountToLodge = scanner.nextDouble();
                            scanner.nextLine(); // Consume newline

                            // Send the lodgement details to the server
                            out.writeObject("LODGE");
                            out.writeObject(userId); // Send user ID
                            out.writeObject(String.valueOf(amountToLodge));

                            // Receive the updated balance from the server
                            String lodgeResponse = (String) in.readObject();
                            if (lodgeResponse.equals("UPDATED_BALANCE")) {
                                double updatedBalance = in.readDouble();
                                System.out.println("Updated Balance: " + updatedBalance);
                            }

                            // Receive and process server response for lodgement success
                            String lodgeSuccessResponse = (String) in.readObject();
                            System.out.println("Server: " + lodgeSuccessResponse);
                            break;
                        case 2:
                            // Retrieve all registered users listing
                            out.writeObject("RETRIEVE_USERS");

                            String usersListResponse = (String) in.readObject();
                            if (usersListResponse.equals("USERS_LIST")) {
                                int numUsers = (int) in.readObject(); //This Reads the number of users
                                System.out.println("Registered Users:");
                                
                                for (int i = 0; i < numUsers; i++) {
                                    String retrievedUserId = (String) in.readObject(); // Reading user ID
                                    String name = (String) in.readObject(); // Reading user details
                                    String email = (String) in.readObject();
                                    String password = (String) in.readObject(); // Didnt include variable as dont want passwords to be displayed
                                    String address = (String) in.readObject();
                                    String balance = (String) in.readObject();
                                    
                                    // Display user details
                                    System.out.println("User ID: " + retrievedUserId);
                                    System.out.println("Name: " + name);
                                    System.out.println("Email: " + email);
                                    System.out.println("Address: " + address);
                                    System.out.println("Balance: " + balance);
                                    System.out.println("------------------------");
                                }
                            } else {
                                System.out.println("Failed to retrieve user listing.");
                            }
                            break;
                            case 3:
                                //Transfer money to another account when providing the following recipient details: • Email Address • PPS Number
                                System.out.print("Enter recipient's email or ID: ");
                                String recipient = scanner.nextLine();
                                System.out.print("Enter the amount to transfer: ");
                                double amountToTransfer = scanner.nextDouble();
                                scanner.nextLine(); // Consume newline

                                // Send transfer details to the server
                                out.writeObject("TRANSFER");
                                out.writeObject(userId); // Send sender's ID
                                out.writeObject(recipient); // Send recipient's email or ID
                                out.writeObject(String.valueOf(amountToTransfer));

                                // Receive and process server response for the transfer
                                String transferResponse = (String) in.readObject();
                                System.out.println("Server: " + transferResponse);
                                break;

                        case 4: // View all transactions
                            out.writeObject("VIEW_TRANSACTIONS");
                            
                            String transactionResponse = (String) in.readObject();
                            if (transactionResponse.equals("TRANSACTIONS_FOUND")) {
                                int numTransactions = (int) in.readObject(); // Reading the number of transactions
                                System.out.println("Transactions:");
                                
                                for (int i = 0; i < numTransactions; i++) {
                                    String date = (String) in.readObject();
                                    double amount = (double) in.readObject();
                                    String type = (String) in.readObject();
                                    String senderId = (String) in.readObject();
                                    String recipientId = (String) in.readObject();
                                    
                                    // Display transaction details
                                    System.out.println("Date: " + date);
                                    System.out.println("Amount: " + amount);
                                    System.out.println("Type: " + type);
                                    System.out.println("Sender: " + senderId);
                                    System.out.println("Recipient: " + recipientId);
                                    System.out.println("------------------------");
                                }
                            } else {
                                System.out.println("No transactions found.");
                            }
                            break;
                    
                        case 5: // Transfer money to another account when providing the following recipient details: • Email Address • PPS Number
                            System.out.print("Enter new password: ");
                            String newPassword = scanner.nextLine();
                            System.out.print("Confirm new password: ");
                            String confirmNewPassword = scanner.nextLine();
                        
                            // Send the new password and confirmation to the server
                            out.writeObject("UPDATE_PASSWORD");
                            out.writeObject(newPassword);
                            out.writeObject(confirmNewPassword);
                        
                            // Receive and process server response for password update
                            String updateResponse = (String) in.readObject();
                            if (updateResponse.equals("PASSWORD_UPDATED")) {
                                System.out.println("Password updated successfully!");
                            } else if (updateResponse.equals("PASSWORD_MISMATCH")) {
                                System.out.println("Passwords don't match. Please try again.");
                            }
                            break;
                        case 0:
                            // Option to log out
                            System.out.println("Logged out...");
                            out.writeObject("LOGOUT"); // Inform server about logout
                            loggedIn = false; // Reset logged-in state
                            continueSession = false; // Exit the loop
                            break;
                        default:
                            System.out.println("Invalid choice. Please choose a valid option.");
                            break;
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
