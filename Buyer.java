package auctionhouse;

public class Buyer extends User {
    
    public String bankAuthCode;
    
    public Buyer(String name, String address, String bankAccount, String bankAuthCode) {
        super(name, address, bankAccount);
        this.bankAuthCode = bankAuthCode;
    }

    public String getBuyerName() {
        return name;
    }
    
    public String getBuyerAddress() {
        return address;
    }
    
    public String getBuyerAuthCode() {
        return bankAuthCode;
    }
    
    public String getBuyerAccount() {
        return bankAccount;
    }

}
