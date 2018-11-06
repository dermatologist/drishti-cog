package com.canehealth.service;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MessageConsumerClient {

    private static final Logger LOG = LoggerFactory.getLogger(MessageConsumerClient.class);

    private Date from;
    private Date to;
    CamelContext camelContext = new DefaultCamelContext();

    RouteBuilder routeBuilder = new RouteBuilder() {

        @Override
        public void configure() {
            from("timer://simpleTimer?period=1000")
                    .setBody(simple("Hello from timer at ${header.firedTime}"))
                    .to("stream:out");
        }

    };

    private MessageConsumerClient(Date from, Date to) throws Exception {
        this.camelContext.addRoutes(this.routeBuilder);
        this.from = from;
        this.to = to;
    }

    public void consume() {
        try {
            camelContext.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
