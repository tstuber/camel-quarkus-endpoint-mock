package org.acme;

import io.quarkus.test.Mock;
import org.apache.camel.Endpoint;

import javax.inject.Named;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.mock;

@Mock
public class MockKafkaEndpointProducer extends KafkaEndpointProducer {

    @Named("kafkaToEndpoint")
    @Override
    public Endpoint createToEndpoint() {
        return mock("kafkaToEndpointMock").resolve(super.context);
    }

    @Named("kafkaFromEndpoint")
    @Override
    public Endpoint createFromEndpoint() {
        return direct("kafkaFromEndpointMock").resolve(super.context);
    }
}
