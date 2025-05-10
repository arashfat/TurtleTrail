package com.example.osm.model.enum

enum class ManeuverType(val code: Int) {
    TURN_LEFT(0),
    TURN_RIGHT(1),
    SHARP_LEFT(2),
    SHARP_RIGHT(3),
    SLIGHT_LEFT(4),
    SLIGHT_RIGHT(5),
    STRAIGHT(6),
    KEEP_LEFT(12),
    KEEP_RIGHT(13),
    U_TURN(8),
    ROUNDABOUT(7),
    ARRIVE(10),
    HEAD(11),
    MERGE(14),
    ON_RAMP(15),
    OFF_RAMP(16),
    END_OF_ROAD(17),
    FERRY(18),
    CONTINUE(19),
    UNKNOWN(-1);
}