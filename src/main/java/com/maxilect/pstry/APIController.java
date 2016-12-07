/**
 * Copyright © Anatoly Rybalchenko, 2016
 * a.rybalchenko@gmail.com
 */
package com.maxilect.pstry;

import com.maxilect.pstry.dao.ResultMapper;
import com.maxilect.pstry.dao.TaskMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class APIController {
    final static Logger logger = Logger.getLogger(APIController.class);

    private static final Long STUB_ID = 1l;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ResultMapper resultMapper;

    @RequestMapping(method = RequestMethod.POST, value = "/task")
    ResponseEntity<?> addTask(@RequestBody Task task) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(STUB_ID).toUri();
        Date createdAt = new Date();
        logger.debug("RECIVED: " + task);
        Calendar calendar = Calendar.getInstance();
        taskMapper.addTask(task.getValue(), task.getTime(), createdAt);
        task.setCreatedAt(createdAt);
        return ResponseEntity.created(location).build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/result/{taskId}")
    Result getResult(@PathVariable Long taskId) {
        return resultMapper.getResultByTaskId(taskId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/task/latest")
    List<Task> getLatestTasks() {
//        List<Task> result = new ArrayList<>();
//        result.add(new Task(2l, "anothertask", new Date(), new Date()));
//        return result;
        List<Task> result =  taskMapper.getLatestTasks(7);
        return result;
    }


}
