package com.example.carl.basketballshotlog2;

import org.junit.Test;

import Helpers.DateConverter;

import static org.junit.Assert.assertEquals;

public class DateConverterTest {
    @Test
    public void convertDateToText_formatsExpected() {
        DateConverter converter = new DateConverter();
        assertEquals("June 22, 2016", converter.convertDateToText("2016-06-22"));
    }
}
