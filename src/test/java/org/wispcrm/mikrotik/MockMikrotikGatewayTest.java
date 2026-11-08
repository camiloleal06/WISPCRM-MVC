package org.wispcrm.mikrotik;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.profiles.Profile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MockMikrotikGatewayTest {

    private MockMikrotikGateway gateway;
    private Profile profile;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        gateway = new MockMikrotikGateway();

        profile = new Profile();
        profile.setName("plan-10mb");

        cliente = new Cliente();
        cliente.setNombres("Juan Perez");
        cliente.setPppoeUser("juan.perez");
        cliente.setPppoePass("pass123");
        cliente.setIpAddress("192.168.1.100");
    }

    @Test
    void addPPPoE_creaSecretNuevo() {
        gateway.addPPPoE(profile, cliente);

        assertTrue(gateway.existePPPoE("juan.perez"));
        Map<String, String> secret = gateway.getPPPoE("juan.perez");
        assertEquals("plan-10mb", secret.get("profile"));
        assertEquals("192.168.1.100", secret.get("remote-address"));
        assertEquals("pppoe", secret.get("service"));
    }

    @Test
    void addPPPoE_noSobreescribeSiYaExiste() {
        gateway.addPPPoE(profile, cliente);

        // Cambiar profile e intentar crear de nuevo
        Profile otroPlan = new Profile();
        otroPlan.setName("plan-20mb");
        gateway.addPPPoE(otroPlan, cliente);

        // Debe conservar el original
        assertEquals("plan-10mb", gateway.getPPPoE("juan.perez").get("profile"));
    }

    @Test
    void updatePPPoE_actualizaSecretExistente() {
        gateway.addPPPoE(profile, cliente);

        Profile nuevoPlan = new Profile();
        nuevoPlan.setName("plan-20mb");
        cliente.setPppoePass("newpass456");
        cliente.setIpAddress("192.168.1.200");

        gateway.updatePPPoE(nuevoPlan, cliente);

        Map<String, String> secret = gateway.getPPPoE("juan.perez");
        assertEquals("plan-20mb", secret.get("profile"));
        assertEquals("192.168.1.200", secret.get("remote-address"));
    }

    @Test
    void updatePPPoE_creaSecretSiNoExiste() {
        gateway.updatePPPoE(profile, cliente);

        assertTrue(gateway.existePPPoE("juan.perez"));
    }

    @Test
    void suspender_agregaIpAMorosos() {
        gateway.suspender(cliente);

        assertTrue(gateway.esMoroso("192.168.1.100"));
    }

    @Test
    void reactivar_removeIpDeMorosos() {
        gateway.suspender(cliente);
        gateway.reactivateEnMikrotik(cliente);

        assertFalse(gateway.esMoroso("192.168.1.100"));
    }

    @Test
    void suspender_sinIp_noFalla() {
        cliente.setIpAddress(null);
        assertDoesNotThrow(() -> gateway.suspender(cliente));
    }

    @Test
    void testConnection_siempreTrue() {
        assertTrue(gateway.testConnection());
    }

    @Test
    void reset_limpiaTodoElEstado() {
        gateway.addPPPoE(profile, cliente);
        gateway.suspender(cliente);

        gateway.reset();

        assertFalse(gateway.existePPPoE("juan.perez"));
        assertFalse(gateway.esMoroso("192.168.1.100"));
    }
}
