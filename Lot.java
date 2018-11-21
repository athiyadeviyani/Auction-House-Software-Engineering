package auctionhouse;

import java.util.ArrayList;
import java.util.List;

public class Lot extends CatalogueEntry {
    
    private String sellerName;
    private Money reservePrice;
    private List<Buyer> interestedBuyers;
    private Buyer highestBidder;
    private Money highestBid;
    private Auctioneer auctioneer;
    
    public Lot(String sellerName, int lotNumber, String description, Money reservePrice, LotStatus status) {
        super(lotNumber, description, status);
        this.sellerName = sellerName;
        this.reservePrice = reservePrice;
        this.interestedBuyers = new ArrayList<Buyer>();
        this.highestBid = new Money("0");
    }
    
    public String getSellerName() {
        return sellerName;
    }
    
    public int getLotNumber() {
        return lotNumber;
    }
    
    public LotStatus getLotStatus() {
        return status;
    }
    
    public void setInAuction() {
        status = LotStatus.IN_AUCTION;
    }
    
    public void setSold() {
        status = LotStatus.SOLD;
    }
    
    public void setUnsold() {
        status = LotStatus.UNSOLD;
    }
    
    public void setPendingPayment() {
        status = LotStatus.SOLD_PENDING_PAYMENT;
    }
    
    public Money getReservePrice() {
        return reservePrice;
    }
    
    public void addBuyer(Buyer interestedBuyer) {
        interestedBuyers.add(interestedBuyer);
    }
    
    public void setHighestBidder(Buyer highestBidder) {
        this.highestBidder = highestBidder;
    }
    
    public List<Buyer> getInterestedBuyers(){
        return interestedBuyers;
    }
    
    public void setHighestBid(Money highestBid) {
        this.highestBid = highestBid;
    }
    
    public Money getHighestBid() {
        return highestBid;
    }

    public Buyer getHighestBidder() {
        return highestBidder;
    }

    public Auctioneer getAuctioneer() {
        return auctioneer;
    }

    public void setAuctioneer(Auctioneer auctioneer) {
        this.auctioneer = auctioneer;
    }
    
    
}
