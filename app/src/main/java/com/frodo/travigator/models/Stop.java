package com.frodo.travigator.models;

/**
 * Created by durgesh on 4/29/16.
 */
public class Stop {
    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getStop_pos() {
        return stop_pos;
    }

    public void setStop_pos(String stop_pos) {
        this.stop_pos = stop_pos;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public String getStop_lat() {
        return stop_lat;
    }

    public void setStop_lat(String stop_lat) {
        this.stop_lat = stop_lat;
    }

    public String getStop_lon() {
        return stop_lon;
    }

    public void setStop_lon(String stop_lon) {
        this.stop_lon = stop_lon;
    }

    private String stop_id, stop_pos, stop_name, stop_lat, stop_lon;
}
