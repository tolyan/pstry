package com.maxilect.pstry;

import com.maxilect.pstry.util.Constants;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String value = "";
    private Date time = new Date();
    private Date createdAt = new Date();

    public Task() {

    }

    public Task(Long id, String value, Date time, Date createdAt) {
        this.value = value;
        this.id = id;
        this.time = time;
        this.createdAt = createdAt;
    }

    public String getTimeStr(Date time) {
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
