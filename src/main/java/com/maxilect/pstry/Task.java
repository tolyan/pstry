package com.maxilect.pstry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.maxilect.pstry.util.Constants;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String value;
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    private Date time;
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    private Date createdAt;
    private String result;

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

    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    public Date getTime() {
        return time;
    }

    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    public void setTime(Date time) {
        this.time = time;
    }

    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    public Date getCreatedAt() {
        return createdAt;
    }

    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Task [id=")
                .append(id)
                .append(", value=")
                .append(value)
                .append(", result=")
                .append(result)
                .append(", time=")
                .append(getTimeStr(time))
                .append(", createdAt=")
                .append(getTimeStr(createdAt))
                .append("]")
                .toString();
    }


}
