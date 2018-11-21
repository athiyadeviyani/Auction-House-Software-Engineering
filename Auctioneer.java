package auctionhouse;

public class Auctioneer {
    public String name;
    public String address;
    
    public Auctioneer(String name, String address) {
        this.name = name;
        this.address = address;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
}
