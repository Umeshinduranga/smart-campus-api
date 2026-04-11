package com.smartcampus.resource;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> list =
                DataStore.readings.getOrDefault(
                        sensorId, new ArrayList<>());
        return Response.ok(list).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        String status = DataStore.sensors
                .get(sensorId).getStatus();
        if ("MAINTENANCE".equalsIgnoreCase(status)) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId
                            + " is under maintenance.");
        }
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(
                System.currentTimeMillis());
        DataStore.readings
                .computeIfAbsent(sensorId,
                        k -> new ArrayList<>())
                .add(reading);
        // side effect: update parent sensor's currentValue
        DataStore.sensors.get(sensorId)
                .setCurrentValue(reading.getValue());
        return Response.status(201)
                .entity(reading).build();
    }
}