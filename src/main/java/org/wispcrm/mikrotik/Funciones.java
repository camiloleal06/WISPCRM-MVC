package org.wispcrm.mikrotik;

import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.stereotype.Service;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.profiles.Profile;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class Funciones extends Conectar implements MikrotikGateway {

    public Funciones(ConfigMikrotik configMikrotik) {
        super(configMikrotik);
    }

    public String getHost() {
        return super.getConfigHost();
    }

    public boolean testConnection() {
        try {
            boolean connected = connect();
            if (connected) disconnect();
            return connected;
        } catch (Exception e) {
            log.warn("MikroTik no disponible: {}", e.getMessage());
            return false;
        }
    }

    private String pppoeProfile() { return super.getConfigPppoeProfile(); }

    public void addPPPoE(Profile profile, Cliente cliente) throws MikrotikApiException {
        if (!connect()) { log.error("No hay conexión con Mikrotik"); return; }
        try {
            List<Map<String, String>> result = con.execute(
                    "/ppp/secret/print where name=" + cliente.getPppoeUser());
            if (result == null || result.isEmpty()) {
                String comment = sanitize(cliente.getNombres() + "_" + cliente.getIpAddress() + "_App");
                con.execute("/ppp/secret/add name=" + cliente.getPppoeUser()
                        + " password=" + cliente.getPppoePass()
                        + " profile=" + pppoeProfile()
                        + " remote-address=" + cliente.getIpAddress()
                        + " comment=" + comment
                        + " service=pppoe");
                log.info("PPPoE creado: {}", cliente.getPppoeUser());
            } else {
                log.info("PPPoE ya existe: {}", cliente.getPppoeUser());
            }
        } catch (Exception e) {
            log.error("Error creando PPPoE: {}", cliente.getPppoeUser(), e);
            throw e;
        } finally {
            disconnect();
        }
    }

    public void updatePPPoE(Profile profile, Cliente cliente) throws MikrotikApiException {
        if (!connect()) { log.error("No hay conexión con Mikrotik"); return; }
        try {
            List<Map<String, String>> result = con.execute(
                    "/ppp/secret/print where name=" + cliente.getPppoeUser());
            String comment = sanitize(cliente.getNombres() + "_" + cliente.getIpAddress() + "_App");
            log.info("[MIKROTIK] updatePPPoE user={} profile={} ip={}",
                    cliente.getPppoeUser(), pppoeProfile(), cliente.getIpAddress());
            if (result == null || result.isEmpty()) {
                con.execute("/ppp/secret/add name=" + cliente.getPppoeUser()
                        + " password=" + cliente.getPppoePass()
                        + " profile=" + pppoeProfile()
                        + " remote-address=" + cliente.getIpAddress()
                        + " comment=" + comment
                        + " service=pppoe");
                log.info("PPPoE creado (desde edición): {}", cliente.getPppoeUser());
            } else {
                String secretId = result.get(0).get(".id");
                log.info("[MIKROTIK] Secret existente: id={} data={}", secretId, result.get(0));
                con.execute("/ppp/secret/set .id=" + secretId
                        + " password=" + cliente.getPppoePass()
                        + " profile=" + pppoeProfile()
                        + " remote-address=" + cliente.getIpAddress()
                        + " comment=" + comment);
                log.info("PPPoE actualizado: {}", cliente.getPppoeUser());
            }
        } catch (Exception e) {
            log.error("Error actualizando PPPoE: {}", cliente.getPppoeUser(), e);
            throw e;
        } finally {
            disconnect();
        }
    }

    public void suspender(Cliente cliente) throws MikrotikApiException {
        String ip = cliente.getIpAddress();
        if (ip == null || ip.isBlank()) {
            log.warn("Cliente {} sin IP, no se puede suspender", cliente.getNombres());
            return;
        }
        if (!connect()) { log.error("No hay conexión con Mikrotik para suspender a {}", cliente.getNombres()); return; }
        try {
            List<Map<String, String>> existing = con.execute(
                    "/ip/firewall/address-list/print where address=" + ip);
            boolean yaEstaEnMoroso = existing.stream().anyMatch(e -> "Moroso".equals(e.get("list")));
            if (!yaEstaEnMoroso) {
                String comment = sanitize(cliente.getNombres() + "_App");
                con.execute("/ip/firewall/address-list/add address=" + ip
                        + " list=Moroso"
                        + " comment=" + comment);
                log.info("Cliente suspendido en MikroTik: {} ({})", cliente.getNombres(), ip);
            } else {
                log.info("Cliente ya está en lista Morosos: {}", cliente.getNombres());
            }
        } catch (Exception e) {
            log.error("Error suspendiendo cliente en MikroTik: {}", cliente.getNombres(), e);
            throw e;
        } finally {
            disconnect();
        }
    }

    public void reactivateEnMikrotik(Cliente cliente) throws MikrotikApiException {
        String ip = cliente.getIpAddress();
        if (ip == null || ip.isBlank()) return;
        if (!connect()) { log.error("No hay conexión con Mikrotik para reactivar a {}", cliente.getNombres()); return; }
        try {
            List<Map<String, String>> existing = con.execute(
                    "/ip/firewall/address-list/print where address=" + ip + " and list=Moroso");
            if (!existing.isEmpty()) {
                con.execute("/ip/firewall/address-list/remove .id=" + existing.get(0).get(".id"));
                log.info("Cliente removido de Morosos: {} ({})", cliente.getNombres(), ip);
            }
        } catch (Exception e) {
            log.error("Error reactivando cliente en MikroTik: {}", cliente.getNombres(), e);
            throw e;
        } finally {
            disconnect();
        }
    }

    public List<Map<String, String>> listPPPoEProfiles() throws MikrotikApiException {
        if (!connect()) return List.of();
        try {
            return con.execute("/ppp/profile/print");
        } finally {
            disconnect();
        }
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
