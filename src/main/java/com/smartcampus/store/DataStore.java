package com.smartcampus.store;
import com.smartcampus.model.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    public static final Map<String, Room> rooms
            = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors
            = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readings
            = new ConcurrentHashMap<>();
}