package org.wispcrm.restcontroller;

import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wispcrm.daos.ClienteDao;
import org.wispcrm.daos.InterfacePagos;
import org.wispcrm.interfaces.ClienteInterface;
import org.wispcrm.modelo.Cliente;
import org.wispcrm.modelo.EstadoCliente;
import org.wispcrm.modelo.Factura;
import org.wispcrm.modelo.InvoiceTemplate;
import org.wispcrm.modelo.Pago;
import org.wispcrm.modelo.PagoDTO;
import org.wispcrm.modelo.PaymentNotificationTemplate;
import org.wispcrm.services.*;
import org.wispcrm.utils.ConstantMensaje;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import static org.wispcrm.utils.Util.currentUserName;

@Controller
@SessionAttributes("factura")
@RequiredArgsConstructor
public class FacturaController {
    public static final String ERROR = "error";
    public static final String CLIENTE_NO_EXISTE_EN_LA_BASE_DE_DATOS = "El cliente no existe en la Base de datos";
    public static final String PAGADA_PDF = "_pagada.pdf";
    private static final int DIA_TREINTI_UNO = 31;
    private static final int DIA_PAGO_UNO = 1;
    private static final int DIA_PAGO_VEINTI_UNO = 21;
    private static final int DIA_PAGO_DIEZ = 10;
    public static final String INFO = "info";
    private static final int DIA_PAGO_ONCE = 11;
    private static final int DIA_PAGO_VEINTE = 20;
    private static final String SE_HA_GENERADO_UNA_NUEVA_FACTURA_DE_SU_SERVICIO_DE_INTERNET = " se ha generado una nueva factura de su servicio de Internet ";
    private static final String ESTIMADO_A = "Estimado(a) ";
    private final PagoService pagosDAO;
    private final InterfacePagos pagosD;
    private final ClienteServiceImpl clienteService;
    private final ClienteInterface clienteDao;
    private final ClienteDao clienteRepo;
    private final FacturaReportService reporte;
    private final FacturaServiceImpl facturaDao;
    private final EnviarSMS smsService;
    private final EmailService emailService;
    private final WhatsappMessageService whatsappMessageService;

    LocalDate fechaActualLocalDate = LocalDate.now();
    Calendar fechavencimiento = Calendar.getInstance();
    int diaactual = fechaActualLocalDate.getDayOfMonth();

    private static final String VER_FORMULARIO_FACTURA = "factura/formFactura";
    private static final String LISTAR_CLIENTE = "cliente/listaCliente";
    private static final String LISTAR_FACTURA = "factura/listaFactura";
    private static final String REDIRECT_LISTARFACTURA = "redirect:/listarfactura";
    private static final String LISTAR_PAGO = "factura/listaPago";
    private static final String REDIRECT_LISTAR = "redirect:/listar";

    private static final String WHATSAPP_GROUP_ID = "120363146011086828@g.us";
    private static final String PAYMENT_MESSAGE_TEMPLATE = "Hemos recibido el pago de : %s por valor de : %s pesos cobrado por : %s";


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(
            FacturaController.class);

    /**
     * @param flash
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/facturar")
    public String facturarEnLote(RedirectAttributes flash) {

        List<Cliente> listaClientes = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        int dayOfMonth = currentDate.getDayOfMonth();

        if (dayOfMonth <= DIA_PAGO_DIEZ) {

            listaClientes = clienteRepo.findByDiapagoBetween(DIA_PAGO_UNO,
                            DIA_PAGO_DIEZ).stream()
                    .filter(cliente -> cliente.getEstado() == EstadoCliente.ACTIVO)
                    .collect(Collectors.toList());

            createFacturasEntreDiaInicialDiaFinal(listaClientes);
        }

        if (dayOfMonth >= DIA_PAGO_ONCE && dayOfMonth <= DIA_PAGO_VEINTE) {

            listaClientes = clienteDao.findByDiaPagoBetween(DIA_PAGO_ONCE,
                            DIA_PAGO_VEINTE).stream()
                    .filter(cliente -> cliente.getEstado() == EstadoCliente.ACTIVO)
                    .collect(Collectors.toList());
           createFacturasEntreDiaInicialDiaFinal(listaClientes);
        }

        if (dayOfMonth >= DIA_PAGO_VEINTI_UNO) {

            listaClientes = clienteDao.findByDiaPagoBetween(DIA_PAGO_VEINTI_UNO,
                            DIA_TREINTI_UNO).stream()
                    .filter(cliente -> cliente.getEstado() == EstadoCliente.ACTIVO)
                    .collect(Collectors.toList());
           createFacturasEntreDiaInicialDiaFinal(listaClientes);
        }

        flash.addFlashAttribute(INFO,
                "Se han generado : " + listaClientes.size() + " Facturas con exito ");

        return REDIRECT_LISTARFACTURA;

    }

    @GetMapping("/notificar")
    public String notificar(RedirectAttributes flash) {

        clienteDao.findAll().stream()
                .filter(cliente -> cliente.getEstado() == EstadoCliente.ACTIVO)
                .collect(Collectors.toList()).forEach(c -> {
                    try {
                        whatsappMessageService.sendSimpleMessage("3225996394",
                                "Estimado cliente, tenemos un daño en nuestros servicios por parte de nuestros proveedores, " + "por lo cual habrá intermitencia o caida total. " + "Agradecemos su comprension");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        return REDIRECT_LISTARFACTURA;
    }

    /**
     * @param clienteID
     * @param modelo
     * @param flash
     * @return
     */
    @RequestMapping(value = "/factura")
    public String crear(@RequestParam(name = "clienteID") Integer clienteID,
            Model modelo, RedirectAttributes flash) {
        Cliente cliente = clienteService.findOne(clienteID);
        if (cliente == null) {
            flash.addFlashAttribute(ERROR,
                    CLIENTE_NO_EXISTE_EN_LA_BASE_DE_DATOS);
            return LISTAR_CLIENTE;
        }

        Factura factura = new Factura();
        factura.setCliente(cliente);
        modelo.addAttribute("listaplan", cliente.getPlanes());
        modelo.addAttribute("factura", factura);
        modelo.addAttribute("titulo", "Nueva Factura");
        return VER_FORMULARIO_FACTURA;
    }

    /**
     * @param modelo
     * @return
     */
    @RequestMapping(value = "/listarfactura")
    public String listarfactura(Model modelo) {
        modelo.addAttribute("listafactura", facturaDao.listadoFacturas());
        modelo.addAttribute("diaactual", diaactual);
        return LISTAR_FACTURA;
    }

    @GetMapping(value = "/listarfacturaid")
    public String verfac(@RequestParam(name = "id") Integer id, Model model) {
        model.addAttribute("facturasid", facturaDao.findFacturabyid(id));
        return LISTAR_FACTURA;
    }

    @Transactional
    @GetMapping("/pagar/{id}")
    public String pagar(@PathVariable("id") int id, SessionStatus status,
            RedirectAttributes flash) throws IOException, InterruptedException {
        Factura factura = facturaDao.findFacturabyid(id);
        if (factura == null) {
            flash.addFlashAttribute(ERROR, "Factura no encontrada");
            return REDIRECT_LISTARFACTURA;
        }

        Pago pago = Pago.builder()
                .pago(factura.getValor())
                .saldo(0)
                .factura(factura)
                .build();
        factura.setEstado(false);

        pagosDAO.save(pago);

        Factura facturaSaved = facturaDao.save(factura);
        String telefono = facturaSaved.getCliente().getTelefono();
        String nombres = facturaSaved.getCliente().getNombres() + " " + factura.getCliente().getApellidos();
        String facturaId = String.valueOf(facturaSaved.getId());

        PaymentNotificationTemplate.PaymentData data = new PaymentNotificationTemplate.PaymentData(
                nombres, String.valueOf(facturaSaved.getValor()),
                LocalDate.now(), facturaId,  "SysRed");

        String mensaje = PaymentNotificationTemplate.buildMessage(data, PaymentNotificationTemplate.Tone.FORMAL);

        whatsappMessageService.sendSimpleMessageWasenderapi(telefono, mensaje);

        whatsappMessageService.sendSimpleMessageToGroupWasApiSender(
                WHATSAPP_GROUP_ID,
                String.format(PAYMENT_MESSAGE_TEMPLATE, nombres,
                        factura.getValor(), currentUserName()));

       flash.addFlashAttribute(INFO, "Pago agregado correctamente");

        log.info("Pago agregado correctamente - ID: {}, Monto: {}", pago.getId(), pago.getPago());
        status.setComplete();
        return REDIRECT_LISTARFACTURA;
    }


    @GetMapping("/pagarmultiple")
    public String pagarMultiple(
            @RequestParam(name = "present", defaultValue = "0") List<String> values,
            RedirectAttributes flash) {
        List<String> listaFacturas = new ArrayList<>();
        values.forEach(item -> {
            Factura factura = facturaDao.findFacturabyid(Integer.valueOf(item));
            Pago pago = Pago.builder().pago(factura.getValor()).saldo(0)
                    .factura(factura).build();
            factura.setEstado(false);
            pagosDAO.save(pago);
            Factura facturaSaved = facturaDao.save(factura);
            listaFacturas.add(facturaSaved.getCliente().getNombres());
            try {
                sendWhatsAppMessagePagoRecibdo(facturaSaved);
            } catch (Exception e) {
                log.error(ERROR, new Exception());
            }
        });

        flash.addFlashAttribute(INFO,
                "se han agregado correctamente " + values.size() + " pagos " + listaFacturas);
        return REDIRECT_LISTARFACTURA;
    }

    @GetMapping("/recordar/{id}")
    public String recordar(@PathVariable("id") int id, SessionStatus status,
            Model modelo, RedirectAttributes flash) {
        Factura factura = facturaDao.findFacturabyid(id);
        String telefono = factura.getCliente().getTelefono();

        factura.setNotificacion(factura.getNotificacion() + 1);
        facturaDao.save(factura);
        this.sendWhatsAppMessageNotificacionDePago(factura);
        flash.addFlashAttribute(INFO,
                "El mensaje ha sido enviado a : " + telefono);
        status.setComplete();

        return REDIRECT_LISTARFACTURA;
    }

    @GetMapping("/avisocorte/{id}")
    public String avisocorte(@PathVariable("id") int id,
            RedirectAttributes flash) {
        Factura factura = facturaDao.findFacturabyid(id);
        factura.setEstado(true);
        facturaDao.save(factura);
        smsService.enviarSMS(factura.getCliente().getTelefono(),
                ESTIMADO_A + factura.getCliente()
                        .getNombres() + " Usted cuenta con dos facturas vencidas, " + "su servicio de internet será suspendido Att. SYSRED");
        flash.addFlashAttribute(INFO,
                "El mensaje ha sido enviado a : " + factura.getCliente()
                        .getTelefono());
        return REDIRECT_LISTARFACTURA;
    }

    @GetMapping("/eliminarfactura/{id}")
    public String eliminarfactura(@PathVariable("id") int id,
            RedirectAttributes flash) {
        Factura factura = facturaDao.findFacturabyid(id);
        if (factura != null) {
            facturaDao.delete(id);
            flash.addFlashAttribute("warning",
                    "Factura : " + factura.getId() + " Eliminada con exito");
        } else {
            flash.addFlashAttribute(ERROR, "No se ha podido eliminar");
        }

        return REDIRECT_LISTARFACTURA;
    }

    @PostMapping("/savefactura")
    public String crearFacturaAndSendSms(@Validated Factura factura,
            RedirectAttributes flash) {
        fechavencimiento.setTime(new Date());
        fechavencimiento.set(Calendar.DAY_OF_MONTH,
                factura.getCliente().getDiapago());
        factura.setFechapago(fechavencimiento.getTime());
        factura.setFechavencimiento(fechavencimiento.getTime());
        factura.setValor(factura.getCliente().getPlanes().getPrecio());
        factura.setNotificacion(ConstantMensaje.ZERO_INT);
        factura.setPeriodo(LocalDate.now().getMonthValue() + DIA_PAGO_UNO);
        clienteService.saveFactura(factura);
        sendWhatsAppMessageNuevaFacturaGenerada(factura);
        flash.addFlashAttribute(INFO,
                "Se ha generado una factura a " + factura.getCliente()
                        .getNombres() + " " + factura.getCliente()
                        .getApellidos() + " correctamente");
        return REDIRECT_LISTAR;
    }

    @Async("threadPoolTaskExecutor")
    public void createFacturasEntreDiaInicialDiaFinal(
            List<Cliente> listaClientes) {
        listaClientes.forEach(this::saveAndSendEmailAndSendSms);
    }

    private Factura saveAndSendEmailAndSendSms(Cliente cliente) {
        fechavencimiento = Calendar.getInstance();
        fechavencimiento.add(Calendar.MONTH, ConstantMensaje.ZERO_INT);
        fechavencimiento.set(Calendar.DAY_OF_MONTH, cliente.getDiapago());
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setFechapago(fechavencimiento.getTime());
        factura.setFechavencimiento(fechavencimiento.getTime());
        factura.setValor(cliente.getPlanes().getPrecio());
        factura.setNotificacion(ConstantMensaje.ZERO_INT);
        factura.setPeriodo(LocalDate.now().getMonthValue());
        Factura facturaSend = facturaDao.save(factura);
       // sendWhatsAppMessageNuevaFacturaGenerada(facturaSend);
        sendWhatsAppMessageNuevaFacturaGeneradaTemplate(facturaSend);
        return factura;
    }

    @GetMapping("/facturar/{id}")
    public String facturarUno(RedirectAttributes flash,
            @PathVariable("id") int id) {
        Cliente cliente = clienteDao.findById(id);
        Factura factura = new Factura();
        int diapago = cliente.getDiapago();
        save(factura, diapago, cliente, 0, 0);
        sendWhatsAppMessageNuevaFacturaGenerada(factura);
        flash.addFlashAttribute(INFO, "Se han generado la Factura");
        return REDIRECT_LISTAR;
    }

    private Factura save(Factura factura, int diapago, Cliente cliente,
            int periodo, int mes) {
        if (!facturaDao.existeFacturaPeriodo()) {
            fechavencimiento.add(Calendar.MONTH, mes);
            fechavencimiento.set(Calendar.DAY_OF_MONTH, diapago);
            factura.setCliente(cliente);
            factura.setFechapago(fechavencimiento.getTime());
            factura.setFechavencimiento(fechavencimiento.getTime());
            factura.setValor(cliente.getPlanes().getPrecio());
            factura.setNotificacion(ConstantMensaje.ZERO_INT);
            factura.setPeriodo(LocalDate.now().getMonthValue() + periodo);
            Factura facturaSend = facturaDao.save(factura);
            emailService.sendMail(facturaSend);
        }
        return factura;
    }

    /**
     * @param id
     * @param flash
     * @param response
     * @throws IOException
     * @throws JRException
     * @throws SQLException
     */
    @GetMapping("/descargarfactura/{id}")
    public void descargarfactura(@PathVariable(value = "id") Integer id,
            RedirectAttributes flash, HttpServletResponse response)
            throws IOException, JRException, SQLException {
        Factura factura = facturaDao.findFacturabyid(id);
        if (factura != null) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + factura.getId() + "_" + factura.getCliente()
                            .getNombres() + "_" + factura.getCliente()
                            .getApellidos() + ".pdf");
            OutputStream out = response.getOutputStream();
            JasperPrint jasperPrint = reporte.descargarPdfFile(factura.getId());
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
        }
    }

    /**
     * @param id
     * @param flash
     * @param response
     * @throws IOException
     * @throws JRException
     */
    @GetMapping("/descargarpago/{id}")
    public void descargarpago(@PathVariable(value = "id") Integer id,
            RedirectAttributes flash, HttpServletResponse response)
            throws IOException, JRException {
        Factura factura = facturaDao.findFacturabyid(id);
        if (factura != null) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + factura.getId() + "_" + factura.getCliente()
                            .getNombres() + ".pdf");
            OutputStream out = response.getOutputStream();
            JasperPrint jasperPrint = reporte.descargarPagoFile(
                    factura.getId());
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
        }
    }

    /**
     * @param modelo
     * @return
     */
    @RequestMapping(value = "/listarpago")
    public String listarpago(Model modelo) throws ParseException {

        LocalDate fechaActual = LocalDate.now();
        // Obtener el primer día del mes
        LocalDate primerDiaDelMes = fechaActual.withDayOfMonth(1);
        // Obtener el último día del mes
        LocalDate ultimoDiaDelMes = fechaActual.withDayOfMonth(
                fechaActual.lengthOfMonth());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String primerDiaFormateado = primerDiaDelMes.format(formatter);
        String ultimoDiaFormateado = ultimoDiaDelMes.format(formatter);

        List<PagoDTO> pago = pagosD.lista();
        Date startDate = getDateAsUtilDate(primerDiaFormateado);
        Date endDate = getDateAsUtilDate(ultimoDiaFormateado);

        modelo.addAttribute("listapagos",
                listaPagosByDates(pago, startDate, endDate).collect(
                        Collectors.toList()));

        modelo.addAttribute("totalpagos",
                listaPagosByDates(pago, startDate, endDate).mapToDouble(
                        PagoDTO::getPago).sum());

        modelo.addAttribute("startDate", startDate);
        modelo.addAttribute("endDate", endDate);

        return LISTAR_PAGO;
    }

    private Stream<PagoDTO> listaPagosByDates(List<PagoDTO> pago,
            Date startDate, Date endDate) {
        return pago.stream().filter(p -> !p.getFechapago()
                .before(startDate) && !p.getFechapago().after(endDate));
    }

    private void sendWhatsAppMessagePagoRecibdo(Factura factura) {
        try {

            int facturaId = factura.getId();
            String telefono = factura.getCliente().getTelefono();
            String fileName = factura.getId() + PAGADA_PDF;
            String ruta = ConstantMensaje.RUTA_DESCARGA_FACTURA_DOCS + fileName;
            String nombres = factura.getCliente().getNombres() + " " + factura.getCliente().getApellidos();
            String mensaje = ESTIMADO_A + nombres + ConstantMensaje.HEMOS_RECIBIDO_SU_PAGO + facturaId;
            reporte.pagoPdfReport(facturaId, fileName);
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            excuteSendMsgToWhatsApp(executorService, mensaje, telefono,
                    fileName, ruta);
        } catch (Exception e) {
            log.error(ERROR, new Exception());
        }
    }

    private void sendWhatsAppMessageNuevaFacturaGenerada(Factura factura) {
        try {
            int facturaId = factura.getId();
            String fileName = factura.getId() + ".pdf";
            String ruta = ConstantMensaje.RUTA_DESCARGA_FACTURA_DOCS + fileName;
            String telefono = factura.getCliente().getTelefono();
            String nombres = factura.getCliente()
                    .getNombres() + " " + factura.getCliente().getApellidos();
            String mensaje = ESTIMADO_A + nombres + SE_HA_GENERADO_UNA_NUEVA_FACTURA_DE_SU_SERVICIO_DE_INTERNET;

            reporte.createPdfReport(facturaId, fileName);
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            excuteSendMsgToWhatsApp(executorService, mensaje, telefono,
                    fileName, ruta);

        } catch (JRException e) {
            log.error(e.getMessage());
        }
    }

    private void sendWhatsAppMessageNuevaFacturaGeneradaTemplate(Factura factura) {

            String telefono = factura.getCliente().getTelefono();
            String nombres = factura.getCliente()
                    .getNombres() + " " + factura.getCliente().getApellidos();

            String mensaje = InvoiceTemplate.generarMensajeFactura(nombres,
                    LocalDate.now().getMonth().toString(), LocalDate.now().getYear(),
                    String.valueOf(factura.getId()), factura.getValor(),
                    factura.getFechavencimiento().toString(), "SYSRED");

          ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          excuteSendMsgToWhatsAppAsyn(executorService, mensaje, telefono);

    }




    private void excuteSendMsgToWhatsApp(
            ScheduledExecutorService executorService, String mensaje,
            String telefono, String fileName, String ruta) {
        executorService.schedule(() -> {
            try {
                log.info("Envio documento : {}", ruta);
                whatsappMessageService.sendDocumentAndMessageWasenderapi(telefono, mensaje, ruta,fileName);
            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 2, TimeUnit.SECONDS);
    }

    private void excuteSendMsgToWhatsAppAsyn(
            ScheduledExecutorService executorService, String mensaje,
            String telefono) {
        executorService.schedule(() -> {
            try {
                log.info("Envio factura");
                whatsappMessageService.sendSimpleMessageWasenderapi(telefono, mensaje);
            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 2, TimeUnit.SECONDS);
    }


    private void sendWhatsAppMessageNotificacionDePago(Factura facturas) {

        Factura factura = facturaDao.findFacturabyid(facturas.getId());
        String telefono = factura.getCliente().getTelefono();
        String nombre = factura.getCliente()
                .getNombres() + " " + factura.getCliente().getApellidos();
        try {
            whatsappMessageService.sendSimpleMessageWasenderapi(telefono,
                    "Estimado(a) : " + nombre + " No hemos recibido el pago de su factura # " + factura.getId() + " por valor de : " + factura.getValor());
         } catch (RuntimeException | IOException e) {
            log.error(ERROR, new RuntimeException());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Date getDateAsUtilDate(String fecha) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(fecha);
    }
}
