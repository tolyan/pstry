/**
 * Copyright © Anatoly Rybalchenko, 2016
 * a.rybalchenko@gmail.com
 */
package com.maxilect.pstry;

import com.maxilect.pstry.dao.ResultMapper;
import com.maxilect.pstry.dao.TaskMapper;
import com.maxilect.pstry.validator.TaskValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class APIController {
    final static Logger logger = Logger.getLogger(APIController.class);

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ResultMapper resultMapper;

    @Autowired
    private SocketController socketController;

    @RequestMapping(method = RequestMethod.POST, value = "/task")
    ResponseEntity<?> addTask(@RequestBody Task task) {
        logger.debug("RECIVED: " + task);
        if (!TaskValidator.isTaskValid(task)) {
            logger.debug("BAD REST REQUEST: " + task);
            return ResponseEntity.badRequest().build();
        }
        taskMapper.addTask(task, task.getValue(), task.getTime(), task.getCreatedAt());
        taskMapper.submitTaskForBackgroundExecution(task.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(task.getId()).toUri();
        socketController.broadcastChange();
        return ResponseEntity.created(location).build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/result/{taskId}")
    Result getResult(@PathVariable Long taskId) {
        return resultMapper.getResultByTaskId(taskId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/task/latest")
    List<Task> getLatestTasks() {
        return taskMapper.getLatestTasks(7);
    }


}
