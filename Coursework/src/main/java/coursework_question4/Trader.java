/**
 * Trader.java
 */
package coursework_question4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NOTE: Due to the JUnit tests, the moment an offer that is
 * lower than the reserved price is placed, the advert is 
 * delisted and put in the unsoldCars map.
 * 
 * Defines the properties and behaviour of an trader.
 * 
 * @author as02795
 */
public class Trader extends Dealership {

  /** The list of sellers */
  private List<Seller> sellers;
  
  /** 
   * Constructor. Calls super parameterised constructor and
   * initialises the sellers list. Throws exceptions defined
   * in superclass.
   */
  public Trader(String name) {
	super(name);
    this.sellers = new ArrayList<Seller>();
  }
		
  /**
   * Method. Checks if the specified car is currently for sale.
   * No validation required as it is called locally by methods
   * which would have already performed validation.
   * 
   * @param car
   * 		  The car to check for the presence of in the 
   * 		  'for sale' map of adverts.
   * 
   * @return
   * 		  True if it is for sale, false if not.
   */
  private boolean checkExistence(Car car) {
	boolean exists = false;
	
	for (Advert advert: this.carsForSale.keySet()) {
	  if (advert.getCar() == car) {
		exists = true;
	  }
    }
  return exists;
  }
  /**
   * NOTE: Due to the JUnit tests, the moment an offer that is
   * lower than the reserved price is placed, the advert is 
   * delisted and put in the unsoldCars map.
   * 
   * Method. Called to end a listing. Checks if the car is being
   * sold. If it is, checks if the highest offer for the car is
   * above or below the reserved price of the car. If higher, it
   * moves the car to the sold cars map. If lower, the listing 
   * was unsccessful, so it is moved to the unsold cars map.
   * 
   * Not a lot of validation needs to exist in this method as
   * it is performed in the calling method, placeOffer.
   * 
   * @param advert
   * 		  the advert that is being ended.
   * 
   * @throws IllegalArgumentException
   * 		  If advert parameter is passed as null, or if advert
   * 		  is not in the map of cars being sold.
   */
  private void endSale(Advert advert) {
	if (advert.getHighestOffer().getValue() >= advert.getCar().getPrice()) {
		this.soldCars.put(advert, this.carsForSale.get(advert));
		this.carsForSale.remove(advert);
		updateStatistics((Seller) this.soldCars.get(advert));
	} else {
		  this.unsoldCars.put(advert, this.carsForSale.get(advert));
		  this.carsForSale.remove(advert);
	}
  }
  
  /**
   * Method. Returns the total car sales.
   * 
   * @return
   * 		  sum of the sales.
   */
  private int getTotalSales() {
	int noOfSales = 0;
	for (Seller seller : this.sellers) {
		 noOfSales += seller.getSales();
	}
	return noOfSales;
  }
  
  /**
   * Method. Used to place offer on a car if the advert for that
   * car is listed in the carsForSale map and the sale type of the
   * advert is FORSALE, not AUCTION. This method uses the boolean
   * return value of the placeOffer method of the Advert class. 
   * It also uses any exceptions that it throws. It then calls
   * endSale to de-list the advert.
   * 
   * @param carAdvert
   * 		  The advert to place the order on.
   * @param user
   * 		  The bidder.
   * @param value
   * 		  The amount being offered.
   * 
   * @return
   * 		  True if offer was placed, false if not placed.
   * 
   * @throws IllegalArgumentException
   * 		  If null is passed for carAdvert or user, or if
   * 		  a negative number is passed for value, or if car
   * 		  SaleType is AUCTION.
   */
  @Override
  public boolean placeOffer(Advert carAdvert, User user, double value) {
	boolean sold = false;
	if (carAdvert == null || user == null) {
		throw new IllegalArgumentException("Cannot enter null values.");
	} else if (carAdvert.getCar().getType() != SaleType.FORSALE) {
    	return sold;
	} else if (checkExistence(carAdvert.getCar())) {
		if (value < carAdvert.getCar().getPrice()) {
			return sold;
		} else {
		    sold = carAdvert.placeOffer(user, value);
	        endSale(carAdvert);
	        return sold;
		}
	} else {
	    return sold;
	}
  }
  
  /**
   * Method. Displays the statistics file contents in the specified format.
   */
  public String displayStatistics() {
	StringBuffer out = new StringBuffer("** Trader - " + getName() + "**\n");
	try {
	  BufferedReader bf = new BufferedReader(new FileReader("stats.txt"));
	  String s = bf.readLine();
	while (s != null) {
	  out.append(s + "\n");
	  s = bf.readLine();
	}
	bf.close();
	} catch (IOException e) { 
	  System.out.println("An exception occured.");
	  e.printStackTrace();
	}
	return out.toString();
  }
  
  /**
   * Method. Saves statistics in the file in the specified format.
   * 
   * @param noOfSales
   * 		  the total number of car sales.
   */
  private void saveInFile(int noOfSales) {
	try {
	  BufferedWriter writer = new BufferedWriter(new FileWriter("stats.txt"));
	  writer.write("Total Sales: " + noOfSales + "\nAll Sellers:");
	  for (Seller seller : this.sellers) {
	      writer.write("\n	" + seller.toString());
	  }
	  writer.close();
	} catch (IOException e) {
      System.out.println("Could not write to file.");
      e.printStackTrace();
	}
  }
  
  /**
   * Method. Updates the statistics of sales for the seller.
   * 
   * @param seller
   * 		  the seller who's statistics will be updated.
   */
  private void updateStatistics(Seller seller) {
	seller.addSale();
	if (!this.sellers.contains(seller)) {
	    this.sellers.add(seller);
	    Collections.sort(this.sellers, (p1, p2) -> p1.toString().compareTo(p2.toString()));
	}
	saveInFile(getTotalSales());
  }
}