package org.wispcrm.modelo;

import lombok.Builder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Builder
public class PaymentNotificationTemplate {

    public enum Tone { FORMAL, AMIGABLE }

    public static class PaymentData {
        private final String nombreCliente;
        private final String monto;
        private final LocalDate fechaPago;
        private final String referenciaPago;
        private final String nombreEmpresa;

        public PaymentData(String nombreCliente, String monto, LocalDate fechaPago,
                String referenciaPago, String nombreEmpresa) {
            this.nombreCliente = Objects.requireNonNull(nombreCliente);
            this.monto = Objects.requireNonNull(monto);
            this.fechaPago = Objects.requireNonNull(fechaPago);
            this.referenciaPago = Objects.requireNonNull(referenciaPago);
            this.nombreEmpresa = Objects.requireNonNull(nombreEmpresa);
        }

        public String getNombreCliente() { return nombreCliente; }
        public String getMonto() { return monto; }
        public LocalDate getFechaPago() { return fechaPago; }
        public String getReferenciaPago() { return referenciaPago; }
        public String getNombreEmpresa() { return nombreEmpresa; }
    }

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String buildMessage(PaymentData data, Tone tone) {
        String fecha = data.getFechaPago().format(FECHA_FMT);
        if (tone == Tone.FORMAL) {
            return """
                   👋 Hola *%s*,

                   ✅ Hemos recibido tu pago correctamente.
                   💳 Monto: *$%s*
                   📅 Fecha: *%s*
                   📄 Factura: *%s*

                   Gracias por confiar en nosotros.
                   Si tienes alguna duda, contáctanos o responde este mensaje.

                   Atentamente,
                   %s
                   """.formatted(
                    data.getNombreCliente(),
                    data.getMonto(),
                    fecha,
                    data.getReferenciaPago(),
                    data.getNombreEmpresa()
            ).trim();
        } else {
            // AMIGABLE
            return """
                   🙌 ¡Gracias por tu pago, *%s*!

                   💵 Monto: *$%s*
                   📅 Fecha: *%s*
                   🔑 Factura: *%s*

                   Tu transacción fue exitosa ✅.
                   Cualquier consulta, escríbenos por este medio.

                   ✨ %s
                   """.formatted(
                    data.getNombreCliente(),
                    data.getMonto(),
                    fecha,
                    data.getReferenciaPago(),
                    data.getNombreEmpresa()
            ).trim();
        }
    }
}
