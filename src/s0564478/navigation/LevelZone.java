package s0564478.navigation;

import s0564478.graph.OffsetPolygon;

public class LevelZone {
    public enum ZoneType {SLOW_ZONE, FAST_ZONE}

    public final ZoneType zoneType;
    public final OffsetPolygon zonePolygon;

    public LevelZone(ZoneType zoneType, OffsetPolygon zonePolygon) {
        this.zoneType = zoneType;
        this.zonePolygon = zonePolygon;
    }
}
