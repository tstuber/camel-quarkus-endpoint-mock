package org.acme;

import org.acme.model.AvroEntry;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class KafkaRoutesTest {

    @Inject
    CamelContext context;

    @Inject
    ProducerTemplate producerTemplate;

    MockEndpoint mockKafkaToEndpoint;

    private static final Logger LOG_ROUTE_FROM_KAFKA = Logger.getLogger("routeFromKafka");
    private static final Logger LOG_ROUTE_TO_KAFKA = Logger.getLogger("routeToKafka");
    LogHandler handler;

    @BeforeEach
    public void setup() {
        mockKafkaToEndpoint = context.getEndpoint("mock:kafkaToEndpointMock", MockEndpoint.class);
        handler = new LogHandler();
        LOG_ROUTE_FROM_KAFKA.addHandler(handler);
        LOG_ROUTE_TO_KAFKA.addHandler(handler);
    }

    @AfterEach
    public void teardown() {
        mockKafkaToEndpoint.reset();
    }

    @Test
    public void SendToKafkaTests() throws Exception {
        String bodyValue = "done";

        Long id = 1L;
        String message = "message";

        AvroEntry entry = new AvroEntry(id, message);
        String entryJson = String.format("{\"id\": %s, \"message\": \"%s\"}", entry.getId(), entry.getMessage());

        mockKafkaToEndpoint.expectedMessageCount(1);
        mockKafkaToEndpoint.expectedBodiesReceivedInAnyOrder(entryJson);

        given()
                .header("Content-type", "application/json").and().body(entryJson)
                .when().post("/event").then().statusCode(200)
                .body("message", equalTo(bodyValue));

        mockKafkaToEndpoint.assertIsSatisfied();

        ArrayList<LogRecord> actualLogEntries = handler.getLogEntries();
        assertEquals(1, actualLogEntries.size());
        assertEquals(actualLogEntries.get(0).getLoggerName(), "routeToKafka");
        assertEquals(actualLogEntries.get(0).getMessage(), "Received {\"id\": 1, \"message\": \"message\"}");
        assertEquals(Level.INFO, actualLogEntries.get(0).getLevel());
    }

    @Test
    public void SendFromKafkaTests() throws Exception {

        Long id = 1L;
        String message = "message";

        AvroEntry entry = new AvroEntry(id, message);

        producerTemplate.sendBody("direct:kafkaFromEndpointMock", entry);

        ArrayList<LogRecord> actualLogEntries = handler.getLogEntries();
        assertEquals(1, actualLogEntries.size());
        assertEquals("routeFromKafka", actualLogEntries.get(0).getLoggerName());
        assertEquals("Avro Message: Entry.id = 1, Entry.message = message", actualLogEntries.get(0).getMessage());
        assertEquals(Level.INFO, actualLogEntries.get(0).getLevel());
    }
}
