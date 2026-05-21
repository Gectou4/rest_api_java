package com.g4.api.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTask extends ModelAbstract {

    private static final Logger log = LoggerFactory.getLogger(UserTask.class);

    private int userId = 0;
    private final Map<Integer, Task> taskList = new LinkedHashMap<>();

    public UserTask() {}

    public UserTask(int id) {
        if (id > 0) {
            load(id);
        }
    }

    @Override
    protected String getTableName() {
        return "user_task";
    }

    @Override
    public void load(int id) {
        if (!loaded) {
            loadByUserId(id);
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    @Override
    public int getId() {
        return getUserId();
    }

    @Override
    public void setId(int id) {
        setUserId(id);
    }

    public int[] getTaskIds() {
        return taskList.keySet().stream().mapToInt(Integer::intValue).toArray();
    }

    public Map<Integer, Task> getTaskList() {
        return taskList;
    }

    public UserTask addTaskId(int taskId) {
        taskList.put(taskId, new Task(taskId));
        return this;
    }

    public UserTask addTask(Task task) {
        taskList.put(task.getId(), new Task(task.getId()));
        return this;
    }

    public UserTask removeTask(Task task) {
        taskList.remove(task.getId());
        return this;
    }

    public UserTask removeTaskId(int taskId) {
        taskList.remove(taskId);
        return this;
    }

    public static UserTask getTaskByUser(User user) {
        UserTask instance = new UserTask();
        instance.loadByUser(user);
        return instance;
    }

    public boolean hasTask(int taskId) {
        return taskList.containsKey(taskId);
    }

    public boolean save() {
        try {
            getConnection().setAutoCommit(false);

            try (PreparedStatement stmt =
                    getConnection().prepareStatement("DELETE FROM user_task WHERE user_id = ?")) {
                stmt.setInt(1, getUserId());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt =
                    getConnection()
                            .prepareStatement(
                                    "INSERT INTO user_task (user_id, task_id) VALUES (?, ?)")) {
                for (int taskId : taskList.keySet()) {
                    stmt.setInt(1, getUserId());
                    stmt.setInt(2, taskId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            getConnection().commit();
            return true;
        } catch (SQLException e) {
            log.error("Failed to save user tasks for user {}: {}", getUserId(), e.getMessage(), e);
            try {
                getConnection().rollback();
            } catch (SQLException rollbackEx) {
                log.warn(
                        "Rollback failed after save error: {}",
                        rollbackEx.getMessage(),
                        rollbackEx);
            }
            return false;
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                log.warn("Failed to reset auto-commit: {}", e.getMessage(), e);
            }
        }
    }

    public boolean deleteUserTask(int taskId) {
        try {
            getConnection().setAutoCommit(false);

            try (PreparedStatement stmt =
                    getConnection()
                            .prepareStatement(
                                    "DELETE FROM user_task WHERE user_id = ? AND task_id = ?")) {
                stmt.setInt(1, getUserId());
                stmt.setInt(2, taskId);
                stmt.executeUpdate();
            }

            getConnection().commit();
            return true;
        } catch (SQLException e) {
            log.error(
                    "Failed to delete user task {} for user {}: {}",
                    taskId,
                    getUserId(),
                    e.getMessage(),
                    e);
            try {
                getConnection().rollback();
            } catch (SQLException rollbackEx) {
                log.warn(
                        "Rollback failed after delete error: {}",
                        rollbackEx.getMessage(),
                        rollbackEx);
            }
            return false;
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                log.warn("Failed to reset auto-commit: {}", e.getMessage(), e);
            }
        }
    }

    public Map<String, Object> toArray() {
        Map<Integer, Map<String, Object>> tasks = new LinkedHashMap<>();
        for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
            tasks.put(entry.getKey(), entry.getValue().toArray());
        }
        return Map.of("user_id", getUserId(), "tasks", tasks);
    }

    protected void loadByUser(User user) {
        loadByUserId(user.getId());
    }

    protected void loadByUserId(int userId) {
        if (loaded) {
            return;
        }
        setId(userId);
        String sql = "SELECT task_id FROM user_task WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addTaskId(rs.getInt("task_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user tasks: " + e.getMessage(), e);
        }
        loaded = true;
    }
}
