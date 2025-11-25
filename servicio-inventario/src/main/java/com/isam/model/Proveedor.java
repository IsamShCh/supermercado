package com.isam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "PROVEEDORES")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDProveedor", length = 36)
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

    public Proveedor(String idProveedor, String nombreProveedor, String contacto,
                    String direccion, String telefono, String email) {
        this.idProveedor = idProveedor;
        this.nombreProveedor = nombreProveedor;
        this.contacto = contacto;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
    }
}