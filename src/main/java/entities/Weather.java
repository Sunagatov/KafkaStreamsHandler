package entities;

public class Weather {
    private String id;
    private String name;
    private String geohash;
    private Integer precision;
    private Double lng;
    private Double lat;
    private Double avg_tmpr_f;
    private Double avg_tmpr_c;
    private String wthr_date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getAvg_tmpr_f() {
        return avg_tmpr_f;
    }

    public void setAvg_tmpr_f(Double avg_tmpr_f) {
        this.avg_tmpr_f = avg_tmpr_f;
    }

    public Double getAvg_tmpr_c() {
        return avg_tmpr_c;
    }

    public void setAvg_tmpr_c(Double avg_tmpr_c) {
        this.avg_tmpr_c = avg_tmpr_c;
    }

    public String getWthr_date() {
        return wthr_date;
    }

    public void setWthr_date(String wthr_date) {
        this.wthr_date = wthr_date;
    }
}
