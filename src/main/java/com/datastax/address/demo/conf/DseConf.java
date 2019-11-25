package com.datastax.address.demo.conf;

import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.dse.driver.api.core.DseSessionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.List;

@Configuration
public class DseConf {

    @Bean
    public DseSession session() {
        return new DseSessionBuilder().build();
    }
}
