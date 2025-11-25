package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PAGOS")
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pago", nullable = false, length = 36)
    private String idPago;
    
    @NotNull(message = "El ticket es obligatorio")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;
    
    @NotNull(message = "El método de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;
    
    @NotNull(message = "El monto recibido es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El monto recibido no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El monto recibido debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "monto_recibido", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoRecibido;
    
    @NotNull(message = "El monto de cambio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El monto de cambio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El monto de cambio debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "monto_cambio", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoCambio;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    public void calcularCambio(BigDecimal totalTicket) {
        if (montoRecibido != null && totalTicket != null) {
            this.montoCambio = montoRecibido.subtract(totalTicket);
            // Si el cambio es negativo, significa que falta dinero
            if (this.montoCambio.compareTo(BigDecimal.ZERO) < 0) {
                this.montoCambio = BigDecimal.ZERO;
            }
        }
    }
    
    @Override
    public String toString() {
        return "Pago{" +
                "idPago='" + idPago + '\'' +
                ", metodoPago=" + metodoPago +
                ", montoRecibido=" + montoRecibido +
                ", montoCambio=" + montoCambio +
                ", fechaHora=" + fechaHora +
                '}';
    }
}