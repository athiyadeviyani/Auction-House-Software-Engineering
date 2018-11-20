/**
 * Money is a class that implements the Comparable interface. 
 * It contains simple methods for basic operations on Money
 * It contains one variable, double value, which stores the value of a monetary sum
 * 
 * @author      Athiya Deviyani
 * @author      Vasilis Ntogramatzis
 */
package auctionhouse;

/**
 * @author pbj
 */
public class Money implements Comparable<Money> {
 
    private double value;
    
    /**
     * Returns the result of a double multiplied by 100 and rounded
     * to the nearest whole number
     * 
     * @param  pounds the monetary amount
     * @return the number multiplied by 100 and rounded to the nearest
     *         whole number
     */
    private static long getNearestPence(double pounds) {
        return Math.round(pounds * 100.0);
    }
 
    /**
     * Executes getNearestPence on the input double and divides the result by 100
     * 
     * @param  pounds the monetary amount
     * @return the number multiplied by 100 and rounded to the nearest
     *         whole number and then divided by 100
     */
    private static double normalise(double pounds) {
        return getNearestPence(pounds)/100.0;
        
    }
 
    /**
     * Executes normalise on the input String which is first converted to a double
     * 
     * @param  pounds the monetary amount
     * @return the normalised amount
     */
    public Money(String pounds) {
        value = normalise(Double.parseDouble(pounds));
    }
    
    /**
     * Assigns a double amount to the value field
     * 
     * @param  pounds the monetary amount
     */
    private Money(double pounds) {
        value = pounds;
    }
    
    /**
     * Increments the value field by a monetary amount m
     * and returns a new Money object with its value field set to this value
     * 
     * @param  pounds an instance of a the Money class
     * @return A new instance of the Money class with the value field equal
     *         to the previous object's value plus the value of the input object
     */
    public Money add(Money m) {
        return new Money(value + m.value);
    }
    
    /**
     * Decrements the value field by a monetary amount m
     * and returns a new Money object with its value field set to this value
     * 
     * @param  pounds an instance of a the Money class
     * @return A new instance of the Money class with the value  field equal
     *         to the previous object's value minus the value of the input object
     */
    public Money subtract(Money m) {
        return new Money(value - m.value);
    }
 
    /**
     * Increments the value field by a certain percentage of its original value
     * and returns a new Money object with its value field set to this value
     * 
     * @param  the percentage increment
     * @return A new instance of the Money class with the value field equal
     *         to the previous object's value incremented by a percentage of its value
     */
    public Money addPercent(double percent) {
        return new Money(normalise(value * (1 + percent/100.0)));
    }
     
    /**
     * Returns this instance's value field converted to a string
     * 
     * @return the value field converted to a string
     */
    @Override
    public String toString() {
        return String.format("%.2f", value);
        
    }
    
    /**
     * Compares this instance's value field with an input instance's value field 
     * 
     * @param another instance of Money to be compared with this instance's value
     * @return greater than 0 if this instance's field is greater than the input instance's,
     *                 less if otherwise and 0 if equal
     */
    public int compareTo(Money m) {
        return Long.compare(getNearestPence(value),  getNearestPence(m.value)); 
    }
    
    /**
     * Compares this instance's value field with another instance's value field using the compareTo() method
     * 
     * @param another instance of Money
     * @return True if this instance's value field is less than the the input instance's value field
     */
    public Boolean lessEqual(Money m) {
        return compareTo(m) <= 0;
    }
    
    
    /**
     * Takes in an object, checks if that object is an instance of Money and if it is then compares the input instance 
     * to this instance and returns true if their value fields are equal. If the input is not an instance of Money 
     * the return false
     * 
     * @param an object
     * @return True if the input object is an instance of the Money class and its value field is equal to this instance's value field
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) return false;
        Money oM = (Money) o;
        return compareTo(oM) == 0;       
    }
    
    /**
     * Returns the hash code value as this instance's value field for this instance 
     * 
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Long.hashCode(getNearestPence(value));
    }
      

}
