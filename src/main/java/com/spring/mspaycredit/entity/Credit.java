package com.spring.mspaycredit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Credit {

    private String id;

    private CreditCard creditCard;

    private Status status;
    
    private Double amount;

    private LocalDateTime date;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate expirationDate;

    public enum Status{
    	CREATED,
    	PAIDOUT,
    	DEFEATED
    }
    
}
