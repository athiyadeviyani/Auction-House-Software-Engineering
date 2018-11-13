/**
 * 
 */
package auctionhouse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private List<Buyer> buyerList = new ArrayList<Buyer>();
    private List<Seller> sellerList = new ArrayList<Seller>();
    private List<Lot> catalogue = new ArrayList<Lot>();
    Map<Integer, List<String>> interestedBuyers = new HashMap<Integer, List<String>>();
    MockMessagingService messages = new MockMessagingService();
    // we can have multiple auctions at once
    Map<Integer, String> highestBidders = new HashMap<Integer, String>();
    Map<Integer, Money> lotBids = new HashMap<Integer, Money>();
    Map<Integer, List<String>> auctioneers = new HashMap<Integer, List<String>>();
    
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
        
        buyerList.add(new Buyer(name, address, bankAccount, bankAuthCode));
        return Status.OK();
    }

    public Status registerSeller(
            String name,
            String address,
            String bankAccount) {
        logger.fine(startBanner("registerSeller " + name));
        
        sellerList.add(new Seller(name, address, bankAccount));
        return Status.OK();      
    }

    public Status addLot(
            String sellerName,
            int number,
            String description,
            Money reservePrice) {
        logger.fine(startBanner("addLot " + sellerName + " " + number));
        
        catalogue.add(new Lot(sellerName, number, description, reservePrice, LotStatus.UNSOLD));
        return Status.OK();    
    }

    public List<CatalogueEntry> viewCatalogue() {
        logger.fine(startBanner("viewCatalog"));
        
        List<CatalogueEntry> catalogue = new ArrayList<CatalogueEntry>();
        
        logger.fine("Catalogue: " + catalogue.toString());
        return catalogue;
    }

    public Status noteInterest(
            String buyerName,
            int lotNumber) {
        logger.fine(startBanner("noteInterest " + buyerName + " " + lotNumber));
       
        List<String> newList = interestedBuyers.get(lotNumber);
        newList.add(buyerName);
        interestedBuyers.put(lotNumber, newList);
       
        return Status.OK();   
    }

    public Status openAuction(
            String auctioneerName,
            String auctioneerAddress,
            int lotNumber) {
        logger.fine(startBanner("openAuction " + auctioneerName + " " + lotNumber));
        
        Lot currentLot = null;
        
        for (Lot lot : catalogue) {
            if (lot.getLotNumber() == lotNumber) {
                currentLot = lot;
            }
        }
        // check if the lot is not already opened
        if (currentLot.getLotStatus() == LotStatus.UNSOLD) {
         // notify the seller
            // could potentially abstract this
            String sellerAddress = "";
            for (Lot lot : catalogue) {
                if (lot.getLotNumber() == lotNumber) {
                    for (Seller s : sellerList) {
                        if (s.getSellerName() == lot.getSellerName()) {
                            sellerAddress = s.getSellerAddress();
                        }
                    }
                }
            }   
            messages.auctionOpened(sellerAddress, lotNumber);
            
            
            // notify each interested buyer
            List<String> buyers = interestedBuyers.get(lotNumber);
            for (String b : buyers) {
                for (Buyer buyer : buyerList) {
                    if (b == buyer.getBuyerName()) {
                        messages.auctionOpened(buyer.getBuyerAddress(), lotNumber);
                    }
                }
            }
            
            // put the lot in the list of open lots, initialise the current bid to 0
            lotBids.put(lotNumber, new Money("0"));
            List<String> auctioneerList = new ArrayList<String>();
            auctioneerList.add(auctioneerName);
            auctioneerList.add(auctioneerAddress);
            auctioneers.put(lotNumber, auctioneerList);
            
            // change the status of the lot
            currentLot.setInAuction();
            
            return Status.OK();
        }
        
        if (currentLot.getLotStatus() == LotStatus.SOLD) {
            return Status.error("This lot is already sold.");
        } else if (currentLot.getLotStatus() == LotStatus.IN_AUCTION) {
            return Status.error("This lot has already been opened.");
        } else {
            return Status.error("This lot is already sold and is pending payment.");
        }
    }

    public Status makeBid(
            String buyerName,
            int lotNumber,
            Money bid) {
        logger.fine(startBanner("makeBid " + buyerName + " " + lotNumber + " " + bid));
        
        Lot currentLot = null;
        
        for (Lot lot : catalogue) {
            if (lot.getLotNumber() == lotNumber) {
                currentLot = lot;
            }
        }
        
        if (currentLot.getLotStatus() == LotStatus.IN_AUCTION) {
        
            Money currentBid = lotBids.get(lotNumber);
            
            if (!bid.lessEqual(currentBid)) {
                lotBids.put(lotNumber, bid);
                highestBidders.put(lotNumber, buyerName);
            }
            
            // could potentially abstract this
            String sellerAddress = "";
            for (Lot lot : catalogue) {
                if (lot.getLotNumber() == lotNumber) {
                    for (Seller s : sellerList) {
                        if (s.getSellerName() == lot.getSellerName()) {
                            sellerAddress = s.getSellerAddress();
                        }
                    }
                }
            }   
            messages.bidAccepted(sellerAddress, lotNumber, bid);
            
            
            // notify each interested buyer
            List<String> buyers = interestedBuyers.get(lotNumber);
            for (String b : buyers) {
                for (Buyer buyer : buyerList) {
                    if (b == buyer.getBuyerName() && b != buyerName) {
                        messages.bidAccepted(buyer.getBuyerAddress(), lotNumber, bid);
                    }
                }
            }
            
            //notify auctioneer
            String auctioneerAddress = auctioneers.get(lotNumber).get(1);
            messages.bidAccepted(auctioneerAddress, lotNumber, bid);
            
            return Status.OK();
        }
        
        if (currentLot.getLotStatus() == LotStatus.SOLD) {
            return Status.error("This lot is already sold.");
        } else if (currentLot.getLotStatus() == LotStatus.UNSOLD) {
            return Status.error("This lot has not been opened.");
        } else {
            return Status.error("This lot is already sold and is pending payment.");
        }
        
    }

    public Status closeAuction(
            String auctioneerName,
            int lotNumber) {
        logger.fine(startBanner("closeAuction " + auctioneerName + " " + lotNumber));
        
        Lot currentLot = null;
        
        for (Lot lot : catalogue) {
            if (lot.getLotNumber() == lotNumber) {
                currentLot = lot;
            }
        }
        
        Money finalBid = lotBids.get(lotNumber);
        
        if (currentLot.getReservePrice().lessEqual(finalBid)) {
            
        }
        
        return Status.OK();  
    }
}
