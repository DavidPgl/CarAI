package s0564478.navigation;

public class LevelZone {
    public enum ZoneType {SLOW_ZONE, FAST_ZONE}

    public final ZoneType zoneType;

    public LevelZone(ZoneType zoneType) {
        this.zoneType = zoneType;
    }
}
