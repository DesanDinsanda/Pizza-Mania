package com.example.pizza_mania.model;

public class AdapterItem {
    private String name;
    private Double lat;
    private Double lon;

    public AdapterItem(String name, Double lat, Double lon){
        this.name=name;
        this.lat=lat;
        this.lon=lon;
    }

    public String getName(){return this.name;}
    public Double getLat(){return this.lat;}
    public Double getLon(){return this.lon;}

    @Override
    public String toString(){
        return this.name;
    }
}
