package com.spring.mspaycredit.service.impl;

import com.spring.mspaycredit.entity.Credit;
import com.spring.mspaycredit.entity.CreditTransaction;
import com.spring.mspaycredit.repository.CreditTransactionRepository;
import com.spring.mspaycredit.service.CreditTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CreditTransactionServiceImpl implements CreditTransactionService {

	private final WebClient webClient;
	private final ReactiveCircuitBreaker reactiveCircuitBreaker;
	
	String uri = "http://gateway:8090/api/ms-credit-charge/creditCharge/find/{id}";
	
	public CreditTransactionServiceImpl(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
		this.webClient = WebClient.builder().baseUrl(this.uri).build();
		this.reactiveCircuitBreaker = circuitBreakerFactory.create("creditcharge");
	}
	
    @Autowired
    private CreditTransactionRepository creditTransactionRepository;
    
    // Plan A
    @Override
    public Mono<Credit> findCredit(String id) {
		return reactiveCircuitBreaker.run(webClient.get().uri(this.uri,id).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Credit.class),
				throwable -> {
					return this.getDefaultCreditCard();
				});
    }
    
    // Plan B
  	public Mono<Credit> getDefaultCreditCard() {
  		Mono<Credit> credit = Mono.just(new Credit("0", null, null,null));
  		return credit;
  	}
    
    @Override
    public Mono<CreditTransaction> create(CreditTransaction t) {
        return creditTransactionRepository.save(t);
    }

    @Override
    public Flux<CreditTransaction> findAll() {
        return creditTransactionRepository.findAll();
    }

    @Override
    public Mono<CreditTransaction> findById(String id) {
        return creditTransactionRepository.findById(id);
    }

    @Override
    public Mono<CreditTransaction> update(CreditTransaction t) {
        return creditTransactionRepository.save(t);
    }

    @Override
    public Mono<Boolean> delete(String t) {
        return creditTransactionRepository.findById(t)
                .flatMap(tar -> creditTransactionRepository.delete(tar).then(Mono.just(Boolean.TRUE)))
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Flux<CreditTransaction> findCreditsPaid(String id) {
        return creditTransactionRepository.findByCreditId(id);
    }

}
