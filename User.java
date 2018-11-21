package auctionhouse;

public class User {
    
    public String name;
    public String address;
    public String bankAccount;
    
    public User(String name, String address, String bankAccount) {
        this.name = name;
        this.address = address;
        this.bankAccount = bankAccount;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getAccount() {
        return bankAccount;
    }

}
