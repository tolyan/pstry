/**
 * Copyright © Anatoly Rybalchenko, 2016
 * a.rybalchenko@gmail.com
 */
package com.maxilect.pstry;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class APIController {

    private static final Long STUB_ID = 1l;

    @RequestMapping(method = RequestMethod.POST, value = "/task")
    ResponseEntity<?> addTask(@RequestBody Task task){
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(STUB_ID).toUri();

        return ResponseEntity.created(location).build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/result/{taskId}")
    Result getResult(@PathVariable Long taskId){
        //TODO add proper DB request
        return new Result(1l, 1l, "resultvalue");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/task/latest")
    List<Task> getLatestTasks(){
        //TODO add proper DB request
        List<Task> result = new ArrayList<>();
        result.add(new Task(2l, "anothertask", new Date()));
        return result;
    }



}
