package com.maxilect.pstry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.maxilect.pstry.util.Constants;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String value;
    @DateTimeFormat()
    private Date time;
    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

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
