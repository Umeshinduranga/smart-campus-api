package com.smartcampus.filter;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.*;

@Provider
public class LoggingFilter
        implements ContainerRequestFilter,
        ContainerResponseFilter {

    private static final Logger LOG =
            Logger.getLogger(
                    LoggingFilter.class.getName());

    @Override
    public void filter(
            ContainerRequestContext req)
            throws IOException {
        LOG.info("REQUEST: "
                + req.getMethod()
                + " "
                + req.getUriInfo()
                .getRequestUri());
    }

    @Override
    public void filter(
            ContainerRequestContext req,
            ContainerResponseContext res)
            throws IOException {
        LOG.info("RESPONSE: status="
                + res.getStatus());
    }
}