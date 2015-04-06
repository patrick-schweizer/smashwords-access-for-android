package com.unleashyouradventure.swaccess.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.unleashyouradventure.swapi.model.SwPrices;

public class Format {

    private static DecimalFormat priceFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(new Locale("en_US")));

    public static String getPrice(SwPrices prices) {
        double amount = 0.00;
        if (prices != null && prices.getPrices().size() > 0)
            amount = prices.getPrices().get(0).getAmount();
        return "$" + priceFormat.format(amount);
    }

}
