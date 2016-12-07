package com.maxilect.pstry;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Task implements Serializable {

  private static final long serialVersionUID = 1L;
  private Long id;
  private String value = "";
  private Date time = new Date();
  
  public Task() {
    
  }
  
  public Task(Long id, String value, Date time) {
    this.value = value;
    this.id = id;
    this.time = time;
  }
  
  private DateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
  
  public String getTimeStr() {
    return df.format(time);
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

  @Override
  public String toString() {
    return "Task [id=" + id + ", value=" + value + ", time=" + getTimeStr() + "]";
  }
  
  
}
