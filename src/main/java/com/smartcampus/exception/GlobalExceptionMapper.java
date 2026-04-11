package com.smartcampus.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(
            GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {

        // Let 404s pass through as proper 404 responses
        if (e instanceof NotFoundException) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", 404);
            body.put("error", "Not Found");
            body.put("message", "The requested resource was not found");
            return Response.status(404)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(body)
                    .build();
        }

        // Everything else is a real 500
        LOG.log(Level.SEVERE, "Unexpected error", e);
        Map<String, Object> body = new HashMap<>();
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred.");
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}