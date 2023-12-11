package TCP;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;

public class Server {
    private static final int SERVER_PORT = 4000;
    private static HashMap<String, String[]> accounts = new HashMap<>();
    private static HashSet<String> emailSet = new HashSet<>();
    private static HashSet<String> idSet = new HashSet<>();
    private static HashMap<Socket, String> loggedInUsers = new HashMap<>();
    private static final String DATABASE_FILE = "TCP/database.txt";

    public static void main(String[] args) {
        loadAccounts();
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
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

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

    private static boolean checkDuplicateEmail(String email) {
        for (String[] userDetails : accounts.values()) {
            if (userDetails[1].equals(email)) {
                return true;
            }
        }
        return false;
    }

    private static void loadAccounts() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATABASE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userInfo = line.split(",");
                String userId = userInfo[0];
                String[] userDetails = new String[userInfo.length - 1];
                System.arraycopy(userInfo, 1, userDetails, 0, userDetails.length);
                accounts.put(userId, userDetails);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveAccounts() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATABASE_FILE))) {
            for (String userId : accounts.keySet()) {
                String[] userDetails = accounts.get(userId);
                StringBuilder line = new StringBuilder(userId);
                for (String detail : userDetails) {
                    line.append(",").append(detail);
                }
                bw.write(line.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    
}
