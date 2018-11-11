/**
 * 
 */
package auctionhouse;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author pbj
 *
 */
public class MoneyTest {

    @Test    
    public void testAdd() {
        Money val1 = new Money("12.34");
        Money val2 = new Money("0.66");
        Money result = val1.add(val2);
        assertEquals("13.00", result.toString());
    }
    

    /*
     ***********************************************************************
     * BEGIN MODIFICATION AREA
     ***********************************************************************
     * Add all your JUnit tests for the Money class below.
     */
    
    @Test
    public void testSubtract() {
        Money val1 = new Money("14.50");
        Money val2 = new Money("8.25");
        Money result = val1.subtract(val2);
        assertEquals("6.25", result.toString());
    }
    
    @Test
    public void testAddPercent() {
        Money val = new Money("25.00");
        Money result = val.addPercent(10.00);
        assertEquals("27.50", result.toString());
        
    }
    
    @Test
    public void testToString() {
        Money val = new Money("25.111111");
        Money result = val.toString();
        assertEquals("25.11",result);
    }
    
    @Test
    public void testCompareTo() {
        Money val1 = new Money("1.00");
        Money val2 = new Money("2.50");
        Money val3 = new Money("0.25");
        Money val4 = new Money("2.50");
        // if x == y, returns 0
        int equal = val2.compareTo(val4);
        assertEquals(0, equal);
        // if x < y, returns a value less than 0
        int lesser = val1.compareTo(val2);
        assertTrue(lesser < 0);
        // if x > y, returns a value greater than 0
        int greater = val1.compareTo(val3);
        assertTrue(greater > 0);
    }
    
    @Test 
    public void testLessEqual() {
        // returns True if lesser
        Money val1 = new Money("12.50");
        Money val2 = new Money("13.25");
        boolean less = val1.lessEqual(val2);
        assertTrue(less);
        boolean greater = val2.lessEqual(val1);
        assertFalse(greater);
    }
    
    @Test 
    public void testEquals() {
        Money val1 = new Money("7.50");
        Money val2 = new Money("1.25");
        Money val3 = new Money("7.50");
        double val4 = 7.50;
        boolean equals = val1.equals(val3);
        boolean notequals1 = val1.equals(val2);
        boolean notequals2 = val1.equals(val4);
        assertTrue(equals);
        assertFalse(notequals1 && notequals1);
        
    }
    
    // changes from pounds to pence
    @Test
    public void testHashCode() {
        Money val = new Money("32.75");
        Money result = val.hashCode();
        assertEquals("3275", result.toString());
    }

    /*
     * Put all class modifications above.
     ***********************************************************************
     * END MODIFICATION AREA
     ***********************************************************************
     */


}
