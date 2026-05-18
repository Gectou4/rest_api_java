package com.g4.api.model;

import com.g4.api.db.DB;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class ModelAbstract {

    protected int id = 0;
    protected String table = "";
    protected String dbServer = "master";
    protected boolean loaded = false;

    protected Connection getConnection() throws SQLException {
        return DB.getInstance(dbServer);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public abstract void load(int id);
}
