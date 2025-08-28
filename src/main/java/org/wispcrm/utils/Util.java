package org.wispcrm.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
public class Util {

    public Util() {
        // TODO Auto-generated constructor stub
    }

    public static Object formatearMoneda(Long numero) {
        Locale locale = new Locale("es", "CO");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        if (null != numero) {
            return currencyFormatter.format(numero.doubleValue());
        } else {
            return currencyFormatter.format(0.0);
        }
    }

    public static String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getName());
        return authentication.getName();
    }

    public static void logClienteOperation(String operationValue,String result, String msg, Object object) {
        try {
            MDC.put("operation", operationValue);
            MDC.put("response", result);
            MDC.put("usuario", currentUserName());
            log.info(msg, object != null ? object : "null");
        } finally {
            MDC.remove("operation");
            MDC.remove("response");
        }
    }
}
