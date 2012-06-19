package com.unleashyouradventure.swaccess.util;

import java.text.DecimalFormat;

public class Format {

    private static DecimalFormat priceFormat = new DecimalFormat("#.##");

    public static String getPrice(int priceInCent) {
        double price = (double) priceInCent / 100;
        return "$" + priceFormat.format(price);
    }

}
