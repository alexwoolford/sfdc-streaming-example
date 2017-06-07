package io.woolford;

import static com.salesforce.emp.connector.LoginHelper.login;

import java.util.Map;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.emp.connector.BayeuxParameters;
import com.salesforce.emp.connector.EmpConnector;
import com.salesforce.emp.connector.TopicSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class EventTransport {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${sfdc.username}")
    private String sfdcUsername;

    @Value("${sfdc.password}")
    private String sfdcPassword;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @PostConstruct
    private void transportEvents() throws Exception {

        long replayFrom = EmpConnector.REPLAY_FROM_EARLIEST;

        // login
        BayeuxParameters params;
        try {
            params = login(sfdcUsername, sfdcPassword);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
            throw e;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        // define consoumer
        Consumer<Map<String, Object>> consumer = (event) -> {
            String eventJSON;
            try {
                eventJSON = objectMapper.writeValueAsString(event);
                logger.info("eventJSON: " + eventJSON);
                kafkaTemplate.send("contact-updates", eventJSON);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        };

        // connect to SFDC using login
        EmpConnector connector = new EmpConnector(params);
        connector.start().get(5, TimeUnit.SECONDS);

        // subscribe to events; process events with consumer
        TopicSubscription subscription = connector.subscribe("/topic/ContactUpdates",
                replayFrom, consumer).get(5, TimeUnit.SECONDS);

        logger.info("Subscribed to " + subscription);

    }
}