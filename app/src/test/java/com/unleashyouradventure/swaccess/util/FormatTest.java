package com.unleashyouradventure.swaccess.util;

import com.unleashyouradventure.swapi.model.SwPrice;
import com.unleashyouradventure.swapi.model.SwPrices;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class FormatTest {

    @Test
    public void nullShouldReturn0() throws Exception {
        checkNull(null);

        SwPrices swPrices = new SwPrices();
        swPrices.setPrices(null);
        checkNull(swPrices);

        swPrices.setPrices(new ArrayList<SwPrice>());
        checkNull(swPrices);

        swPrices.getPrices().add(new SwPrice());
        checkNull(swPrices);
    }

    private void checkNull(SwPrices swPrices){
        assertEquals("Expected to handle null value", "$0.00", Format.getPrice(swPrices));
    }
}