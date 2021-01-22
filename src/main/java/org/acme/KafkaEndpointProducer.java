package org.acme;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.kafka;

@Dependent
public class KafkaEndpointProducer {

    @Inject
    CamelContext context;

    @ConfigProperty(name = "kafka.ssl.keystore.location")
    String kafkaSslKeystoreLocation;

    @ConfigProperty(name = "kafka.ssl.keystore.password")
    String kafkaSslKeystorePassword;

    @ConfigProperty(name = "kafka.ssl.truststore.location")
    String kafkaSslTruststoreLocation;

    @ConfigProperty(name = "kafka.ssl.truststore.password")
    String kafkaSslTruststorePassword;

    // Define SSL context (required for camel's fluent DSL
    private SSLContextParameters getSslContext() {

        // Configure keystore
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(kafkaSslKeystoreLocation);
        ksp.setPassword(kafkaSslKeystorePassword);
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyStore(ksp);

        // Configure keystore
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource(kafkaSslTruststoreLocation);
        tsp.setPassword(kafkaSslTruststorePassword);
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(tsp);

        // Define SSL context
        SSLContextParameters scp = new SSLContextParameters();
        scp.setKeyManagers(kmp);
        scp.setTrustManagers(tmp);

        return scp;
    }

    @Named("kafkaToEndpoint")
    public Endpoint createToEndpoint() {
        return kafka("{{kafka.topic}}")
                .brokers("{{kafka.bootstrap.servers}}")
                .securityProtocol("{{kafka.security.protocol}}")
                .sslContextParameters(getSslContext())
                .sslKeystoreType("{{kafka.ssl.keystore.type}}")
                .sslTruststoreType("{{kafka.ssl.truststore.type}}")
                .schemaRegistryURL("{{schema.registry.url}}")
                .valueSerializer ("{{kafka.value.serializer}}")
                .resolve(context);
    }

    @Named("kafkaFromEndpoint")
    public Endpoint createFromEndpoint() {
        return kafka("{{kafka.topic}}")
                .brokers("{{kafka.bootstrap.servers}}")
                .groupId("{{kafka.group.id}}")
                .securityProtocol("{{kafka.security.protocol}}")
                .sslContextParameters(getSslContext())
                .sslKeystoreType("{{kafka.ssl.keystore.type}}")
                .sslTruststoreType("{{kafka.ssl.truststore.type}}")
                .schemaRegistryURL("{{schema.registry.url}}")
                .specificAvroReader(true)
                .valueDeserializer("{{kafka.value.deserializer}}")
                .resolve(context);
    }
}