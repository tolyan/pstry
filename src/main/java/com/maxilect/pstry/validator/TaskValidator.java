package com.maxilect.pstry.validator;

import com.maxilect.pstry.Task;
import org.springframework.util.StringUtils;

/**
 * Validation logic for data incoming from client side.
 */
public class TaskValidator {

    private static final int MAX_STR_LENGTH = 20;
    private static final int MAX_TIME_LENGTH_MINS = 5 * 60 * 1000;

    /**
     * Validates task
     * @param task task to be validated
     * @return true for valid tasks and false otherwise.
     */
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
        if (task.getCreatedAt() == null) {
            return false;
        }
        return task.getTime().getTime() - task.getCreatedAt().getTime() < MAX_TIME_LENGTH_MINS;
    }
}
