package com.datastax.address.demo.model;

public class Address {

    private String id;
    private String city;
    private double longitude;
    private double latitude;
    private String num;
    private String type;
    private String zipcode;

    public Address() {
    }

    public Address(String id, String city, double longitude, double latitude, String num, String type, String zipcode) {
        this.id = id;
        this.city = city;
        this.longitude = longitude;
        this.latitude = latitude;
        this.num = num;
        this.type = type;
        this.zipcode = zipcode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
