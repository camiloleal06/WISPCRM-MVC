package org.wispcrm.mikrotik;

import javax.net.SocketFactory;

import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;

/**
 *
 */
@Slf4j
abstract class Conectar {

    protected void connect() throws MikrotikApiException {
        log.debug("Connecting to {} as {}", ConfigMikrotik.HOST, ConfigMikrotik.USERNAME);
        con = ApiConnection.connect(SocketFactory.getDefault(), ConfigMikrotik.HOST,
                ApiConnection.DEFAULT_PORT, 10000);
        con.login(ConfigMikrotik.USERNAME, ConfigMikrotik.PASSWORD);
        log.debug("Connected to Mikrotik");
    }

    protected void disconnect() throws ApiConnectionException {
        log.debug("Closing connection");
        con.close();
    }

    protected ApiConnection con;

}