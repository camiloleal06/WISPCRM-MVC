package org.wispcrm.modelo;

public class InvoiceTemplate {

      public static String generarMensajeFactura(String nombreCliente, String mes, int anio,
                String numeroFactura, double valor,
                String fechaVencimiento, String empresa) {
            return String.format(
                    "👋 Hola %s,\n\n" +
                            "Te compartimos la información de tu factura correspondiente a %s %d.\n\n" +
                            "🧾 Factura N.° %s\n\n" +
                            "💰 Valor a pagar: $%,.2f\n\n" +
                            "📅 Fecha de vencimiento: %s\n\n" +
                            "🏢 Empresa: %s\n\n",
                    nombreCliente, mes, anio,
                    numeroFactura,
                    valor,
                    fechaVencimiento,
                    empresa
            );
        }
   }

