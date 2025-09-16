package com.example.pizza_mania.model;

import java.util.List;

public class AutoCompleteResult {
    public String type;
    public List<Feature> features;
    public class Feature{
        public String type;
        public Properties properties;
        public Geometry geometry;
    }

    public class Properties{
        public String osm_type;
        public String osm_id;
        public String osm_key;
        public String osm_value;
        public String type;
        public String countrycode;
        public String name;
        public String country;
        public String state;
        public String county;
        public List<Double> extent;
    }
    public class Geometry{
        public String type;
        public List<Double> coordinates;
    }
}
