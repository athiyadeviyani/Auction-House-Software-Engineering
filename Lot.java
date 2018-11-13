package auctionhouse;

public class Lot extends CatalogueEntry {
    
    public String sellerName;
    public Money reservePrice;
    
    public Lot(String sellerName, int lotNumber, String description, Money reservePrice, LotStatus status) {
        super(lotNumber, description, status);
        this.sellerName = sellerName;
        this.reservePrice = reservePrice;
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
}
