package org.wispcrm.restcontroller;

import lombok.Builder;

@Builder
public class Bill {
    private String value;
    private String transactionId;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Bill(String value, String transactionId) {
        this.value = value;
        this.transactionId = transactionId;
    }
}
