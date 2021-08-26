package com.spring.mspaycredit.entity;

import lombok.Data;

@Data
public class Customer {

    private String id;

    private String name;

    private String lastName;

    private TypeCustomer typeCustomer;

    private DocumentType documentType;
    
    private String documentNumber;

    public enum DocumentType {
    	DNI,
    	PASAPORTE
    }
}

