package org.wispcrm.mikrotik;

import javax.net.SocketFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
abstract class Conectar {

    private final ConfigMikrotik configMikrotik;
    protected ApiConnection con;

    protected String getConfigHost() {
        return configMikrotik.getHost();
    }

    protected boolean connect() throws MikrotikApiException {
        String host = configMikrotik.getHost();
        String user = configMikrotik.getUsername();
        log.info("[MIKROTIK] Conectando a {}:{} como {}", host, ApiConnection.DEFAULT_PORT, user);
        try {
            con = ApiConnection.connect(SocketFactory.getDefault(), host, ApiConnection.DEFAULT_PORT, 10000);
            con.login(user, configMikrotik.getPassword());
        } catch (MikrotikApiException e) {
            log.error("[MIKROTIK] Conexion fallida a {} | Error: {}", host, e.getMessage());
            throw e;
        }
        if (con.isConnected()) {
            log.info("[MIKROTIK] Conectado exitosamente a {}", host);
        } else {
            log.error("[MIKROTIK] No se pudo conectar a {}", host);
        }
        return con.isConnected();
    }

    protected void disconnect() throws ApiConnectionException {
        con.close();
        log.info("[MIKROTIK] Desconectado de {}", configMikrotik.getHost());
    }

}