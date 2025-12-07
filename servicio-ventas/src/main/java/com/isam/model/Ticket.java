package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TICKETS")
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDTicket", nullable = false, length = 36)
    private String idTicket;
    
    @Size(max = 50, message = "El número de ticket debe tener máximo 50 caracteres")
    @Column(name = "NumeroTicket", unique = true, length = 50)
    private String numeroTicket;
    
    @NotBlank(message = "El ID de usuario es obligatorio")
    @Size(max = 36, message = "El ID de usuario debe tener máximo 36 caracteres")
    @Column(name = "IDUsuario", nullable = false, length = 36)
    private String idUsuario;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "FechaHora", nullable = false)
    private LocalDateTime fechaHora;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El subtotal debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "Subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El total de impuestos no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El total de impuestos debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "TotalImpuestos", precision = 10, scale = 2)
    private BigDecimal totalImpuestos;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El total no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El total debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "Total", precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(name = "IDPago", length = 36)
    private String idPago;
    
    @NotNull(message = "El estado del ticket es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "EstadoTicket", nullable = false, length = 20)
    private EstadoTicket estadoTicket;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemTicket> items = new ArrayList<>();

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Pago pago;
    
    
    public void addItem(ItemTicket item) {
        items.add(item);
        item.setTicket(this);
    }
    
    /**
     * Elimina un item del ticket y opcionalmente reordena los números de línea.
     * @param item El item a eliminar
     */
    public void removeItem(ItemTicket item) {
        items.remove(item);
        item.setTicket(null);
        
        // TODO: Descomentar si se desea reordenar los números de línea después de eliminar
        // Reordenar números de línea para mantener secuencia 1, 2, 3... sin huecos
        /*
        int contador = 1;
        for (ItemTicket i : items) {
            i.setNumeroLinea(contador++);
        }
        */
    }
    
    public void calcularSubtotal() {
        this.subtotal = items.stream()
            .map(ItemTicket::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "idTicket='" + idTicket + '\'' +
                ", numeroTicket='" + numeroTicket + '\'' +
                ", idUsuario='" + idUsuario + '\'' +
                ", fechaHora=" + fechaHora +
                ", total=" + total +
                ", estadoTicket=" + estadoTicket +
                '}';
    }
}