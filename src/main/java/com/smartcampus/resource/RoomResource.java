package com.smartcampus.resource;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;

@Path("/rooms")
public class RoomResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        return Response.ok(
                new ArrayList<>(DataStore.rooms.values())
        ).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room,
                               @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(400)
                    .entity(Map.of("error","id is required"))
                    .build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(409)
                    .entity(Map.of("error","Room ID already exists"))
                    .build();
        }
        DataStore.rooms.put(room.getId(), room);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(
            @PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("error","Room not found"))
                    .build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(
            @PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("error","Room not found"))
                    .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room — it still has "
                            + room.getSensorIds().size()
                            + " sensor(s) assigned.");
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}
