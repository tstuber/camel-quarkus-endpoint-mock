package org.acme;

import org.acme.model.AvroEntry;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.HttpMethod;

@ApplicationScoped
public class KafkaRoutes extends EndpointRouteBuilder {

    @Inject
    @Named("kafkaFromEndpoint")
    Endpoint kafkaFromEndpoint;

    @Inject
    @Named("kafkaToEndpoint")
    Endpoint kafkaToEndpoint;

    @Override
    public void configure() throws Exception {

        // Allows to read a stream more than once (e.g. for writing to file + save to cache)
        getContext().setStreamCaching(true);

        /*
         * REST to Kafka (with AVRO Schema)
         *
         * Endpoint can be invoked with curl, such as:
         *   curl -H "Content-Type: application/json" -X POST -d '{"id": "1", "message": "some message text"}' localhost:8080/event
         */
        from(platformHttp("/event").httpMethodRestrict(HttpMethod.POST))
                .routeId("proj:quarkus-template:kafka-output")
                .routeDescription("Reading kafka values from MDM topic")
                // Unmarshal request to POJO
                .unmarshal().json(JsonLibrary.Jackson, AvroEntry.class)
                .log("Received ${body}")

                .to(kafkaToEndpoint)
                // Set response for invoker
                .setBody(constant("{\"message\": \"done\"}"));

        /*
         * Kafka to Log (with AVRO Schema)
         */
        from(kafkaFromEndpoint)
                .routeId("proj:quarkus-template:kafka-input")
                .log("Avro Message: Entry.id = ${body.id}, Entry.message = ${body.message}");
    }
}
