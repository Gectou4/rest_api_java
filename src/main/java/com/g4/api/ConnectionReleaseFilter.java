package com.g4.api;

import com.g4.api.db.DB;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ConnectionReleaseFilter implements ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(ConnectionReleaseFilter.class);

    @Override
    public void filter(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        try {
            DB.releaseConnection();
        } catch (Exception e) {
            log.warn("Failed to release DB connection: {}", e.getMessage(), e);
        }
    }
}
