package com.maxilect.pstry.dao;

import com.maxilect.pstry.Result;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
 * Mapper of Result class, provides DB operations with 'result' entity.
 */
public interface ResultMapper {

    /**
     * Returns 'result' entity with specified 'taskId' field.
     * @param taskId  id of the related task.
     * @return result of the task with specified id.
     */
    @Select("select id, TASK_ID as taskId, value from Result where task_id = #{taskId}")
    Result getResultByTaskId(@Param("taskId") long taskId);
}
