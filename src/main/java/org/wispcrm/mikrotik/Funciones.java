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
public class Funciones extends Conectar {

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

    public void addPPPoE(Profile profile, Cliente cliente) throws MikrotikApiException {
        if (!connect()) {
            log.error("No hay conexión con Mikrotik");
            return;
        }
        try {
            List<Map<String, String>> result = con.execute(
                    "/ppp/secret/print where name=\"" + cliente.getPppoeUser() + "\"");

            if (result == null || result.isEmpty()) {
                con.execute("/ppp/secret/add name=" + cliente.getPppoeUser()
                        + " password=" + cliente.getPppoePass()
                        + " profile=" + profile.getName()
                        + " remote-address=" + cliente.getIpAddress()
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

    public void suspender(Cliente cliente) throws MikrotikApiException {
        String ip = cliente.getIpAddress();
        if (ip == null || ip.isBlank()) {
            log.warn("Cliente {} no tiene IP asignada, no se puede suspender en MikroTik", cliente.getNombres());
            return;
        }
        if (!connect()) {
            log.error("No hay conexión con Mikrotik para suspender a {}", cliente.getNombres());
            return;
        }
        try {
            if (con.execute("/ip/firewall/address-list/print where address=" + ip + " list=Moroso").isEmpty()) {
                String cmd = "/ip/firewall/address-list/add address=" + ip + " list=Moroso";
                String user = cliente.getPppoeUser();
                if (user != null && !user.isBlank()) cmd += " comment=\"" + user + "\"";
                con.execute(cmd);
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
        if (!connect()) {
            log.error("No hay conexión con Mikrotik para reactivar a {}", cliente.getNombres());
            return;
        }
        try {
            List<Map<String, String>> existing = con.execute(
                    "/ip/firewall/address-list/print where address=" + ip + " list=Moroso");
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
}
