package auctionhouse;

public class Seller extends User {

    public Seller(String name, String address, String bankAccount) {
        super(name, address, bankAccount);
    }
    
    public String getSellerName() {
        return name;
    }
    
    public String getSellerAddress() {
        return address;
    }
}
