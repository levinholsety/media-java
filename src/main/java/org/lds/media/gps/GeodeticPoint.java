package org.lds.media.gps;

public class GeodeticPoint {

    private static final double pi = Math.PI;
    private static final double xpi = Math.PI * 3000 / 180;
    private static final double a = 6378245;// 卫星椭球坐标投影到平面地图坐标系的投影因子。
    private static final double ee = 0.00669342162296594323;// 椭球的偏心率。

    private static double lat(double x, double y) {
        double ret = -100 + 2 * x + 3 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20 * Math.sin(6 * x * pi) + 20 * Math.sin(2 * x * pi)) * 2 / 3;
        ret += (20 * Math.sin(y * pi) + 40 * Math.sin(y / 3 * pi)) * 2 / 3;
        ret += (160 * Math.sin(y / 12 * pi) + 320 * Math.sin(y * pi / 30)) * 2 / 3;
        return ret;
    }

    private static double lon(double x, double y) {
        double ret = 300 + x + 2 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20 * Math.sin(6 * x * pi) + 20 * Math.sin(2 * x * pi)) * 2 / 3;
        ret += (20 * Math.sin(x * pi) + 40 * Math.sin(x / 3 * pi)) * 2 / 3;
        ret += (150 * Math.sin(x / 12 * pi) + 300 * Math.sin(x / 30 * pi)) * 2 / 3;
        return ret;
    }

    private static boolean outsideChina(GeodeticPoint p) {
        return (p.latitude < 0.8293 || p.latitude > 55.8271) || (p.longitude < 72.004 || p.longitude > 137.8347);
    }

    private final double latitude;
    private final double longitude;

    public GeodeticPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("%.6f,%.6f", longitude, latitude);
    }

    public GeodeticPoint fromWGS84ToGCJ02() {
        if (outsideChina(this)) return new GeodeticPoint(latitude, longitude);
        double lat = lat(longitude - 105, latitude - 35);
        double lon = lon(longitude - 105, latitude - 35);
        double radLat = latitude / 180 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        lat = (lat * 180) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        lon = (lon * 180) / (a / sqrtMagic * Math.cos(radLat) * pi);
        return new GeodeticPoint(latitude + lat, longitude + lon);
    }

    public GeodeticPoint fromGCJ02ToBD09() {
        double z = Math.sqrt(latitude * latitude + longitude * longitude) + 0.00002 * Math.sin(latitude * xpi);
        double theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * xpi);
        return new GeodeticPoint(z * Math.sin(theta) + 0.006, z * Math.cos(theta) + 0.0065);
    }

    public GeodeticPoint fromBD09ToGCJ02() {
        double lat = latitude - 0.006;
        double lon = longitude - 0.0065;
        double z = Math.sqrt(lat * lat + lon * lon) - 0.00002 * Math.sin(lat * xpi);
        double theta = Math.atan2(lat, lon) - 0.000003 * Math.cos(lon * xpi);
        return new GeodeticPoint(z * Math.sin(theta), z * Math.cos(theta));
    }

    public GeodeticPoint fromGCJ02ToWGS84() {
        GeodeticPoint p = fromWGS84ToGCJ02();
        return new GeodeticPoint(latitude * 2 - p.latitude, longitude * 2 - p.longitude);
    }
}
