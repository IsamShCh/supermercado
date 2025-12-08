package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import org.hibernate.annotations.Check;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ITEM_TICKET")
public class ItemTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDItemTicket", nullable = false, length = 36)
    private String idItemTicket;
    
    @NotNull(message = "El ticket es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTicket", nullable = false)
    private Ticket ticket;
    
    @NotNull(message = "El número de línea es obligatorio")
    @Min(value = 1, message = "El número de línea debe ser mayor que 0")
    @Column(name = "NumeroLinea", nullable = false)
    private Integer numeroLinea;
    
    @NotBlank(message = "El SKU es obligatorio")
    @Column(name = "SKU", nullable = false, length = 50)
    private String sku;
    

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(max = 200, message ="El nombre del producto no puede exceder 200 caracteres")
    @Check(constraints = "trim(nombre_producto) <> ''") // Esta restriccion generará una precondicion dentro del la base de datos. Por ende, el error que genere será a nivel de BBDD.
    @Column(name = "NombreProducto", length = 200, nullable = false)
    private String nombreProducto;
    
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", inclusive = true, message = "La cantidad debe ser mayor que 0")
    @Digits(integer = 10, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
    @Column(name = "Cantidad", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio unitario debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El precio unitario debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(name = "PrecioUnitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El descuento no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El descuento debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(name = "Descuento", precision = 10, scale = 2)
    private BigDecimal descuento;
    
    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El subtotal debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(name = "Subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El impuesto no puede ser negativo")
    @Digits(integer = 5, fraction = 2, message = "El impuesto debe tener máximo 5 dígitos enteros y 2 decimales")
    @Column(name = "Impuesto", precision = 5, scale = 2)
    private BigDecimal impuesto;
    
    @NotNull(message = "La unidad de medida es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida", length = 20, nullable = false)
    private UnidadMedida unidadMedida;
    
    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    @Column(name = "Categoria", length = 100, nullable = false)
    private String categoria;
    
    public void calcularSubtotal() {
        BigDecimal subtotalSinDescuento = precioUnitario.multiply(cantidad)
                    .setScale(2, java.math.RoundingMode.HALF_UP); // Multiplicar un numero de precision 1 con uno de precision 2 produce un numero de precision decimal 3.
        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            this.subtotal = subtotalSinDescuento.subtract(descuento);
        } else {
            this.subtotal = subtotalSinDescuento;
        }
    }
    
    @Override
    public String toString() {
        return "ItemTicket{" +
                "idItemTicket='" + idItemTicket + '\'' +
                ", numeroLinea=" + numeroLinea +
                ", sku='" + sku + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}