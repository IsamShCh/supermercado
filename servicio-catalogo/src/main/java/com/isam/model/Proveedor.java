package com.isam.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PROVEEDORES")
public class Proveedor {

    @Id
    @Column(name = "IDProveedor", length = 50)
    private String idProveedor;

    @Column(name = "NombreProveedor", length = 200, nullable = false)
    private String nombreProveedor;

    @Column(name = "Contacto", length = 100)
    private String contacto;

    @Column(name = "Direccion", length = 300)
    private String direccion;

    @Column(name = "Telefono", length = 20)
    private String telefono;

    @Column(name = "Email", length = 100)
    private String email;

    // Constructors
    public Proveedor() {}

    public Proveedor(String idProveedor, String nombreProveedor) {
        this.idProveedor = idProveedor;
        this.nombreProveedor = nombreProveedor;
    }

    // Getters and Setters
    public String getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(String idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}