package com.maxilect.pstry;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.maxilect.pstry.util.Constants;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String value;
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date time;
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date createdAt;

    public Task() {

    }

    public Task(Long id, String value, Date time, Date createdAt) {
        this.value = value;
        this.id = id;
        this.time = time;
        this.createdAt = createdAt;
    }

    private String getTimeStr(Date time) {
        if (time == null) {
            return null;
        }
        return Constants.YYYY_MM_DD_HH_MM_SSS.get().format(time);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonDeserialize(using = TimestampDeserializer.class)
    public Date getTime() {
        return time;
    }

    @JsonDeserialize(using = TimestampDeserializer.class)
    public void setTime(Date time) {
        this.time = time;
    }

    @JsonDeserialize(using = TimestampDeserializer.class)
    public Date getCreatedAt() {
        return createdAt;
    }

    @JsonDeserialize(using = TimestampDeserializer.class)
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Task [id=")
                .append(id)
                .append(", value=")
                .append(value)
                .append(", time=")
                .append(getTimeStr(time))
                .append(", createAt=")
                .append(getTimeStr(createdAt))
                .append("]")
                .toString();
    }


}
