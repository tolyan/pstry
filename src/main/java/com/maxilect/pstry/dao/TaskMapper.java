package com.maxilect.pstry.dao;

import com.maxilect.pstry.Task;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * Copyright DonRiver Inc. All Rights Reserved.
 * Created on: 07.12.16
 * Created by: Oleg Maximchuk
 */
public interface TaskMapper {

    @Insert("INSERT INTO task(value, time, created_at) VALUES (#{value}, #{time}, #{createdAt})")
    void addTask(@Param("value") String value, @Param("time") Date time, @Param("createdAt") Date createdAt);

    @Select("select id, value, time, created_at as createdAt from Task WHERE ROWNUM <= #{latestTasksCount}")
    List<Task> getLatestTasks(@Param("latestTasksCount") int latestTasksCount);
}
