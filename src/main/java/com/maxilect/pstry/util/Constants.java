package com.maxilect.pstry.util;

import java.text.SimpleDateFormat;

public class Constants {

    public static final ThreadLocal<SimpleDateFormat> YYYY_MM_DD_HH_MM_SSS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
        }
    };
}
