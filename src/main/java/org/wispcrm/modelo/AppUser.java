package org.wispcrm.modelo;

import lombok.Getter;

@Getter
public enum AppUser {

    CAMILO("camiloleal", "T3CN02020+-+", Role.ADMIN),
    FERNANDO("fernando", "Fernando2020+-", Role.USER),
    AUDILUZ("audiluz", "audiluz", Role.USER),
    JOSE("jose", "jose", Role.USER),
    CACHI("cachi", "cachi", Role.USER);

    private final String username;
    private final String password;
    private final Role role;

    AppUser(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public enum Role {
        ADMIN, USER
    }
}
