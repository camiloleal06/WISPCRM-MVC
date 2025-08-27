package org.wispcrm.utils;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.modelo.FacturaDto;
import org.wispcrm.services.WhatsappMessageService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacturacionProgramada {

    private static final int DIAS_MORA_MINIMO = 4;
    private static final int DIAS_MORA_MAXIMO = 30;
    private final InterfaceFacturas facturaDao;
    private final WhatsappMessageService whatsappMessageService;

  //  @Scheduled(cron = "0 0 9 * * MON-SAT") // Ejecuta a las 8:00 AM de lunes a sábado
    @Scheduled(cron = "0 0 9 * * 2,6")
    public void scheduledPaymentNotification() {
        if (!debeEjecutarHoy()) {
            log.info("Hoy no corresponde enviar mensajes");
            return;
        }

        try {
            log.info("Iniciando envío de mensajes programados");
            List<FacturaDto> facturas = facturaDao.listadoFacturasPendientes();
            facturas.stream()
                    .filter(this::tieneMoreRelevante)
                    .forEach(this::enviarMensaje);
            log.info("Finalizado envío de mensajes programados");
        } catch (Exception e) {
            log.error("Error al procesar envío de mensajes programados", e);
        }
    }

    private boolean debeEjecutarHoy() {
        LocalDate hoy = LocalDate.now();
        DayOfWeek diaSemana = hoy.getDayOfWeek();
        if (diaSemana == DayOfWeek.SUNDAY) {
            return false;
        }
        return hoy.getDayOfMonth() % 2 == 0;
    }

    private boolean tieneMoreRelevante(FacturaDto factura) {
        if (factura == null) {
            log.warn("Se recibió una factura nula");
            return false;
        }
        return factura.getMora() > DIAS_MORA_MINIMO && factura.getMora() < DIAS_MORA_MAXIMO;
    }

    private void enviarMensaje(FacturaDto factura) {
        try {
            String mensaje = construirMensaje(factura);
            whatsappMessageService.sendSimpleMessageWasenderapi(
                    factura.getTelefonoCliente(),
                    mensaje
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Operación interrumpida", e);
        } catch (IOException e) {
            log.error("Error de IO al enviar mensaje para factura {}: {}",
                    factura.getIdFactura(), e.getMessage());
        }
    }


    private String construirMensaje(FacturaDto factura) {
        return String.format(
                "Estimado(a) %s:%n" +
                        "Su factura #%s tiene una mora de %d días.%n" +
                        "Por favor hacer el pago correspondiente. " +
                        "Si ya realizó el pago, comuníquese con nosotros o haga caso omiso.%n" +
                        "Gracias por su preferencia.",
                factura.getNombres(),
                factura.getIdFactura(),
                factura.getMora()
        );
    }
}
