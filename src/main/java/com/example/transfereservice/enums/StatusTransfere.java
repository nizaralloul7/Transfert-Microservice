package com.example.transfereservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusTransfere
{
    A_SERVIR("a servir"), PAYE("paye"), BLOQUE("bloque"), DEBLOQUE_A_SERVIR("bloque a servir"), EXTOURNE("extourne"), DESHERENCE("desherence");

    private String value;

    StatusTransfere(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return this.value;
    }

}
