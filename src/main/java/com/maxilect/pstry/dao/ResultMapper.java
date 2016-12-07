package com.maxilect.pstry.dao;

import com.maxilect.pstry.Result;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Copyright DonRiver Inc. All Rights Reserved.
 * Created on: 07.12.16
 * Created by: Oleg Maximchuk
 */
public interface ResultMapper {

    @Select("select id, TASK_ID as taskId, value from Result where task_id = #{taskId}")
    Result getResultByTaskId(@Param("taskId") long taskId);
}
