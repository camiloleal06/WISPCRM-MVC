package org.wispcrm.mikrotik;

import javax.net.SocketFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;

/**
 *
 */
abstract class Conectar {

    protected void connect() throws MikrotikApiException {
        con = ApiConnection.connect(SocketFactory.getDefault(), ConfigMikrotik.HOST, ApiConnection.DEFAULT_PORT, 2000);
        con.login(ConfigMikrotik.USERNAME, ConfigMikrotik.PASSWORD);
    }

    protected void disconnect() throws ApiConnectionException {
        con.close();
    }

    protected ApiConnection con;

}