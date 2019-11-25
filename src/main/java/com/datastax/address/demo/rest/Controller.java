package com.datastax.address.demo.rest;

import com.datastax.address.demo.dao.AddressDao;
import com.datastax.address.demo.model.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class Controller {


    @Autowired
    private AddressDao dao;

    @GetMapping("/search")
    private Flux<Address> searchAddress(@RequestParam(value = "address", required = true) String address) {
        if (address == null || address.length() < 4) {
            return Flux.empty();
        }
        return dao.search(address);
    }

    @GetMapping("/findAddress")
    private Mono<Address> findAddress(@RequestParam(value = "localisation", required = true) String localisation) {
         return dao.findAddress(localisation);
    }
}
