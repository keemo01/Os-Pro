package TCP;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;

public class Server {
    private static final int SERVER_PORT = 8888;
    private static HashMap<String, String> accounts = new HashMap<>();
    private static HashSet<String> emailSet = new HashSet<>();
    private static HashSet<String> idSet = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started. Listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            while (true) {
                String option = (String) in.readObject();
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
                        accounts.put(id, password);
                        out.writeObject("Registration successful!");
                    }
                } else if (option.equals("LOGIN")) {
                    String id = (String) in.readObject();
                    String password = (String) in.readObject();

                    if (accounts.containsKey(id) && accounts.get(id).equals(password)) {
                        out.writeObject("LOGIN_SUCCESS");
                    } else {
                        out.writeObject("LOGIN_FAILURE");
                    }
                } else if (option.equals("GET_BALANCE")) {
                    String userId = (String) in.readObject();
                    double balance = 0.0;
                    
                    if (accounts.containsKey(userId)) {
                        balance = Double.parseDouble(accounts.get(userId));
                    }
                    
                    out.writeDouble(balance);
                } else if (option.equals("LODGE_MONEY")) {
                    try {
                        String userId = (String) in.readObject();
                        double amountToLodge = in.readDouble();
                
                        if (accounts.containsKey(userId)) {
                            double currentBalance = Double.parseDouble(accounts.get(userId));
                            double updatedBalance = currentBalance + amountToLodge;
                            accounts.put(userId, String.valueOf(updatedBalance));
                            out.writeDouble(updatedBalance);
                        } else {
                            out.writeDouble(-1.0);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkDuplicateEmail(String email) {
        for (String storedEmail : accounts.values()) {
            if (storedEmail.equals(email)) {
                return true;
            }
        }
        return false;
    }
}
