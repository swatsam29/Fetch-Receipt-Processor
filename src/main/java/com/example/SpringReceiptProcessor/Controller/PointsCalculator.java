package com.example.SpringReceiptProcessor.Controller;

import com.example.SpringReceiptProcessor.Model.ReceiptsData;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointsCalculator {

    static Logger logger = LoggerFactory.getLogger(MainController.class);

    public static PointsBreakdown calculatePoints(ReceiptsData receipt) {
        int totalPoints = 0;
        List<String> breakdownDetails = new ArrayList<>();

        
        totalPoints += calculateAlphanumericPoints(receipt.getRetailer());
        breakdownDetails.add(String.format("%d points - retailer name has %d alphanumeric characters",
                totalPoints, calculateAlphanumericPoints(receipt.getRetailer())));

        
        if (isRoundDollarAmount(receipt.getTotal())) {
            totalPoints += 50;
            breakdownDetails.add("50 points - total is a round dollar amount with no cents");
        }

        if (isMultipleOfQuarter(receipt.getTotal())) {
            totalPoints += 25;
            breakdownDetails.add("25 points - total is a multiple of 0.25");
        }

        int itemPoints = calculateItemPoints(receipt.getItems().size());
        totalPoints += itemPoints;
        breakdownDetails.add(String.format("%d points - %d items (%d pairs @ 5 points each)", itemPoints,
                receipt.getItems().size(), itemPoints / 5));

        for (ReceiptsData.Item item : receipt.getItems()) {
            BigDecimal itemPrice = item.getPrice();
            int descriptionPoints = calculateDescriptionPoints(item.getShortDescription(), itemPrice);
            totalPoints += descriptionPoints;

            String breakdownDetail = String.format("%d Points - \"%s\" is %d characters (a multiple of 3) " +
                            "item price of %.2f * 0.2 = %.2f, rounded up is %d points",
                    descriptionPoints, item.getShortDescription(), item.getShortDescription().trim().length(),
                    itemPrice, itemPrice.multiply(BigDecimal.valueOf(0.2)).doubleValue(), descriptionPoints);

            breakdownDetails.add(breakdownDetail);
        }

        if (isValidPurchaseDateTime(receipt.getPurchaseDateTime())) {
            totalPoints += 6; // Rule 6
            breakdownDetails.add("6 points - purchase day is odd");

            totalPoints += 10; // Rule 7
            breakdownDetails.add("10 points - time of purchase is after 2:00 pm and before 4:00 pm");
        } else {
            logger.error("Invalid PurchaseDateTime. Check data initialization.");
        }

        breakdownDetails.add(" + ---------");
        breakdownDetails.add(String.format(" = %d points", totalPoints));

        return new PointsBreakdown(totalPoints, breakdownDetails);
    }

    private static int calculateAlphanumericPoints(String retailer) {
        return retailer.replaceAll("[^a-zA-Z0-9]", "").length();
    }

    private static boolean isRoundDollarAmount(BigDecimal total) {
        return total.stripTrailingZeros().scale() <= 0;
    }

    private static boolean isMultipleOfQuarter(BigDecimal total) {
        return total.remainder(new BigDecimal("0.25")).equals(BigDecimal.ZERO);
    }

    private static int calculateItemPoints(int totalItems) {
        return (totalItems / 2) * 5;
    }

    private static int calculateDescriptionPoints(String description, BigDecimal price) {
        int descriptionLength = description.trim().length();
        if (descriptionLength % 3 == 0) {
            double itemPrice = price.doubleValue();
            return (int) Math.ceil(itemPrice * 0.2);
        }
        return 0;
    }

    private static boolean isValidPurchaseDateTime(LocalDateTime purchaseDateTime) {
        return isDayOdd(purchaseDateTime) && isBusinessHours(purchaseDateTime);
    }
    
    private static boolean isDayOdd(LocalDateTime localDateTime) {
        int day = localDateTime.getDayOfMonth();
        return day % 2 != 0;
    }
    
    private static boolean isBusinessHours(LocalDateTime localDateTime) {
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        LocalTime startTime = LocalTime.of(14, 0); // 2:00pm
        LocalTime endTime = LocalTime.of(16, 0);   // 4:00pm
    
         return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY &&
                localDateTime.toLocalTime().isAfter(startTime) && localDateTime.toLocalTime().isBefore(endTime);
    }
    
    
    

}
