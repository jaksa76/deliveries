package com.zuhlke.deliveries;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Car {
    @Id @GeneratedValue public Long id;
    public String make;
    public String model;
    public String color;
}
