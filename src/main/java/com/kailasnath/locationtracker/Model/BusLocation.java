package com.kailasnath.locationtracker.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BusLocation {

    @Id
    private int busId;
    private double latitude;
    private double longitude;
}
