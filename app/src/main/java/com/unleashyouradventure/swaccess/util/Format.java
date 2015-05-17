package com.unleashyouradventure.swaccess.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.unleashyouradventure.swapi.model.SwPrices;

public final class Format {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(new Locale("en_US")));

    private Format() {
    }

    public static String getPrice(SwPrices prices) {
        double amount = 0.00;
        if (prices != null && prices.getPrices() != null && !prices.getPrices().isEmpty()) {
            amount = prices.getPrices().get(0).getAmount();
        }
        return "$" + PRICE_FORMAT.format(amount);
    }
}
