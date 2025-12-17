package com.example.smartpark;

import com.google.firebase.firestore.PropertyName;

public class Parking {
    private String nombre;
    private String direccion;
    private int plazas;
    private int plazasLibres;
    private double latitud;
    private double longitud;
    private String id;
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }


    public Parking() {}

    public Parking(String nombre, String direccion, int plazas, int plazasLibres, double latitud, double longitud) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.plazas = plazas;
        this.plazasLibres = plazasLibres;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public int getPlazas() { return plazas; }
    public int getPlazasLibres() { return plazasLibres; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
}
