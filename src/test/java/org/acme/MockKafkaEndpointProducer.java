package org.acme;

import io.quarkus.test.Mock;
import org.apache.camel.Endpoint;

import javax.inject.Inject;
import javax.inject.Named;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.mock;

@Mock
public class MockKafkaEndpointProducer extends KafkaEndpointProducer {

    @Named("kafkaToEndpoint")
    @Override
    public Endpoint createToEndpoint() {
        return mock("kafkaToKafka").resolve(super.context);
    }

    @Named("kafkaFromEndpoint")
    @Override
    public Endpoint createFromEndpoint() {
        return direct("kafkaFromKafka").resolve(super.context);
    }
}
