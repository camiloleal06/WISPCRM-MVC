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

    protected boolean connect() throws MikrotikApiException {
        log.info("Connecting to {} as {}", configMikrotik.getHost(), configMikrotik.getUsername());
        con = ApiConnection.connect(SocketFactory.getDefault(), configMikrotik.getHost(),
                ApiConnection.DEFAULT_PORT, 10000);
        con.login(configMikrotik.getUsername(), configMikrotik.getPassword());
        if (con.isConnected()) {
            log.info("Connected to Mikrotik");
        }
        else {
            log.info("Error Connecting to Mikrotik");
        }
        return con.isConnected();
    }

    protected void disconnect() throws ApiConnectionException {
        log.info("Closing connection");
        con.close();
    }

}