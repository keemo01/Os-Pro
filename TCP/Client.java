package TCP;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            boolean loggedIn = false;

            while (true) {
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

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (choice) {
                        // Inside the client while loop, under case 1 (Lodge money)
                        case 1:
                            // Lodge money
                            out.writeObject("GET_BALANCE");
                            System.out.print("Enter your ID: "); // Prompt for ID
                            String userId = scanner.nextLine(); // Read the user's ID
                            out.writeObject(userId); // Send the user's ID to the server

                            // Request balance from the server
                            out.writeObject("REQUEST_BALANCE");
                            double currentBalance = in.readDouble(); // Read the current balance from the server
                            System.out.println("Your current balance is: " + currentBalance);

                            System.out.print("Enter the amount to lodge: ");
                            double amountToLodge = scanner.nextDouble();
                            scanner.nextLine(); // Consume newline

                            // Send the request to lodge money to the server
                            out.writeObject("LODGE_MONEY");
                            out.writeObject(userId); // Send the user's ID
                            out.writeDouble(amountToLodge);

                            // Read the updated balance from the server
                            double updatedBalance = in.readDouble();
                            System.out.println("Updated balance: " + updatedBalance);
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
                        case 5:
                            // Update password
                            // Implement functionality to update the user's password
                            // Example:
                            System.out.print("Enter new password: ");
                            String newPassword = scanner.nextLine();
                            out.writeObject("UPDATE_PASSWORD");
                            out.writeObject(newPassword);
                            String updateResponse = (String) in.readObject();
                            System.out.println("Server: " + updateResponse);
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