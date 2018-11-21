/**
 * 
 */
package auctionhouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * @author pbj
 *
 */
public class AuctionHouseImp implements AuctionHouse {

    private static Logger logger = Logger.getLogger("auctionhouse");
    private static final String LS = System.lineSeparator();
    // added code here
    private Parameters parameters;
    //Association between AuctionHouse and Buyers - map buyers to their names for easy access in methods
    private Map<String,Buyer> buyerList = new HashMap<String,Buyer>();
    //Association between AuctionHouse and Sellers - map sellers to their names for easy access in methods
    private Map<String, Seller> sellerList = new HashMap<String, Seller>();
    //Association between AuctionHouse and Lots - map lots to their lotNumbers for easy access in methods
    private Map<Integer, Lot> catalogueLot = new HashMap<Integer, Lot>();
    TreeMap<Integer, CatalogueEntry> catalogueEntries = new TreeMap<>();
    
    private String startBanner(String messageName) {
        return  LS 
          + "-------------------------------------------------------------" + LS
          + "MESSAGE IN: " + messageName + LS
          + "-------------------------------------------------------------";
    }
   
    public AuctionHouseImp(Parameters parameters) {
        this.parameters = parameters;
    }
    
    public Status registerBuyer(
            String name,
            String address,
            String bankAccount,
            String bankAuthCode) {
        logger.fine(startBanner("registerBuyer " + name));
        
        //check if buyer is registered 
        if(buyerList.get(name) == null) {
            buyerList.put(name, new Buyer(name, address, bankAccount, bankAuthCode));
            return Status.OK();    
        }
        else {
            logger.warning(startBanner("Buyer already registered"));
            return Status.error("This buyer has already been registerd");
        }
    }

    public Status registerSeller(
            String name,
            String address,
            String bankAccount) {
        logger.fine(startBanner("registerSeller " + name));
        
        //check if Seller is registered
        if(sellerList.get(name) == null) {
            sellerList.put(name, new Seller(name, address, bankAccount));
            return Status.OK();    
        }
        else {
            logger.warning(startBanner("Seller already registered"));
            return Status.error("This seller has already been registered");
        }
    }

    public Status addLot(
            String sellerName,
            int number,
            String description,
            Money reservePrice) {
        logger.fine(startBanner("addLot " + sellerName + " " + number));
        
        //check if seller is unregistered
        if(sellerList.get(sellerName) == null) {
            logger.warning(startBanner("Seller not registered"));
            return Status.error("This seller has not been registered"); 
        }
        
        catalogueEntries.put(number, new CatalogueEntry(number, description, LotStatus.UNSOLD));
        catalogueLot.put(number, new Lot(sellerName, number, description, reservePrice, LotStatus.UNSOLD));
        return Status.OK();    
    }

    public List<CatalogueEntry> viewCatalogue() {
        logger.fine(startBanner("viewCatalog"));
        
       List<CatalogueEntry> catalogue = new ArrayList<CatalogueEntry>(catalogueEntries.values());
        
        return catalogue;
    }
    
    public Status noteInterest(
            String buyerName,
            int lotNumber) {
        logger.fine(startBanner("noteInterest " + buyerName + " " + lotNumber));
       
        if(catalogueLot.get(lotNumber) == null) {
            logger.warning(startBanner("Lot not registered"));
            return Status.error("This lot has not been registerd");
        }
        
        catalogueLot.get(lotNumber).addBuyer(buyerList.get(buyerName));
       
        return Status.OK();   
    }

    public Status openAuction(
            String auctioneerName,
            String auctioneerAddress,
            int lotNumber) {
        logger.fine(startBanner("openAuction " + auctioneerName + " " + lotNumber));
        
        if(catalogueLot.get(lotNumber) == null) {
            logger.warning(startBanner("Lot not registered"));
            return Status.error("This lot has not been registerd");
        }
        
        
        Lot currentLot = catalogueLot.get(lotNumber);
        
        // check if the lot is not already opened
        if (currentLot.getLotStatus() == LotStatus.UNSOLD) {
         // notify the seller
            //retrieve seller
            
            String sellerAddress = sellerList.get(currentLot.getSellerName()).getAddress();
            
            parameters.messagingService.auctionOpened(sellerAddress, lotNumber);
            
            
            // notify each interested buyer
            List<Buyer> buyers = catalogueLot.get(lotNumber).getInterestedBuyers();
            for (Buyer b : buyers) {
               parameters.messagingService.auctionOpened(b.getAddress(), lotNumber);
            }
            
            currentLot.setAuctioneer(new Auctioneer(auctioneerName, auctioneerAddress));
            
            // change the status of the lot
            currentLot.setInAuction();
            
            return Status.OK();
        }
        
        if (currentLot.getLotStatus() == LotStatus.SOLD) {
            logger.warning(startBanner("Lot already sold"));
            return Status.error("This lot is already sold.");
        } else if (currentLot.getLotStatus() == LotStatus.IN_AUCTION) {
            logger.warning(startBanner("Lot not opened"));
            return Status.error("This lot has already been opened.");
        } else {
            logger.warning(startBanner("Lot already sold and pending payment"));
            return Status.error("This lot is already sold and is pending payment.");
        }
    }

    public Status makeBid(
            String buyerName,
            int lotNumber,
            Money bid) {
        logger.fine(startBanner("makeBid " + buyerName + " " + lotNumber + " " + bid));
        
        Lot currentLot = catalogueLot.get(lotNumber);
        
        if (currentLot.getLotStatus() == LotStatus.IN_AUCTION) {
            
            Money currentBid = currentLot.getHighestBid();
            
            //if a buyer has not noted interest in a lot he/she cannot make a bid on it
            if(!currentLot.getInterestedBuyers().contains(buyerList.get(buyerName))){
                logger.warning("Buyer has not noted interest");
                return Status.error("Buyer has not noted interest");
            }
            
            if (!bid.lessEqual(currentBid)) {
                currentLot.setHighestBid(bid);
                currentLot.setHighestBidder(buyerList.get(buyerName));;
            }
            else {
                logger.warning("Bid not high enough");
                return Status.error("Bid not high enough");
            }
            
            String sellerAddress = sellerList.get(currentLot.getSellerName()).getAddress();
            //notify Seller that the bid was accepted
            parameters.messagingService.bidAccepted(sellerAddress, lotNumber, bid);
            
            
            // notify each interested buyer
            List<Buyer> buyers = catalogueLot.get(lotNumber).getInterestedBuyers();
            for (Buyer b : buyers) {
                if (b.getName() != buyerName) {
                    parameters.messagingService.bidAccepted(b.getAddress(), lotNumber, bid);
                }
            }
            
            //notify auctioneer
            String auctioneerAddress = currentLot.getAuctioneer().getAddress();
            parameters.messagingService.bidAccepted(auctioneerAddress, lotNumber, bid);
            
            return Status.OK();
        }
        
        if (currentLot.getLotStatus() == LotStatus.SOLD) {
            logger.warning(startBanner("Lot already sold"));
            return Status.error("This lot is already sold.");
        } else if (currentLot.getLotStatus() == LotStatus.UNSOLD) {
            logger.warning(startBanner("Lot not opened"));
            return Status.error("This lot has not been opened.");
        } else {
            logger.warning(startBanner("Lot already sold and pending payment"));
            return Status.error("This lot is already sold and is pending payment.");
        }
        
    }

    public Status closeAuction(
            String auctioneerName,
            int lotNumber) {
        logger.fine(startBanner("closeAuction " + auctioneerName + " " + lotNumber));
        
        Lot currentLot = catalogueLot.get(lotNumber);
        
        
        if(currentLot == null) {
            return Status.error("This lot has not been registerd");
        }
        
        if(currentLot.status != LotStatus.IN_AUCTION) {
            return Status.error("This lot has not been opened");
        }
        
        if(currentLot.getAuctioneer().getName() != auctioneerName) {
            return Status.error("This auctioneer is not authorized to close this auction");
        }
        
        
        
        Money finalBid = currentLot.getHighestBid();
        
        if (currentLot.getReservePrice().lessEqual(finalBid)) {
            String buyerAccount = currentLot.getHighestBidder().getAccount();
            String buyerAuthCode = currentLot.getHighestBidder().getAuthCode();
            String sellerAccount = sellerList.get(catalogueLot.get(lotNumber).getSellerName()).getAccount();
            Money amountBuyer = currentLot.getHighestBid().addPercent(parameters.buyerPremium);
            Money amountSeller = currentLot.getHighestBid().addPercent(-parameters.commission);
            Status buyertoHouse = parameters.bankingService.transfer(buyerAccount, buyerAuthCode, parameters.houseBankAccount, amountBuyer);
            Status housetoSeller = parameters.bankingService.transfer(parameters.houseBankAccount, parameters.houseBankAuthCode, sellerAccount, amountSeller);
            
            //verify both transactions were okay
            if(housetoSeller.kind == Status.Kind.OK && buyertoHouse.kind == Status.Kind.OK) {
                currentLot.setSold();
                String sellerAddress = sellerList.get(currentLot.getSellerName()).getAddress();
                
                parameters.messagingService.lotSold(sellerAddress, lotNumber);
                                
                // notify each interested buyer
                List<Buyer> buyers = catalogueLot.get(lotNumber).getInterestedBuyers();
                for (Buyer b : buyers) {
                   parameters.messagingService.lotSold(b.getAddress(), lotNumber);
                }
                
                return new Status(Status.Kind.SALE);
            } else {
                //if the transactions do not go through then the sale is pending
                currentLot.setPendingPayment();
                return new Status(Status.Kind.SALE_PENDING_PAYMENT);
            }   
        } else {
            currentLot.setUnsold();
            String sellerAddress = sellerList.get(currentLot.getSellerName()).getAddress();
            
            parameters.messagingService.lotUnsold(sellerAddress, lotNumber);
            
            // notify each interested buyer
            List<Buyer> buyers = catalogueLot.get(lotNumber).getInterestedBuyers();
            for (Buyer b : buyers) {
               parameters.messagingService.lotUnsold(b.getAddress(), lotNumber);
            }
            
            return new Status(Status.Kind.NO_SALE);
        }
    }
}
