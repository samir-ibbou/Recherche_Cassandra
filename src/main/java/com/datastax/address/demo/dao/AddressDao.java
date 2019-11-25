package com.datastax.address.demo.dao;

import com.datastax.address.demo.model.Address;
import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.stream.Collectors;

@Repository
public class AddressDao {

    @Autowired
    private DseSession dseSession;

    @Autowired
    private RowToAddressMapper rowMapper;


    private PreparedStatement solrPreparedStatement;

    @PostConstruct
    public void init() {
        solrPreparedStatement = dseSession.prepare("SELECT * from address where solr_query=? limit 10");
    }

    public Flux<Address> search(String address) {

        String query = makeSearch(address.replace("\"", " "));
        System.out.println(query);
        BoundStatement searchStatement = solrPreparedStatement.bind(query).setIdempotent(true);
        ReactiveResultSet rs = dseSession.executeReactive(searchStatement);
        return Flux.from(rs).map(rowMapper);
    }

    //add * to the last element and fuzzy search
    private String makeSearch(String address) {
        String lastOptions = address.endsWith(" ") ? (address.matches(".*\\d\\s+") ? "" : "~1") : ("*");
        String[] split = address.split("\\s+");
        String[] splitWithoutLastElement = Arrays.copyOf(split, split.length - 1);
        var strings = Arrays.stream(splitWithoutLastElement).map(s -> {
            if (s.matches("\\d+")) return s;
            else return s + "~1";
        }).collect(Collectors.toList());
        strings.add(split[split.length - 1] + lastOptions);
        return "{\"q\":\"full_address:(" + String.join(" AND ", strings) + ")\",\"sort\":\"score desc\"}";
    }

    private String makeSimpleSearch(String address) {
        var strings = address.split("\\s+");
        return "{\"q\":\"full_address:(" + String.join(" AND ", strings) + ")\",\"sort\":\"score desc\"}";
    }

    //add * to the last element but no fuzzy search
    private String makeSimple2Search(String address) {
        address = address.endsWith(" ") ? address : (address + "*");
        var strings = address.split("\\s+");
        return "{\"q\":\"full_address:(" + String.join(" AND ", strings) + ")\",\"sort\":\"score desc\"}";
    }

    public Mono<Address> findAddress(String localisation) {
        String query="{\"q\":\"{!geofilt sfield=coord pt="+localisation+" d=1.0 score=recipDistance}\",\"sort\":\"score desc\"}";
        System.out.println(query);
        BoundStatement searchStatement = solrPreparedStatement.bind(query).setIdempotent(true);
        ReactiveResultSet rs = dseSession.executeReactive(searchStatement);
        return Mono.from(rs).map(rowMapper);
    }
}
