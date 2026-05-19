package com.g4.api.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

public class Task extends ModelAbstract {

    public static final int STATUS_BACKLOG = TaskStatus.BACKLOG.getValue();
    public static final int STATUS_TODO = TaskStatus.TODO.getValue();
    public static final int STATUS_IN_PROGRESS = TaskStatus.IN_PROGRESS.getValue();
    public static final int STATUS_DONE = TaskStatus.DONE.getValue();
    public static final int STATUS_CLOSE = TaskStatus.CLOSED.getValue();

    private String title = "";
    private String description = "";
    private LocalDateTime creationDate = null;
    private TaskStatus status = TaskStatus.BACKLOG;
    protected String table = "task";

    public Task() {}

    public Task(int id) {
        if (id > 0) {
            load(id);
        }
    }

    @Override
    public void load(int id) {
        if (loaded) {
            return;
        }
        setId(id);
        String sql =
                "SELECT status, title, description, creation_date FROM "
                        + table
                        + " WHERE task_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    setStatus(TaskStatus.fromValue(rs.getInt("status")));
                    setTitle(rs.getString("title"));
                    setDescription(rs.getString("description"));
                    setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                    loaded = true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load task: " + e.getMessage(), e);
        }
    }

    public Task setStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public Task setDescription(String description) {
        this.description = description;
        return this;
    }

    public Task setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Task setTitle(String title) {
        this.title = title;
        return this;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreationDate() {
        return creationDate != null
                ? creationDate
                : LocalDateTime.now(ZoneId.of("Europe/Paris"));
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public boolean save() {
        try {
            if (getId() <= 0) {
                String sql =
                        "INSERT INTO "
                                + table
                                + " (status, title, description, creation_date) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt =
                        getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, getStatus().getValue());
                    stmt.setString(2, getTitle());
                    stmt.setString(3, getDescription());
                    stmt.setTimestamp(4, Timestamp.valueOf(getCreationDate()));
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            setId(rs.getInt(1));
                        }
                    }
                }
            } else {
                String sql =
                        "UPDATE "
                                + table
                                + " SET status=?, title=?, description=?, creation_date=? WHERE task_id = ?";
                try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                    stmt.setInt(1, getStatus().getValue());
                    stmt.setString(2, getTitle());
                    stmt.setString(3, getDescription());
                    stmt.setTimestamp(4, Timestamp.valueOf(getCreationDate()));
                    stmt.setInt(5, getId());
                    stmt.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean delete() {
        try {
            String sql = "DELETE FROM " + table + " WHERE task_id = ?";
            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                stmt.setInt(1, getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public Map<Integer, Map<String, Object>> getAll() {
        Map<Integer, Map<String, Object>> taskList = new LinkedHashMap<>();
        String sql = "SELECT task_id, status, title, description, creation_date FROM " + table;
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int taskId = rs.getInt("task_id");
                TaskStatus taskStatus = TaskStatus.fromValue(rs.getInt("status"));
                Map<String, Object> task = new LinkedHashMap<>();
                task.put("task_id", taskId);
                task.put("status", taskStatus.getValue());
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description"));
                task.put(
                        "creation_date",
                        rs.getTimestamp("creation_date").toLocalDateTime().toString());
                taskList.put(taskId, task);
            }
        } catch (SQLException e) {
            // nothing yet
        }
        return taskList;
    }

    public Map<String, Object> toArray() {
        return Map.of(
                "task_id",
                getId(),
                "status",
                getStatus().getValue(),
                "title",
                getTitle(),
                "description",
                getDescription(),
                "creation_date",
                getCreationDate().toString());
    }
}
