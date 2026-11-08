package org.wispcrm.mikrotik;

import me.legrange.mikrotik.MikrotikApiException;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.profiles.Profile;

public interface MikrotikGateway {
    boolean testConnection();
    void addPPPoE(Profile profile, Cliente cliente) throws MikrotikApiException;
    void updatePPPoE(Profile profile, Cliente cliente) throws MikrotikApiException;
    void suspender(Cliente cliente) throws MikrotikApiException;
    void reactivateEnMikrotik(Cliente cliente) throws MikrotikApiException;
    String getHost();
    java.util.List<java.util.Map<String, String>> listPPPoEProfiles() throws MikrotikApiException;
}
