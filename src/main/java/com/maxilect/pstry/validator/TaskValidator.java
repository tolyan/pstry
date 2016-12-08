package com.maxilect.pstry.validator;

import com.maxilect.pstry.Task;
import org.springframework.util.StringUtils;

/**
 * Copyright DonRiver Inc. All Rights Reserved.
 * Created on: 08.12.16
 * Created by: Oleg Maximchuk
 */
public class TaskValidator {

    private static final int MAX_STR_LENGTH = 20;
    private static final int MAX_TIME_LENGTH_MINS = 5 * 60 * 1000;

    public static boolean isTaskValid(Task task) {
        if (task == null) {
            return false;
        }
        return isValueValid(task) && isTimeValid(task);
    }

    private static boolean isValueValid(Task task) {
        return !StringUtils.isEmpty(task.getValue()) && task.getValue().length() <= MAX_STR_LENGTH;
    }

    private static boolean isTimeValid(Task task) {
        if (task.getTime() == null) {
            return false;
        }
        //NOTE createdAt field is always filled on backend
        return task.getTime().getTime() - task.getCreatedAt().getTime() < MAX_TIME_LENGTH_MINS;
    }
}
