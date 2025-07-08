package com.javafx.terminmanagement;

import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.ListIterator;

public class TaskList {
    private LinkedList<Task> taskList;

    public TaskList() {
        taskList = new LinkedList<Task>();
    }

    public boolean add(Task task) {
        return taskList.add(task);
    }

    public boolean remove(Task task) {
        return taskList.remove(task);
    }

    public Task getTask(int index) {
        return taskList.get(index);
    }
}
