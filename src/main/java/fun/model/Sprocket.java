package fun.model;

import fun.model.Part;

import javax.persistence.Entity;

@Entity
public class Sprocket extends Part {
    public Sprocket(Double cost, String manufacturer) {
        setCost(cost);
        setManufacturer(manufacturer);
    }
}
