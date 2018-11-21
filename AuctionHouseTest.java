/**
 * 
 */
package auctionhouse;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pbj
 *
 */
public class AuctionHouseTest {

    private static final double BUYER_PREMIUM = 10.0;
    private static final double COMMISSION = 15.0;
    private static final Money INCREMENT = new Money("10.00");
    private static final String HOUSE_ACCOUNT = "AH A/C";
    private static final String HOUSE_AUTH_CODE = "AH-auth";

    private AuctionHouse house;
    private MockMessagingService messagingService;
    private MockBankingService bankingService;

    /*
     * Utility methods to help shorten test text.
     */
    private static void assertOK(Status status) { 
        assertEquals(Status.Kind.OK, status.kind);
    }
    private static void assertError(Status status) { 
        assertEquals(Status.Kind.ERROR, status.kind);
    }
    private static void assertSale(Status status) { 
        assertEquals(Status.Kind.SALE, status.kind);
    }
    
    private static void assertPendingPayment(Status status) { 
        assertEquals(Status.Kind.SALE_PENDING_PAYMENT, status.kind);
    }
    
    
    /*
     * Logging functionality
     */

    // Convenience field.  Saves on getLogger() calls when logger object needed.
    private static Logger logger;

    // Update this field to limit logging.
    public static Level loggingLevel = Level.ALL;

    private static final String LS = System.lineSeparator();

    @BeforeClass
    public static void setupLogger() {

        logger = Logger.getLogger("auctionhouse"); 
        logger.setLevel(loggingLevel);

        // Ensure the root handler passes on all messages at loggingLevel and above (i.e. more severe)
        Logger rootLogger = Logger.getLogger("");
        Handler handler = rootLogger.getHandlers()[0];
        handler.setLevel(loggingLevel);
    }

    private String makeBanner(String testCaseName) {
        return  LS 
                + "#############################################################" + LS
                + "TESTCASE: " + testCaseName + LS
                + "#############################################################";
    }

    @Before
    public void setup() {
        messagingService = new MockMessagingService();
        bankingService = new MockBankingService();

        house = new AuctionHouseImp(
                    new Parameters(
                        BUYER_PREMIUM,
                        COMMISSION,
                        INCREMENT,
                        HOUSE_ACCOUNT,
                        HOUSE_AUTH_CODE,
                        messagingService,
                        bankingService));

    }
    /*
     * Setup story running through all the test cases.
     * 
     * Story end point is made controllable so that tests can check 
     * story prefixes and branch off in different ways. 
     */
    private void runStory(int endPoint) {
        assertOK(house.registerSeller("SellerY", "@SellerY", "SY A/C"));       
        assertOK(house.registerSeller("SellerZ", "@SellerZ", "SZ A/C")); 
        if (endPoint == 1) return;
        
        assertOK(house.addLot("SellerY", 2, "Painting", new Money("200.00")));
        assertOK(house.addLot("SellerY", 1, "Bicycle", new Money("80.00")));
        assertOK(house.addLot("SellerZ", 5, "Table", new Money("100.00")));
        if (endPoint == 2) return;
        
        assertOK(house.registerBuyer("BuyerA", "@BuyerA", "BA A/C", "BA-auth"));       
        assertOK(house.registerBuyer("BuyerB", "@BuyerB", "BB A/C", "BB-auth"));
        assertOK(house.registerBuyer("BuyerC", "@BuyerC", "BC A/C", "BC-auth"));
        if (endPoint == 3) return;
        
        assertOK(house.noteInterest("BuyerA", 1));
        assertOK(house.noteInterest("BuyerA", 5));
        assertOK(house.noteInterest("BuyerB", 1));
        assertOK(house.noteInterest("BuyerB", 2));
        if (endPoint == 4) return;
        
        assertOK(house.openAuction("Auctioneer1", "@Auctioneer1", 1));

        messagingService.expectAuctionOpened("@BuyerA", 1);
        messagingService.expectAuctionOpened("@BuyerB", 1);
        messagingService.expectAuctionOpened("@SellerY", 1);
        messagingService.verify(); 
        if (endPoint == 5) return;
        
        Money m70 = new Money("70.00");
        assertOK(house.makeBid("BuyerA", 1, m70));
        
        messagingService.expectBidReceived("@BuyerB", 1, m70);
        messagingService.expectBidReceived("@Auctioneer1", 1, m70);
        messagingService.expectBidReceived("@SellerY", 1, m70);
        messagingService.verify();
        if (endPoint == 6) return;
        
        Money m100 = new Money("100.00");
        assertOK(house.makeBid("BuyerB", 1, m100));

        messagingService.expectBidReceived("@BuyerA", 1, m100);
        messagingService.expectBidReceived("@Auctioneer1", 1, m100);
        messagingService.expectBidReceived("@SellerY", 1, m100);
        messagingService.verify();
        if (endPoint == 7) return;
        
        assertSale(house.closeAuction("Auctioneer1",  1));
        messagingService.expectLotSold("@BuyerA", 1);
        messagingService.expectLotSold("@BuyerB", 1);
        messagingService.expectLotSold("@SellerY", 1);
        messagingService.verify();       

        bankingService.expectTransfer("BB A/C",  "BB-auth",  "AH A/C", new Money("110.00"));
        bankingService.expectTransfer("AH A/C",  "AH-auth",  "SY A/C", new Money("85.00"));
        bankingService.verify();
        
    }
    
    @Test
    public void testEmptyCatalogue() {
        logger.info(makeBanner("emptyLotStore"));

        List<CatalogueEntry> expectedCatalogue = new ArrayList<CatalogueEntry>();
        List<CatalogueEntry> actualCatalogue = house.viewCatalogue();

        assertEquals(expectedCatalogue, actualCatalogue);

    }

    @Test
    public void testRegisterSeller() {
        logger.info(makeBanner("testRegisterSeller"));
        logger.info(makeBanner("testRegisterSeller"));
        runStory(1);
    }

    @Test
    public void testRegisterSellerDuplicateNames() {
        logger.info(makeBanner("testRegisterSellerDuplicateNames"));
        runStory(1);     
        assertError(house.registerSeller("SellerY", "@SellerZ", "SZ A/C"));       
    }
    
    @Test
    //test the case where the an already registered buyer is registered again
    public void testRegisterBuyerDuplicateNames() {
        logger.info(makeBanner("testRegisterBuyerDuplicateNames"));
        runStory(3);     
        assertError(house.registerBuyer("BuyerA", "@BuyerA", "BA A/C", "BA-auth")); 
    }

    @Test
    public void testAddLot() {
        logger.info(makeBanner("testAddLot"));
        runStory(2);
    }
    
    @Test
    //test the case where an unregistered seller tries to add a lot
    public void testUnrigesteredSellerAddLot() {
        logger.info(makeBanner("testUnrigesteredSellerAddLot"));
        runStory(1);
        assertError(house.addLot("SellerN", 2, "Painting", new Money("200.00")));
    }
    
    @Test
    public void testViewCatalogue() {
        logger.info(makeBanner("testViewCatalogue"));
        runStory(2);
        
        List<CatalogueEntry> expectedCatalogue = new ArrayList<CatalogueEntry>();
        expectedCatalogue.add(new CatalogueEntry(1, "Bicycle", LotStatus.UNSOLD)); 
        expectedCatalogue.add(new CatalogueEntry(2, "Painting", LotStatus.UNSOLD));
        expectedCatalogue.add(new CatalogueEntry(5, "Table", LotStatus.UNSOLD));

        List<CatalogueEntry> actualCatalogue = house.viewCatalogue();

        assertEquals(expectedCatalogue, actualCatalogue);
    }

    @Test
    public void testRegisterBuyer() {
        logger.info(makeBanner("testRegisterBuyer"));
        runStory(3);       
    }

    @Test
    public void testNoteInterest() {
        logger.info(makeBanner("testNoteInterest"));
        runStory(4);
    }
    
    @Test
    //test the case where a buyer tries to note interest on an unregistered Lot
    public void testNoteInterestUnregLot() {
        logger.info(makeBanner("testNoteInterest"));
        runStory(3);
        assertError(house.noteInterest("BuyerA", 19));
    }
      
    @Test
    public void testOpenAuction() {
        logger.info(makeBanner("testOpenAuction"));
        runStory(5);       
    }
    
    @Test
    //test the case where an auction is opened on an unregistered lot
    public void testOpenAuctionUnregLot() {
        logger.info(makeBanner("testOpenAuction"));
        runStory(4);
        assertError(house.openAuction("Auctioneer1", "@Auctioneer1", 19));
    }
      
    @Test
    public void testMakeBid() {
        logger.info(makeBanner("testMakeBid"));
        runStory(7);
        assertError(house.closeAuction("Auctioneer1",  19));
    }
    
    @Test
    public void testMakeBidWithoutNotingInterest() {
        logger.info(makeBanner("testMakeBidWithoutNotingInterest"));
        runStory(7);
        assertError(house.makeBid("BuyerB",5, new Money("1200")));
    }
    
    @Test
    public void bidNotEnough() {
        logger.info(makeBanner("bidNotEnough"));
        runStory(7);
        assertError(house.makeBid("BuyerA",1, new Money("20")));
    }
    
    @Test
    public void testCloseAuctionUnopenedLot() {
        logger.info(makeBanner("testCloseAuctionUnopenedLot"));
        runStory(7);
        assertError(house.closeAuction("Auctioneer", 2));
    }
  
    @Test
    public void testCloseAuctionWithSale() {
        logger.info(makeBanner("testCloseAuctionWithSale"));
        runStory(8);
    }
    
    @Test
    //test the case where an auction is closed on an unregistered lot
    public void testCloseAuctionUnregLot() {
        logger.info(makeBanner("testCloseAuctionUnregLot"));
        runStory(7);
        assertError(house.closeAuction("Auctioneer1",  19));
    }
    
    @Test
    //test the case where an auction results in no sale due to the hammer price being less than the reserve price
    public void testCloseAuctionNoSale() {
        logger.info(makeBanner("testCloseAuctionNoSale"));
        runStory(7);
        house.openAuction("Auctioneer2", "@Auctioneer2", 2);
        assertError(house.closeAuction("Auctioneer1",  2));
    }
    
    @Test
    //test a transaction will result in pending payment status of the transaction with a bad account
    public void testCloseAuctionBadAccount() {
        logger.info(makeBanner("testCloseAuctionBadAccount"));
        runStory(7);
        bankingService.setBadAccount("BB A/C");
        
        assertPendingPayment(house.closeAuction("Auctioneer1",  1));
    }
    
     
    
}
