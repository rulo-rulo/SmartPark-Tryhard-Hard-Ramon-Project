package com.example.smartpark;

public class ReservaUsuario {
    private String id;
    private String parkingId;
    private String nombreParking;
    private String fecha;
    private String estado;

    public ReservaUsuario() {}

    public ReservaUsuario(String parkingId, String nombreParking, String fecha, String estado) {
        this.parkingId = parkingId;
        this.nombreParking = nombreParking;
        this.fecha = fecha;
        this.estado = estado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParkingId() { return parkingId; }
    public String getNombreParking() { return nombreParking; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
}
