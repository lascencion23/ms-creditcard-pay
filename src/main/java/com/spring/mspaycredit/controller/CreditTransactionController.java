package com.spring.mspaycredit.controller;

import com.spring.mspaycredit.entity.Credit.Status;
import com.spring.mspaycredit.entity.CreditTransaction;
import com.spring.mspaycredit.service.CreditTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/creditPaid")
public class CreditTransactionController {

	@Autowired
	CreditTransactionService creditTransactionService;

	@GetMapping("list")
	public Flux<CreditTransaction> findAll() {
		return creditTransactionService.findAll();
	}

	@GetMapping("/find/{id}")
	public Mono<CreditTransaction> findById(@PathVariable String id) {
		return creditTransactionService.findById(id);
	}

	@GetMapping("/findByIdCustomer/{id}")
	public Flux<CreditTransaction> findByIdCustomer(@PathVariable String id){
		return creditTransactionService.findByIdCustomer(id);
	}
	
	@GetMapping("/findByIdCreditCard/{id}")
	public Flux<CreditTransaction> findByIdCreditCard(@PathVariable String id){
		return creditTransactionService.findByIdCreditCard(id);
	}
	
	@PostMapping("/create")
    public Mono<ResponseEntity<CreditTransaction>> create(@RequestBody CreditTransaction creditTransaction){
        // BUSCO EL CREDITO QUE SE PRETENDE HACER EL PAGO
        return creditTransactionService.findCredit(creditTransaction.getCredit().getId())
                .flatMap(credit -> creditTransactionService.findCreditsPaid(credit.getId()) // TODAS PAGOS DE ESTE CREDITO
                                    .collectList()
                                    .filter(listCt -> credit.getAmount() >= listCt.stream().mapToDouble(ct -> ct.getTransactionAmount()).sum() + creditTransaction.getTransactionAmount())
                                    .flatMap(listCt -> {
                                    	credit.setStatus(Status.PAIDOUT); 
                                        return creditTransactionService.updateCredit(credit)
                                        		.flatMap(cdUp -> {
                                        			creditTransaction.setCredit(cdUp);
                                        			creditTransaction.setTransactionDateTime(LocalDateTime.now());
                                        			return creditTransactionService.create(creditTransaction);
                                        		});

                                    })
                )
                .map(ct -> {
                	return new ResponseEntity<>(ct , HttpStatus.CREATED);
                })
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

	@PutMapping("/update")
	public Mono<ResponseEntity<CreditTransaction>> update(@RequestBody CreditTransaction creditTransaction) {
		return creditTransactionService.findById(creditTransaction.getId())
				.flatMap(ctDB -> creditTransactionService.findCredit(creditTransaction.getCredit().getId())
						.flatMap(credit -> creditTransactionService.findCreditsPaid(credit.getId()).collectList()
								.filter(listCt -> credit.getAmount() >= listCt.stream()
										.mapToDouble(ct -> ct.getTransactionAmount()).sum()
										- ctDB.getTransactionAmount() + creditTransaction.getTransactionAmount())
								.flatMap(listCt -> {
									creditTransaction.setCredit(credit);
									creditTransaction.setTransactionDateTime(LocalDateTime.now());
									return creditTransactionService.create(creditTransaction);
								}))

				).map(ct -> new ResponseEntity<>(ct, HttpStatus.CREATED))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
	}

	@DeleteMapping("/delete/{id}")
	public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
		return creditTransactionService.delete(id).filter(deleteCustomer -> deleteCustomer)
				.map(deleteCustomer -> new ResponseEntity<>("Customer Deleted", HttpStatus.ACCEPTED))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
