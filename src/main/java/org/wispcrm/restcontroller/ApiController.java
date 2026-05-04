package org.wispcrm.restcontroller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.wispcrm.daos.ClienteDao;
import org.wispcrm.daos.InterfaceFacturas;
import org.wispcrm.daos.InterfacePagos;
import org.wispcrm.interfaces.PlanInterface;
import org.wispcrm.interfaces.ProfileInterface;
import org.wispcrm.mikrotik.Funciones;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.clientes.ClienteDTO;
import org.wispcrm.modelo.clientes.EditarClienteDTO;
import org.wispcrm.modelo.clientes.EstadoCliente;
import org.wispcrm.modelo.facturas.DeudorDTO;
import org.wispcrm.modelo.facturas.FacturaDto;
import org.wispcrm.modelo.facturas.ResumenFacturasDTO;
import org.wispcrm.modelo.pagos.PagoDTO;
import org.wispcrm.modelo.pagos.PagoMensualDTO;
import org.wispcrm.modelo.planes.Plan;
import org.wispcrm.modelo.profiles.Profile;
import org.wispcrm.services.ClienteServiceImpl;
import org.wispcrm.services.WhatsappMessageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

    private final ClienteDao clienteDao;
    private final ClienteServiceImpl clienteService;
    private final InterfaceFacturas facturaDao;
    private final InterfacePagos pagosDao;
    private final PlanInterface planService;
    private final ProfileInterface profileService;
    private final Funciones funciones;
    private final WhatsappMessageService whatsappService;

    @org.springframework.beans.factory.annotation.Value("${sysred.notification.suspensionNotify}")
    private boolean suspensionNotify;

    // ==================== HELPERS ====================

    private ResponseEntity<Map<String, String>> ok(String action, String message) {
        log.info("[{}] {}", action, message);
        return ResponseEntity.ok(Map.of("status", "ok", "message", message));
    }

    private ResponseEntity<Map<String, String>> badRequest(String action, String message) {
        log.warn("[{}] {}", action, message);
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", message));
    }

    private ResponseEntity<Map<String, String>> error(String action, String message, Exception e) {
        log.error("[{}] {} - {}", action, message, e.getMessage(), e);
        return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", message));
    }

    // ==================== AUTH ====================

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> currentUser(Authentication auth) {
        Map<String, String> user = new HashMap<>();
        user.put("username", auth != null ? auth.getName() : "anonymous");
        user.put("role", auth != null ? auth.getAuthorities().iterator().next().getAuthority() : "");
        return ResponseEntity.ok(user);
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        CompletableFuture<Long> clientesFuture = CompletableFuture.supplyAsync(clienteDao::totalClientesActivos);
        CompletableFuture<Long> inactivosFuture = CompletableFuture.supplyAsync(clienteDao::totalClientesInactivos);
        CompletableFuture<Long> suspendidosFuture = CompletableFuture.supplyAsync(clienteDao::totalClientesSuspendidos);
        CompletableFuture<Long> pendientesFuture = CompletableFuture.supplyAsync(facturaDao::totalFacturasPendientesMes);
        CompletableFuture<Long> cantidadFuture = CompletableFuture.supplyAsync(facturaDao::totalCantidadFacturasMes);
        CompletableFuture<Long> recaudadoFuture = CompletableFuture.supplyAsync(pagosDao::pagadas);
        CompletableFuture<List<PagoDTO>> ultimosPagosFuture = CompletableFuture.supplyAsync(
                () -> pagosDao.findLastTenPagos(PageRequest.of(0, 5)));
        CompletableFuture<List<FacturaDto>> morososFuture = CompletableFuture.supplyAsync(
                facturaDao::listadoFacturasPendientes);
        CompletableFuture<List<DeudorDTO>> deudoresFuture = CompletableFuture.supplyAsync(
                facturaDao::topDeudores);
        CompletableFuture<Long> clientesNuevosFuture = CompletableFuture.supplyAsync(clienteDao::clientesNuevosMes);
        CompletableFuture<Long> ingresoEsperadoFuture = CompletableFuture.supplyAsync(clienteDao::ingresoEsperado);
        CompletableFuture<Long> clientesConDeudaFuture = CompletableFuture.supplyAsync(facturaDao::clientesConDeuda);

        java.util.Calendar semana = java.util.Calendar.getInstance();
        semana.add(java.util.Calendar.DAY_OF_MONTH, 7);
        CompletableFuture<List<FacturaDto>> porVencerFuture = CompletableFuture.supplyAsync(
                () -> facturaDao.facturasPorVencer(semana.getTime()));

        CompletableFuture.allOf(clientesFuture, inactivosFuture, suspendidosFuture,
                pendientesFuture, cantidadFuture, recaudadoFuture,
                ultimosPagosFuture, morososFuture, deudoresFuture,
                clientesNuevosFuture, ingresoEsperadoFuture, clientesConDeudaFuture, porVencerFuture).join();

        List<FacturaDto> morosos = morososFuture.join().stream()
                .sorted((a, b) -> Integer.compare(b.getMora(), a.getMora()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("clientesActivos", clientesFuture.join());
        data.put("clientesInactivos", inactivosFuture.join());
        data.put("clientesSuspendidos", suspendidosFuture.join());
        data.put("pendientesMes", pendientesFuture.join());
        data.put("cantidadPendientesMes", cantidadFuture.join());
        data.put("recaudadoMes", recaudadoFuture.join());
        data.put("ultimosPagos", ultimosPagosFuture.join());
        data.put("topMorosos", morosos);
        data.put("topDeudores", deudoresFuture.join().stream().limit(10).collect(java.util.stream.Collectors.toList()));
        data.put("clientesNuevosMes", clientesNuevosFuture.join());
        data.put("ingresoEsperado", ingresoEsperadoFuture.join());
        data.put("clientesConDeuda", clientesConDeudaFuture.join());
        data.put("porVencer", porVencerFuture.join().stream().limit(10).collect(java.util.stream.Collectors.toList()));
        return ResponseEntity.ok(data);
    }

    // ==================== CLIENTES ====================

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> listarClientes() {
        return ResponseEntity.ok(clienteDao.lista());
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<EditarClienteDTO> getCliente(@PathVariable int id) {
        return ResponseEntity.ok(clienteService.clienteDTOById(id));
    }

    @PostMapping("/clientes")
    public ResponseEntity<Map<String, String>> saveCliente(@RequestBody EditarClienteDTO dto) {
        String action = dto.getId() == null || dto.getId() == 0 ? "CREAR_CLIENTE" : "EDITAR_CLIENTE";
        try {
            Integer id = dto.getId();
            boolean esNuevo = (id == null || id == 0);

            if (dto.getIdentificacion() != null) {
                clienteDao.findFirstClienteByIdentificacion(dto.getIdentificacion())
                        .filter(c -> esNuevo || c.getId() != id)
                        .ifPresent(c -> { throw new IllegalArgumentException("Ya existe un cliente con la identificación " + dto.getIdentificacion()); });
            }
            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                clienteDao.findFirstClienteByEmail(dto.getEmail())
                        .filter(c -> esNuevo || c.getId() != id)
                        .ifPresent(c -> { throw new IllegalArgumentException("Ya existe un cliente con el email " + dto.getEmail()); });
            }

            Cliente cliente = clienteService.toCliente(dto);
            clienteService.save(cliente);
            return ok(action, "Cliente " + dto.getNombres() + " guardado");
        } catch (IllegalArgumentException e) {
            return badRequest(action, e.getMessage());
        } catch (Exception e) {
            return error(action, "Error al guardar cliente", e);
        }
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Map<String, String>> eliminarCliente(@PathVariable int id) {
        try {
            Cliente cliente = clienteService.findById(id);
            cliente.setEstado(EstadoCliente.INACTIVO);
            clienteService.save(cliente);
            return ok("DESACTIVAR_CLIENTE", "Cliente " + cliente.getNombres() + " desactivado");
        } catch (Exception e) {
            return error("DESACTIVAR_CLIENTE", "Error al desactivar cliente " + id, e);
        }
    }

    @PutMapping("/clientes/{id}/reactivar")
    public ResponseEntity<Map<String, String>> reactivarCliente(@PathVariable int id) {
        try {
            Cliente cliente = clienteService.findById(id);
            String nombres = cliente.getNombres() + " " + cliente.getApellidos();
            cliente.setEstado(EstadoCliente.ACTIVO);
            clienteService.save(cliente);
            try { funciones.reactivarEnMikrotik(cliente); } catch (Exception e) {
                log.warn("[REACTIVAR_CLIENTE] MikroTik no disponible para {}: {}", nombres, e.getMessage());
            }
            if (suspensionNotify) {
                whatsappService.sendSimpleMessageWasenderapi(cliente.getTelefono(),
                        "\u2705 *Servicio Reactivado*\n\nEstimado(a) " + nombres
                        + ",\n\nSu servicio de internet ha sido *reactivado*.\n\nGracias por su pago.\n\nAtt. SYSRED");
                whatsappService.sendSimpleMessageToGroupWasApiSender(WHATSAPP_GROUP_ID,
                        "\u2705 *Servicio reactivado:* " + nombres + " | IP: " + cliente.getIpAddress());
            }
            return ok("REACTIVAR_CLIENTE", nombres + " reactivado" + (suspensionNotify ? " y notificado" : ""));
        } catch (Exception e) {
            return error("REACTIVAR_CLIENTE", "Error al reactivar cliente " + id, e);
        }
    }

    @PutMapping("/clientes/{id}/reactivar-forzado")
    public ResponseEntity<Map<String, String>> reactivarForzado(@PathVariable int id) {
        try {
            Cliente cliente = clienteService.findById(id);
            String nombres = cliente.getNombres() + " " + cliente.getApellidos();
            cliente.setEstado(EstadoCliente.ACTIVO);
            clienteService.save(cliente);
            try { funciones.reactivarEnMikrotik(cliente); } catch (Exception e) {
                log.warn("[REACTIVAR_FORZADO] MikroTik no disponible para {}: {}", nombres, e.getMessage());
            }
            return ok("REACTIVAR_FORZADO", nombres + " reactivado sin pago (forzado)");
        } catch (Exception e) {
            return error("REACTIVAR_FORZADO", "Error al reactivar cliente " + id, e);
        }
    }

    private static final String WHATSAPP_GROUP_ID = "120363146011086828@g.us";

    @PutMapping("/clientes/{id}/suspender")
    public ResponseEntity<Map<String, String>> suspenderCliente(@PathVariable int id) {
        try {
            Cliente cliente = clienteService.findById(id);
            String nombres = cliente.getNombres() + " " + cliente.getApellidos();

            if (cliente.getIpAddress() == null || cliente.getIpAddress().isBlank()) {
                return badRequest("SUSPENDER_CLIENTE", "Cliente " + nombres + " no tiene IP asignada, no se puede suspender");
            }

            List<FacturaDto> pendientes = facturaDao.facturasPendientesByCliente(id);

            if (pendientes.isEmpty()) {
                return badRequest("SUSPENDER_CLIENTE", "Cliente " + nombres + " no tiene facturas pendientes");
            }

            double total = pendientes.stream().mapToDouble(FacturaDto::getValorFactura).sum();
            String detalle = buildDetalleFacturas(pendientes);
            String deudaStr = "$" + String.format("%,.0f", total);

            // Intentar suspender en MikroTik
            boolean mikrotikOk = false;
            try {
                funciones.suspender(cliente);
                mikrotikOk = true;
                log.info("[SUSPENDER_CLIENTE] MikroTik: IP {} agregada a Morosos para {}", cliente.getIpAddress(), nombres);
            } catch (Exception e) {
                log.error("[SUSPENDER_CLIENTE] MikroTik NO disponible | Host: {} | Cliente: {} | IP: {} | Error: {}",
                        funciones.getHost(), nombres, cliente.getIpAddress(), e.getMessage());
            }

            if (mikrotikOk) {
                // Suspender en BD + notificar suspensión
                cliente.setEstado(EstadoCliente.SUSPENDIDO);
                clienteService.save(cliente);

                String msgCliente = "\u26A0\uFE0F *AVISO DE SUSPENSI\u00d3N*\n\n"
                        + "Estimado(a) " + nombres + ",\n\n"
                        + "Su servicio de internet ha sido *suspendido* por las siguientes facturas pendientes:\n\n"
                        + detalle + "\n\uD83D\uDCB0 *Total adeudado: " + deudaStr + "*\n\n"
                        + "Para reactivar su servicio, realice el pago y comun\u00edquese con nosotros."
                        + org.wispcrm.utils.ConstantMensaje.MEDIOS_DE_PAGO + "\nAtt. SYSRED";
                if (suspensionNotify) {
                    whatsappService.sendSimpleMessageWasenderapi(cliente.getTelefono(), msgCliente);
                    whatsappService.sendSimpleMessageToGroupWasApiSender(WHATSAPP_GROUP_ID,
                            "\uD83D\uDEAB *Servicio suspendido:* " + nombres + " | Deuda: " + deudaStr + " | IP: " + cliente.getIpAddress());
                } else {
                    log.info("[SUSPENDER_CLIENTE] Notificacion desactivada, no se envio WhatsApp a {}", nombres);
                }

                return ok("SUSPENDER_CLIENTE", nombres + " suspendido en MikroTik" + (suspensionNotify ? " y notificado" : " (sin notificar)") + " (deuda: " + deudaStr + ")");
            } else {
                // MikroTik no disponible: solo enviar aviso de cobro, NO suspender
                String msgAviso = "\u26A0\uFE0F *AVISO DE COBRO*\n\n"
                        + "Estimado(a) " + nombres + ",\n\n"
                        + "Le recordamos que tiene las siguientes facturas pendientes:\n\n"
                        + detalle + "\n\uD83D\uDCB0 *Total adeudado: " + deudaStr + "*\n\n"
                        + "Si no realiza el pago, su servicio ser\u00e1 *suspendido*."
                        + org.wispcrm.utils.ConstantMensaje.MEDIOS_DE_PAGO + "\nAtt. SYSRED";
                if (suspensionNotify) {
                    whatsappService.sendSimpleMessageWasenderapi(cliente.getTelefono(), msgAviso);
                    whatsappService.sendSimpleMessageToGroupWasApiSender(WHATSAPP_GROUP_ID,
                            "\u26A0\uFE0F Aviso de cobro enviado a: " + nombres + " | Deuda: " + deudaStr + " | MikroTik NO disponible");
                } else {
                    log.info("[SUSPENDER_CLIENTE] Notificacion desactivada, no se envio aviso de cobro a {}", nombres);
                }

                return badRequest("SUSPENDER_CLIENTE",
                        "MikroTik no disponible." + (suspensionNotify ? " Se envio aviso de cobro a " + nombres : "") + " NO se suspendio el servicio");
            }
        } catch (Exception e) {
            return error("SUSPENDER_CLIENTE", "Error al suspender cliente " + id, e);
        }
    }

    private String buildDetalleFacturas(List<FacturaDto> facturas) {
        StringBuilder sb = new StringBuilder();
        for (FacturaDto f : facturas) {
            sb.append("  \u2022 Factura #").append(f.getIdFactura())
              .append(" - $").append(String.format("%,.0f", f.getValorFactura()));
            if (f.getMora() > 0) sb.append(" (").append(f.getMora()).append(" d\u00edas de mora)");
            sb.append("\n");
        }
        return sb.toString();
    }

    // ==================== FACTURAS ====================

    @PostMapping("/facturas/lote")
    public ResponseEntity<Map<String, Object>> facturarEnLote() {
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            int dia = now.getDayOfMonth();
            int diaInicio, diaFin;

            if (dia <= 10) { diaInicio = 1; diaFin = 10; }
            else if (dia <= 20) { diaInicio = 11; diaFin = 20; }
            else { diaInicio = 21; diaFin = 31; }

            List<Cliente> clientes = clienteDao.findByDiapagoBetween(diaInicio, diaFin).stream()
                    .filter(c -> c.getEstado() == EstadoCliente.ACTIVO)
                    .collect(java.util.stream.Collectors.toList());

            int generadas = 0, omitidas = 0;
            for (Cliente cliente : clientes) {
                if (facturaDao.countFacturaClienteMesActual(cliente.getId()) > 0) {
                    omitidas++;
                    continue;
                }
                java.util.Calendar venc = java.util.Calendar.getInstance();
                venc.set(java.util.Calendar.DAY_OF_MONTH, cliente.getDiapago());
                org.wispcrm.modelo.facturas.Factura factura = new org.wispcrm.modelo.facturas.Factura();
                factura.setCliente(cliente);
                factura.setFechapago(venc.getTime());
                factura.setFechavencimiento(venc.getTime());
                factura.setValor(cliente.getPlanes().getPrecio());
                factura.setNotificacion(0);
                factura.setPeriodo(now.getMonthValue());
                facturaDao.save(factura);
                generadas++;
            }

            log.info("[FACTURAR_LOTE] Rango dias {}-{} | Clientes: {} | Generadas: {} | Omitidas: {}",
                    diaInicio, diaFin, clientes.size(), generadas, omitidas);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "ok");
            result.put("message", "Facturación completada: " + generadas + " generadas, " + omitidas + " omitidas (ya facturadas)");
            result.put("generadas", generadas);
            result.put("omitidas", omitidas);
            result.put("total", clientes.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Error en facturación: " + e.getMessage()));
        }
    }

    @GetMapping("/facturas")
    public ResponseEntity<List<FacturaDto>> listarFacturas() {
        return ResponseEntity.ok(facturaDao.listadoFacturasPendientes());
    }

    @GetMapping("/facturas/resumen")
    public ResponseEntity<ResumenFacturasDTO> resumenFacturas() {
        return ResponseEntity.ok(facturaDao.getResumenFacturasMes());
    }

    @org.springframework.transaction.annotation.Transactional
    @DeleteMapping("/facturas/{id}")
    public ResponseEntity<Map<String, String>> eliminarFactura(@PathVariable int id) {
        try {
            org.wispcrm.modelo.facturas.Factura factura = facturaDao.findById(id).orElse(null);
            if (factura == null) return badRequest("ELIMINAR_FACTURA", "Factura " + id + " no encontrada");
            facturaDao.delete(factura);
            return ok("ELIMINAR_FACTURA", "Factura #" + id + " eliminada");
        } catch (Exception e) {
            return error("ELIMINAR_FACTURA", "Error al eliminar factura " + id, e);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    @PutMapping("/facturas/{id}/pagar")
    public ResponseEntity<Map<String, String>> pagarFactura(@PathVariable int id) {
        try {
            org.wispcrm.modelo.facturas.Factura factura = facturaDao.findById(id).orElse(null);
            if (factura == null) return badRequest("PAGAR_FACTURA", "Factura " + id + " no encontrada");
            factura.setEstado(false);
            facturaDao.save(factura);
            org.wispcrm.modelo.pagos.Pago pago = org.wispcrm.modelo.pagos.Pago.builder()
                    .pago(factura.getValor()).saldo(0).factura(factura).build();
            pagosDao.save(pago);

            String nombres = factura.getCliente().getNombres() + " " + factura.getCliente().getApellidos();

            // Notificar al cliente
            if (suspensionNotify) {
                List<FacturaDto> pendientes = facturaDao.facturasPendientesByCliente(factura.getCliente().getId());
                StringBuilder msg = new StringBuilder();
                msg.append("\u2705 *Pago Recibido*\n\n");
                msg.append("Estimado(a) ").append(nombres).append(",\n\n");
                msg.append("Hemos recibido el pago de su factura #").append(id)
                   .append(" por valor de $").append(String.format("%,.0f", factura.getValor())).append(".\n");

                if (!pendientes.isEmpty()) {
                    double totalDeuda = pendientes.stream().mapToDouble(FacturaDto::getValorFactura).sum();
                    msg.append("\n\u26A0\uFE0F *A\u00fan tiene facturas pendientes:*\n\n");
                    for (FacturaDto f : pendientes) {
                        msg.append("  \u2022 Factura #").append(f.getIdFactura())
                           .append(" - $").append(String.format("%,.0f", f.getValorFactura()));
                        if (f.getMora() > 0) msg.append(" (").append(f.getMora()).append(" d\u00edas)");
                        msg.append("\n");
                    }
                    msg.append("\n\uD83D\uDCB0 *Total pendiente: $").append(String.format("%,.0f", totalDeuda)).append("*\n");
                } else {
                    msg.append("\n\uD83C\uDF89 *No tiene facturas pendientes.* \u00a1Gracias!");
                }
                msg.append(org.wispcrm.utils.ConstantMensaje.MEDIOS_DE_PAGO);
                msg.append("\nAtt. SYSRED");
                whatsappService.sendSimpleMessageWasenderapi(factura.getCliente().getTelefono(), msg.toString());

                whatsappService.sendSimpleMessageToGroupWasApiSender(WHATSAPP_GROUP_ID,
                        "\u2705 Pago recibido: " + nombres + " | Factura #" + id
                        + " | $" + String.format("%,.0f", factura.getValor())
                        + (pendientes.isEmpty() ? " | Sin deuda" : " | Pendientes: " + pendientes.size()));
            }

            return ok("PAGAR_FACTURA", "Pago registrado para factura #" + id + " de " + nombres);
        } catch (Exception e) {
            return error("PAGAR_FACTURA", "Error al pagar factura " + id, e);
        }
    }

    @PutMapping("/facturas/{id}/recordar")
    public ResponseEntity<Map<String, String>> recordarFactura(@PathVariable int id) {
        try {
            org.wispcrm.modelo.facturas.Factura factura = facturaDao.findById(id).orElse(null);
            if (factura == null) return badRequest("RECORDAR_FACTURA", "Factura " + id + " no encontrada");
            factura.setNotificacion(factura.getNotificacion() + 1);
            facturaDao.save(factura);
            String nombres = factura.getCliente().getNombres() + " " + factura.getCliente().getApellidos();
            whatsappService.sendSimpleMessageWasenderapi(factura.getCliente().getTelefono(),
                    "Estimado(a) " + nombres + ", no hemos recibido el pago de su factura #" + id + " por valor de $" + String.format("%,.0f", factura.getValor()) + "." + org.wispcrm.utils.ConstantMensaje.MEDIOS_DE_PAGO + "Att. SYSRED");
            return ok("RECORDAR_FACTURA", "Recordatorio enviado a " + nombres);
        } catch (Exception e) {
            return error("RECORDAR_FACTURA", "Error al enviar recordatorio factura " + id, e);
        }
    }

    // ==================== PAGOS ====================

    @org.springframework.transaction.annotation.Transactional
    @PutMapping("/facturas/batch/pagar")
    public ResponseEntity<Map<String, Object>> pagarBatch(@RequestBody List<Integer> ids) {
        int ok = 0, fail = 0;
        for (int id : ids) {
            try {
                org.wispcrm.modelo.facturas.Factura f = facturaDao.findById(id).orElse(null);
                if (f == null || !f.isEstado()) { fail++; continue; }
                f.setEstado(false);
                facturaDao.save(f);
                pagosDao.save(org.wispcrm.modelo.pagos.Pago.builder().pago(f.getValor()).saldo(0).factura(f).build());
                ok++;
            } catch (Exception e) { fail++; log.error("[PAGAR_BATCH] Error factura {}: {}", id, e.getMessage()); }
        }
        log.info("[PAGAR_BATCH] Pagadas: {} | Fallidas: {}", ok, fail);
        return ResponseEntity.ok(Map.of("status", "ok", "message", ok + " pagos registrados" + (fail > 0 ? ", " + fail + " fallidos" : "")));
    }

    @org.springframework.transaction.annotation.Transactional
    @PutMapping("/facturas/batch/eliminar")
    public ResponseEntity<Map<String, Object>> eliminarBatch(@RequestBody List<Integer> ids) {
        int ok = 0, fail = 0;
        for (int id : ids) {
            try {
                org.wispcrm.modelo.facturas.Factura f = facturaDao.findById(id).orElse(null);
                if (f == null) { fail++; continue; }
                facturaDao.delete(f);
                ok++;
            } catch (Exception e) { fail++; log.error("[ELIMINAR_BATCH] Error factura {}: {}", id, e.getMessage()); }
        }
        log.info("[ELIMINAR_BATCH] Eliminadas: {} | Fallidas: {}", ok, fail);
        return ResponseEntity.ok(Map.of("status", "ok", "message", ok + " facturas eliminadas" + (fail > 0 ? ", " + fail + " fallidas" : "")));
    }

    @PutMapping("/facturas/batch/recordar")
    public ResponseEntity<Map<String, Object>> recordarBatch(@RequestBody List<Integer> ids) {
        int ok = 0, fail = 0;
        for (int id : ids) {
            try {
                org.wispcrm.modelo.facturas.Factura f = facturaDao.findById(id).orElse(null);
                if (f == null) { fail++; continue; }
                f.setNotificacion(f.getNotificacion() + 1);
                facturaDao.save(f);
                String nombres = f.getCliente().getNombres() + " " + f.getCliente().getApellidos();
                whatsappService.sendSimpleMessageWasenderapi(f.getCliente().getTelefono(),
                        "Estimado(a) " + nombres + ", no hemos recibido el pago de su factura #" + id + " por valor de $" + String.format("%,.0f", f.getValor()) + "." + org.wispcrm.utils.ConstantMensaje.MEDIOS_DE_PAGO + "Att. SYSRED");
                ok++;
            } catch (Exception e) { fail++; log.error("[RECORDAR_BATCH] Error factura {}: {}", id, e.getMessage()); }
        }
        log.info("[RECORDAR_BATCH] Recordatorios: {} | Fallidos: {}", ok, fail);
        return ResponseEntity.ok(Map.of("status", "ok", "message", ok + " recordatorios enviados" + (fail > 0 ? ", " + fail + " fallidos" : "")));
    }

    @PutMapping("/clientes/batch/suspender")
    public ResponseEntity<Map<String, Object>> suspenderBatch(@RequestBody List<Integer> clienteIds) {
        int ok = 0, fail = 0;
        for (int cid : clienteIds) {
            try {
                ResponseEntity<Map<String, String>> res = suspenderCliente(cid);
                if (res.getStatusCode().is2xxSuccessful()) ok++; else fail++;
            } catch (Exception e) { fail++; log.error("[SUSPENDER_BATCH] Error cliente {}: {}", cid, e.getMessage()); }
        }
        log.info("[SUSPENDER_BATCH] Suspendidos: {} | Fallidos: {}", ok, fail);
        return ResponseEntity.ok(Map.of("status", "ok", "message", ok + " clientes suspendidos" + (fail > 0 ? ", " + fail + " fallidos" : "")));
    }

    @GetMapping("/pagos")
    public ResponseEntity<List<PagoDTO>> listarPagos() {
        return ResponseEntity.ok(pagosDao.listaPagosMesActual());
    }

    @GetMapping("/pagos/recientes")
    public ResponseEntity<List<PagoDTO>> pagosRecientes() {
        return ResponseEntity.ok(pagosDao.findLastTenPagos(PageRequest.of(0, 10)));
    }

    @GetMapping("/pagos/mensual")
    public ResponseEntity<List<PagoMensualDTO>> pagosMensuales() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, -11);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        return ResponseEntity.ok(pagosDao.pagosPorMes(cal.getTime()));
    }

    // ==================== PLANES & PROFILES ====================

    @GetMapping("/planes")
    public ResponseEntity<List<Plan>> listarPlanes() {
        return ResponseEntity.ok(planService.findAll());
    }

    @PostMapping("/planes")
    public ResponseEntity<Map<String, String>> savePlan(@RequestBody Plan plan) {
        try {
            planService.save(plan);
            return ok("GUARDAR_PLAN", "Plan " + plan.getNombre() + " guardado");
        } catch (Exception e) {
            return error("GUARDAR_PLAN", "Error al guardar plan", e);
        }
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<Profile>> listarProfiles() {
        return ResponseEntity.ok(profileService.findAll());
    }
}
