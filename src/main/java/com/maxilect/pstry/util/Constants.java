package com.maxilect.pstry.util;

import java.text.SimpleDateFormat;


/**
 * Class to store constants.
 */
public class Constants {
    /**
     * Defines date/time format to be used.
     */
    public static final ThreadLocal<SimpleDateFormat> YYYY_MM_DD_HH_MM_SSS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
        }
    };
}
