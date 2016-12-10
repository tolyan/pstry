package com.maxilect.pstry.dao;

import com.maxilect.pstry.Task;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.Date;
import java.util.List;

/**
 *  Mapper of Task class, provides DB operations with 'task' entity.
 */
public interface TaskMapper {

    /**
     * Stores task with specified values into DB.
     * @param task storage object to be returned after successful insert operation.
     * @param value string value of the task
     * @param time scheduled time for execution
     * @param createdAt creation time of task
     */
    @Insert("INSERT INTO task(value, time, created_at) VALUES (#{value}, #{time}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "task.id")
    void addTask(@Param("task") Task task, @Param("value") String value, @Param("time") Date time, @Param
            ("createdAt") Date createdAt);

    /**
     * Schedules task for execution by Oracle Job Scheduler.
     * @param taskId id of task to be scheduled.
     */
    @Insert(value = "{ CALL submitTask( #{taskId, mode=IN, jdbcType=INTEGER})}")
    @Options(statementType = StatementType.CALLABLE)
    void submitTaskForBackgroundExecution(@Param("taskId") long taskId);

    /**
     * Returns of latest registered tasks. List size is specified by input parameter.
     * @param latestTasksCount defines size of the list to be returned.
     * @return List of tasks
     */
    @Select("select * from (select task.id, task.value, task.time, task.created_at as createdAt, result.value as " +
            "result from Task task  left JOIN Result result on result.task_id = task.id order by task.created_at " +
            "DESC) where rownum <= #{latestTasksCount}")
    List<Task> getLatestTasks(@Param("latestTasksCount") int latestTasksCount);

    /**
     * Returns task by specified ROWID.
     * @param rowId ROWID of the task
     * @return Task entity
     */
    @Select("select task.id, task.value, task.time, task.created_at as createdAt, result.value as result from task join result " +
            "on result.task_id = task.id where result.rowid=#{rowid}")
    Task getTask(@Param("rowid") String rowId);
}
