package com.maxilect.pstry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;

/**
 * Copyright DonRiver Inc. All Rights Reserved.
 * Created on: 08.12.16
 * Created by: Oleg Maximchuk
 */
public class TimestampDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        return new Date(Long.valueOf(text));
    }
}
