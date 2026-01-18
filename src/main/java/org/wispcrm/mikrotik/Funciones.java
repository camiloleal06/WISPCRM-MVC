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

    public void addlistsuspendidos(String ip, String comentario) throws MikrotikApiException {
        connect();
        con.execute("/ip/firewall/address-list/add address=" + ip + " list=Morosos comment=" + comentario);
        disconnect();
    }

    public void addPPPoE(Profile profile, Cliente cliente) throws MikrotikApiException {
        connect();
        List<Map<String, String>> result =
                con.execute("/ppp/secret/print where name=\"" + cliente.getPppoeUser() + "\"");

        boolean existe = result != null && !result.isEmpty();

        if (!existe){
            con.execute("/ppp/secret/add name=" + cliente.getPppoeUser()
                    + " password=" + cliente.getPppoePass()
                    + " profile=" + profile.getName()
                    + " remote-address=" + cliente.getIpAddress()
                    + " service=pppoe");
       log.info("Se ha agregado un nuevo cliente PPPoE: {}", cliente.getPppoeUser());
        }
      disconnect();
    }
}