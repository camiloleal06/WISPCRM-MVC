package org.wispcrm.mikrotik;

import me.legrange.mikrotik.MikrotikApiException;

public class Funciones extends Conectar {

    public Funciones() {
        super();
    }

    public void addlistsuspendidos(String ip, String comentario) throws MikrotikApiException {
        connect();
        con.execute("/ip/firewall/address-list/add address=" + ip + " list=Morosos comment=" + comentario);
        disconnect();
    }
}