package com.maxilect.pstry.validator;

import com.maxilect.pstry.Task;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Copyright DonRiver Inc. All Rights Reserved.
 * Created on: 08.12.16
 * Created by: Oleg Maximchuk
 */
public class TaskValidatorTest {

    @Test
    public void testValidateNullTask() {
        assertFalse(TaskValidator.isTaskValid(null));
    }

    @Test
    public void testNullValue() {
        assertFalse(TaskValidator.isTaskValid(buildTaskWithValidDates(null)));
    }

    @Test
    public void testEmptyValue() {
        assertFalse(TaskValidator.isTaskValid(buildTaskWithValidDates("")));
    }

    @Test
    public void testTooLongValue() {
        assertFalse(TaskValidator.isTaskValid(buildTaskWithValidDates("012345678901234567890")));
    }

    @Test
    public void testBoundaryLength() {
        assertTrue(TaskValidator.isTaskValid(buildTaskWithValidDates("01234567890123456789")));
    }

    @Test
    public void testValidLength() {
        assertTrue(TaskValidator.isTaskValid(buildTaskWithValidDates("123")));
    }

    @Test
    public void testMissingExecDate() {
        assertFalse(TaskValidator.isTaskValid(buildTaskWithValiValue(null, new Date())));
    }

    @Test
    public void testMissingCreatedDate() {
        assertFalse(TaskValidator.isTaskValid(buildTaskWithValiValue(new Date(), null)));
    }

    @Test
    public void testTooFarDate() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(Calendar.MINUTE, 5);
        assertFalse(TaskValidator.isTaskValid(buildTaskWithValiValue(instance.getTime(), new Date())));
    }

    private Task buildTaskWithValidDates(String value) {
        Task task = new Task();
        task.setValue(value);
        task.setCreatedAt(new Date());
        task.setTime(new Date());
        return task;
    }

    private Task buildTaskWithValiValue(Date execTime, Date createdAt) {
        Task task = new Task();
        task.setValue("123");
        task.setCreatedAt(createdAt);
        task.setTime(execTime);
        return task;
    }

}