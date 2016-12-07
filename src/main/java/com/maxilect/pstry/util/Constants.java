package com.maxilect.pstry.util;

import java.text.SimpleDateFormat;

/**
 * Copyright DonRiver Inc. All Rights Reserved.
 * Created on: 07.12.16
 * Created by: Oleg Maximchuk
 */
public class Constants {

    public static final ThreadLocal<SimpleDateFormat> YYYY_MM_DD_HH_MM_SSS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
        }
    };
}
