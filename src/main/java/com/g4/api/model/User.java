package com.g4.api.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class User extends ModelAbstract {

    private String email = "";
    private String name = "";
    protected String table = "user";

    public User() {
    }

    public User(int id) {
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
        String sql = "SELECT email, name FROM " + table + " WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    setName(rs.getString("name"));
                    setEmail(rs.getString("email"));
                    loaded = true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user: " + e.getMessage(), e);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getTasks() {
        return UserTask.getTaskByUser(this).toArray();
    }

    public Map<String, Object> toArray() {
        return Map.of("user_id", getId(), "name", getName(), "email", getEmail());
    }
}
