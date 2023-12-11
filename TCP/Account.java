package TCP;

public class Account {
    private String userId;
    private String[] userDetails; // Other details: name, email, password, address, etc.
    private double currentBalance;

    public Account(String userId, String[] userDetails, double initialBalance) {
        this.userId = userId;
        this.userDetails = userDetails;
        this.currentBalance = initialBalance;
    }

    // Getters and setters for user details and balance
    public String getUserId() {
        return userId;
    }

    public String[] getUserDetails() {
        return userDetails;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double newBalance) {
        this.currentBalance = newBalance;
    }
}
