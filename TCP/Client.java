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
                            // Implement functionality to retrieve and display all registered users
                            // Example:
                            out.writeObject("RETRIEVE_USERS");
                            // Receive and display the user listing from the server
                            break;
                        case 3:
                            // Transfer money to another account
                            // Implement functionality to transfer money to another account
                            // Example:
                            // Collect recipient details (email, PPS number, etc.)
                            // Send details to the server for the transfer process
                            break;
                        case 4:
                            // View all transactions
                            // Implement functionality to view all transactions on the bank account
                            // Example:
                            out.writeObject("VIEW_TRANSACTIONS");
                            // Receive and display the transactions from the server
                            break;
                        case 5: // Inside the case 5 block in the client code for updating the password
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
