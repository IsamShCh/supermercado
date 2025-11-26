-- Crea la secuencia solo si no existe para evitar errores al reiniciar
CREATE SEQUENCE IF NOT EXISTS ticket_fiscal_seq 
    START WITH 1 
    INCREMENT BY 1;