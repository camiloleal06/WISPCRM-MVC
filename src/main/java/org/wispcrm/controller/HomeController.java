package org.wispcrm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.wispcrm.daos.ClienteDao;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.daos.InterfacePagos;
import org.wispcrm.modelo.pagos.PagoDTO;
import org.wispcrm.utils.Util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.wispcrm.utils.Util.currentUserName;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {
    private final InterfaceFacturas facturaDao;
    private final ClienteDao clienteDao;
    private final InterfacePagos pagosDao;

    @GetMapping("/")
    public String home(Model modelo) {

        CompletableFuture<Long> clientesActivosFuture = CompletableFuture.supplyAsync(
                clienteDao::totalClientesActivos);

        CompletableFuture<Long> pendientesMesFuture = CompletableFuture.supplyAsync(
                facturaDao::totalFacturasPendientesMes);

        CompletableFuture<Long> cantidadPendientesMesFuture = CompletableFuture.supplyAsync(
                facturaDao::totalCantidadFacturasMes);

        CompletableFuture<Long> pagadasMesFuture = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return listaPagosByDates();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });

        CompletableFuture<Long> pendienteHistoricoFuture = CompletableFuture.supplyAsync(
                facturaDao::totalFacturasPendientesHistorico);

        CompletableFuture<Long> cantidadPendientesHistoricoFuture = CompletableFuture.supplyAsync(
                facturaDao::totalCantidadFacturasHistorico);

        CompletableFuture<Long> pagadasHistoricoFuture = CompletableFuture.supplyAsync(
                facturaDao::totalFacturasPagadasHistorico);

        try {

            modelo.addAttribute("numeroclientes", clientesActivosFuture.get());
            modelo.addAttribute("totalpendientemes", Util.formatearMoneda(pendientesMesFuture.get()));
            modelo.addAttribute("cantidadfacturaspendientesmes", cantidadPendientesMesFuture.get());
            modelo.addAttribute("totalpagadasmes", Util.formatearMoneda(pagadasMesFuture.get()));
            modelo.addAttribute("totalpendientehistorico", Util.formatearMoneda(pendienteHistoricoFuture.get()));
            modelo.addAttribute("cantidadfacturaspendienteshistorico", cantidadPendientesHistoricoFuture.get());
            modelo.addAttribute("totalpagadashistorico", Util.formatearMoneda(pagadasHistoricoFuture.get()));

            log.info("Usuario logueado: {}", currentUserName());

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al obtener datos para la página principal: ", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error al cargar los datos", e);
        }

        if (currentUserName().equals("cachi"))
            return "sendMessage";
        else
        return "home";
    }

    private Long listaPagosByDates() throws ParseException {
        LocalDate fechaActual = LocalDate.now();
        LocalDate primerDiaDelMes = fechaActual.withDayOfMonth(1);
        LocalDate ultimoDiaDelMes = fechaActual.withDayOfMonth(
                fechaActual.lengthOfMonth());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String primerDiaFormateado = primerDiaDelMes.format(formatter);
        String ultimoDiaFormateado = ultimoDiaDelMes.format(formatter);
        Date startDate = getDateAsUtilDate(primerDiaFormateado);
        Date endDate = getDateAsUtilDate(ultimoDiaFormateado);
        List<PagoDTO> pago = pagosDao.lista();

        return pago.stream()
                .filter(p -> !p.getFechapago().before(startDate)
                        && !p.getFechapago().after(endDate))
                .mapToLong(p -> (long) p.getPago())
                .sum();
    }
    public Date getDateAsUtilDate(String fecha) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(fecha);
    }
}
