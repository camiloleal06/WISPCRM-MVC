package org.wispcrm.mikrotik;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.wispcrm.modelo.clientes.Cliente;
import org.wispcrm.modelo.profiles.Profile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Primary
@org.springframework.context.annotation.Profile("mock-mikrotik")
public class MockMikrotikGateway implements MikrotikGateway {

    // Estado en memoria: simula los secrets PPPoE y la lista Moroso
    private final Map<String, Map<String, String>> pppoeSecrets = new HashMap<>();
    private final Set<String> morosos = new HashSet<>();

    @Override
    public boolean testConnection() {
        log.info("[MOCK-MIKROTIK] testConnection -> true");
        return true;
    }

    @Override
    public String getHost() {
        return "mock-host";
    }

    @Override
    public void addPPPoE(org.wispcrm.modelo.profiles.Profile profile, Cliente cliente) {
        String user = cliente.getPppoeUser();
        if (pppoeSecrets.containsKey(user)) {
            log.info("[MOCK-MIKROTIK] PPPoE ya existe: {}", user);
            return;
        }
        Map<String, String> secret = new HashMap<>();
        secret.put("name", user);
        secret.put("password", cliente.getPppoePass());
        secret.put("profile", profile.getName());
        secret.put("remote-address", cliente.getIpAddress());
        secret.put("service", "pppoe");
        pppoeSecrets.put(user, secret);
        log.info("[MOCK-MIKROTIK] PPPoE creado: {} | profile={} | ip={}", user, profile.getName(), cliente.getIpAddress());
    }

    @Override
    public void updatePPPoE(org.wispcrm.modelo.profiles.Profile profile, Cliente cliente) {
        String user = cliente.getPppoeUser();
        Map<String, String> secret = pppoeSecrets.getOrDefault(user, new HashMap<>());
        secret.put("name", user);
        secret.put("password", cliente.getPppoePass());
        secret.put("profile", profile.getName());
        secret.put("remote-address", cliente.getIpAddress());
        secret.put("service", "pppoe");
        pppoeSecrets.put(user, secret);
        log.info("[MOCK-MIKROTIK] PPPoE {}: {} | profile={} | ip={}",
                pppoeSecrets.containsKey(user) ? "actualizado" : "creado", user, profile.getName(), cliente.getIpAddress());
    }

    @Override
    public void suspender(Cliente cliente) {
        String ip = cliente.getIpAddress();
        if (ip == null || ip.isBlank()) {
            log.warn("[MOCK-MIKROTIK] suspender: cliente {} sin IP", cliente.getNombres());
            return;
        }
        morosos.add(ip);
        log.info("[MOCK-MIKROTIK] IP {} agregada a Morosos (cliente: {})", ip, cliente.getNombres());
    }

    @Override
    public void reactivateEnMikrotik(Cliente cliente) {
        String ip = cliente.getIpAddress();
        if (ip == null || ip.isBlank()) return;
        boolean removed = morosos.remove(ip);
        log.info("[MOCK-MIKROTIK] IP {} {} de Morosos (cliente: {})", ip, removed ? "removida" : "no estaba", cliente.getNombres());
    }

    @Override
    public java.util.List<java.util.Map<String, String>> listPPPoEProfiles() {
        log.info("[MOCK-MIKROTIK] listPPPoEProfiles -> []");
        return java.util.List.of();
    }

    // Métodos de inspección para tests
    public boolean existePPPoE(String user) { return pppoeSecrets.containsKey(user); }
    public Map<String, String> getPPPoE(String user) { return pppoeSecrets.get(user); }
    public boolean esMoroso(String ip) { return morosos.contains(ip); }
    public void reset() { pppoeSecrets.clear(); morosos.clear(); }
}
