/**
 * 
 */
package auctionhouse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private Map<String,Buyer> buyerList = new HashMap<String,Buyer>();
    private Map<String, Seller> sellerList = new HashMap<String, Seller>();
    private Map<Integer, Lot> catalogueLot = new HashMap<Integer, Lot>();
    private Map<Integer, CatalogueEntry> catalogueEntries = new HashMap<Integer ,CatalogueEntry>();
    private Map<Integer, List<String>> interestedBuyers = new HashMap<Integer, List<String>>();
    // we can have multiple auctions at once
    private Map<Integer, String> highestBidders = new HashMap<Integer, String>();
    private Map<Integer, Money> lotBids = new HashMap<Integer, Money>();
    private Map<Integer, List<String>> auctioneers = new HashMap<Integer, List<String>>();
    
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
        
        buyerList.put(name, new Buyer(name, address, bankAccount, bankAuthCode));
        return Status.OK();
    }

    public Status registerSeller(
            String name,
            String address,
            String bankAccount) {
        logger.fine(startBanner("registerSeller " + name));
        
        sellerList.put(name, new Seller(name, address, bankAccount));
        return Status.OK();      
    }

    public Status addLot(
            String sellerName,
            int number,
            String description,
            Money reservePrice) {
        logger.fine(startBanner("addLot " + sellerName + " " + number));
        
        catalogueEntries.put(number, new CatalogueEntry(number, description, LotStatus.UNSOLD));
        catalogueLot.put(number, new Lot(sellerName, number, description, reservePrice, LotStatus.UNSOLD));
        interestedBuyers.put(number, new ArrayList<String>());
        return Status.OK();    
    }

    public List<CatalogueEntry> viewCatalogue() {
        logger.fine(startBanner("viewCatalog"));
        
        List<CatalogueEntry> catalogue = new ArrayList<CatalogueEntry>();
        
        logger.fine("Catalogue: " + catalogue.toString());
        
        //make loop
        List<Integer> keys = new ArrayList<Integer>();
        
        for (Integer key : catalogueEntries.keySet()) {
            keys.add(key);
        }
        
        Collections.sort(keys);  
        
        for(Integer key : keys) {
            catalogue.add(catalogueEntries.get(key));
        }
        
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
        
        Lot currentLot = catalogueLot.get(lotNumber);
        
        // check if the lot is not already opened
        if (currentLot.getLotStatus() == LotStatus.UNSOLD) {
         // notify the seller
            //retrieve seller
            
            String sellerAddress = sellerList.get(currentLot.getSellerName()).getSellerAddress();
            
            parameters.messagingService.auctionOpened(sellerAddress, lotNumber);
            
            
            // notify each interested buyer
            List<String> buyers = interestedBuyers.get(lotNumber);
            for (String b : buyers) {
               parameters.messagingService.auctionOpened(buyerList.get(b).getBuyerAddress(), lotNumber);
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
        
        Lot currentLot = catalogueLot.get(lotNumber);
        
        if (currentLot.getLotStatus() == LotStatus.IN_AUCTION) {
        
            Money currentBid = lotBids.get(lotNumber);
            
            if (!bid.lessEqual(currentBid)) {
                lotBids.put(lotNumber, bid);
                highestBidders.put(lotNumber, buyerName);
            }
            
            // could potentially abstract this
            String sellerAddress = sellerList.get(currentLot.getSellerName()).getSellerAddress(); 
            parameters.messagingService.bidAccepted(sellerAddress, lotNumber, bid);
            
            
            // notify each interested buyer
            List<String> buyers = interestedBuyers.get(lotNumber);
            for (String b : buyers) {
                if (b != buyerName) {
                    parameters.messagingService.bidAccepted(buyerList.get(b).getBuyerAddress(), lotNumber, bid);
                }
            }
            
            //notify auctioneer
            String auctioneerAddress = auctioneers.get(lotNumber).get(1);
            parameters.messagingService.bidAccepted(auctioneerAddress, lotNumber, bid);
            
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
        
        Lot currentLot = catalogueLot.get(lotNumber);
        
        Money finalBid = lotBids.get(lotNumber);
        
        if (currentLot.getReservePrice().lessEqual(finalBid)) {
            String buyerAccount = buyerList.get(highestBidders.get(lotNumber)).getBuyerAccount();
            String buyerAuthCode = buyerList.get(highestBidders.get(lotNumber)).getBuyerAuthCode();
            String sellerAccount = sellerList.get(catalogueLot.get(lotNumber).getSellerName()).getSellerAccount();
            Money amountBuyer = lotBids.get(lotNumber).addPercent(parameters.buyerPremium);
            Money amountSeller = lotBids.get(lotNumber).addPercent(-parameters.commission);
            Status buyertoHouse = parameters.bankingService.transfer(buyerAccount, buyerAuthCode, parameters.houseBankAccount, amountBuyer);
            Status housetoSeller = parameters.bankingService.transfer(parameters.houseBankAccount, parameters.houseBankAuthCode, sellerAccount, amountSeller);
            
            
            if(housetoSeller.kind == Status.Kind.OK) {
                currentLot.setSold();
                String sellerAddress = sellerList.get(currentLot.getSellerName()).getSellerAddress();
                
                parameters.messagingService.lotSold(sellerAddress, lotNumber);
                                
                // notify each interested buyer
                List<String> buyers = interestedBuyers.get(lotNumber);
                for (String b : buyers) {
                   parameters.messagingService.lotSold(buyerList.get(b).getBuyerAddress(), lotNumber);
                }
                
                return new Status(Status.Kind.SALE);
            } else {
                currentLot.setPendingPayment();
                return new Status(Status.Kind.SALE_PENDING_PAYMENT);
            }   
        } else {
            currentLot.setUnsold();
            String sellerAddress = sellerList.get(currentLot.getSellerName()).getSellerAddress();
            
            parameters.messagingService.lotUnsold(sellerAddress, lotNumber);
            
            // notify each interested buyer
            List<String> buyers = interestedBuyers.get(lotNumber);
            for (String b : buyers) {
               parameters.messagingService.lotUnsold(buyerList.get(b).getBuyerAddress(), lotNumber);
            }
            
            return new Status(Status.Kind.NO_SALE);
        }
        
  //      return Status.OK();  
    }
}
