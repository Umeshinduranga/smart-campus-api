package com.smartcampus.exception;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(
            SensorUnavailableException e) {
        return Response.status(403)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", 403,
                        "error", "Forbidden",
                        "message", e.getMessage()))
                .build();
    }
}