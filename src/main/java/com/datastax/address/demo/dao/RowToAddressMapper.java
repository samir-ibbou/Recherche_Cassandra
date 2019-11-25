package com.datastax.address.demo.dao;

import com.datastax.address.demo.model.Address;
import com.datastax.dse.driver.api.core.data.geometry.Point;
import com.datastax.oss.driver.api.core.cql.Row;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Function;

@Component
public class RowToAddressMapper implements Function<Row, Address> {

    @Override
    public Address apply(Row row) {
        var id = Objects.requireNonNull(row.getString("id"), "column id cannot be null");
        var city = Objects.requireNonNull(row.getString("city"), "column city cannot be null");
        var coord = Objects.requireNonNull(row.get("coord", Point.class), "column coord cannot be null");
        var num = row.getString("num");
        var type = row.getString("type");
        var zipcode = Objects.requireNonNull(row.getString("zipcode"), "column zipcode cannot be null");
        return new Address(id, city, coord.X(), coord.Y(), num, type, zipcode);
    }
}