package org.wispcrm.mikrotik;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
@NoArgsConstructor
public class ConfigMikrotik {
    @Value("${mikrotik.host}")
    private String host;
    @Value("${mikrotik.username}")
    private String username;
    @Value("${mikrotik.password}")
    private String password;
}