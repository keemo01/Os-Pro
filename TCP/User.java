package TCP;
public class User {
    private String name;
    private int id;
    private String email;
    private String password;
    private String address;
    private double balance;

    // Constructor
    public User(String name, int id, String email, String password, String address, double balance) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.password = password;
        this.address = address;
        this.balance = balance;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public double getBalance() {
        return balance;
    }

    // Setter methods if needed

    // toString method
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                ", balance=" + balance +
                '}';
    }
}
