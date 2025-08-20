package org.wispcrm.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    public Connection conectar() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/tecnowisp?user=root&password=admin";
        return DriverManager.getConnection(url);

    }
}