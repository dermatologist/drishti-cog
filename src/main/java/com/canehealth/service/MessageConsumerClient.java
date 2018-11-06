package com.canehealth.service;

import com.canehealth.model.OAuth2AccessToken;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;
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
            from("timer://scheduler?period=30s")
                    .log("get access token")
                    .to("direct:authService");

            from("direct:authService").tracing()
                    .setHeader(Exchange.HTTP_PATH)
                    .simple("<auth service context>/oauth2/token")
                    .setHeader("CamelHttpMethod")
                    .simple("POST")
                    .setHeader("Content-Type")
                    .simple("application/x-www-form-urlencoded")
                    .setHeader("Accept")
                    .simple("application/json")
                    .setBody()
                    .constant("grant_type=client_credentials&client_id=<client id>&client_secret=<client sec>")
                    .to("https4://<remote auth service url>")
                    .convertBodyTo(String.class)
                    .log("response from API: " + body())
                    .choice()
                    .when().simple("${header.CamelHttpResponseCode} == 200")
                    .unmarshal().json(JsonLibrary.Jackson, OAuth2AccessToken.class)
                    .setHeader("jwt").simple("${body.access_token}")
                    .to("direct:<some direct route>")
                    .otherwise()
                    .log("Not Authenticated!!!");

            from("direct:<some direct route>").tracing()
                    .log("body: " + body().toString())
                    .setBody().constant(null)
                    .setHeader(Exchange.HTTP_PATH)
                    .simple("v1/canais")
                    .setHeader("CamelHttpMethod")
                    .simple("GET")
                    .setHeader("Accept")
                    .simple("application/json")
                    .setHeader("Authorization")
                    .simple("${header.jwt}")     // <<<<<< HERE YOU GET YOUR AUTH TOKEN GRANTED IN PREVIOUS ROUTE >>>>>>
                    .to("https4://<remote secured service url>")
                    .convertBodyTo(String.class)
                    .choice()
                    .when().simple("${header.CamelHttpResponseCode} == 200")
                    .setBody().javaScript(""
                    + " canais = JSON.parse(request.body);"
                    + " idx = Math.floor(Math.random() * (canais.length - 1));"
                    + " result = canais[idx].id;"
                    + "")
                    .log("response from globosat API: " + body())
                    .to("direct:<another route>")
                    .otherwise()
                    .log("Error!!!");
        }

    };

    private MessageConsumerClient() throws Exception {
        this.camelContext.addRoutes(this.routeBuilder);
//        this.from = from;
//        this.to = to;
    }

    public void consume() {
        try {
            camelContext.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }
}
