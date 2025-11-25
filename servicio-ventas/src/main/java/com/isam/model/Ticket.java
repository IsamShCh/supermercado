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
    @Column(name = "id_ticket", nullable = false, length = 36)
    private String idTicket;
    
    @NotBlank(message = "El número de ticket es obligatorio")
    @Size(max = 50, message = "El número de ticket debe tener máximo 50 caracteres")
    @Column(name = "numero_ticket", unique = true, length = 50)
    private String numeroTicket;
    
    @NotBlank(message = "El ID de usuario es obligatorio")
    @Size(max = 36, message = "El ID de usuario debe tener máximo 36 caracteres")
    @Column(name = "id_usuario", nullable = false, length = 36)
    private String idUsuario;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El subtotal debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El total de impuestos no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El total de impuestos debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "total_impuestos", precision = 10, scale = 2)
    private BigDecimal totalImpuestos;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "El total no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El total debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(name = "id_pago", length = 36)
    private String idPago;
    
    @NotNull(message = "El estado del ticket es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ticket", nullable = false, length = 20)
    private EstadoTicket estadoTicket;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemTicket> items = new ArrayList<>();
    
    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Pago pago;
    
    
    public void addItem(ItemTicket item) {
        items.add(item);
        item.setTicket(this);
    }
    
    public void calcularSubtotal() {
        this.subtotal = items.stream()
            .map(ItemTicket::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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