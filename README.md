# SFDC streaming

This is a quick & dirty example that shows how to capture changes in Salesforce.com, in real-time, and publish them to a Kafka topic.

First, it's necessary to create a topic based on an SOQL query. To do that, tweak the snippet of Apex code and execute it:

    PushTopic pushTopic = new PushTopic();
    pushTopic.Name = 'ContactUpdates';
    pushTopic.Query = 'SELECT id, firstname, lastname, accountid, email FROM contact';
    pushTopic.ApiVersion = 39.0;
    pushTopic.NotifyForOperationCreate = true;
    pushTopic.NotifyForOperationUpdate = true;
    pushTopic.NotifyForOperationUndelete = true;
    pushTopic.NotifyForOperationDelete = true;
    pushTopic.NotifyForFields = 'Referenced';
    insert pushTopic;

Then edit the properties file (`src/main/java/io/woolford/resources/application.properties`) - add your own SFDC login, and Kafka broker.

The `sfdc.password` property is the concatenation of the password *and* the SFDC security token.

This project depends on the `emp-connector` artifact from `com.salesforce.conduit`. This dependency doesn't exist in Maven Central. You could install it locally:

    git clone https://github.com/forcedotcom/EMP-Connector.git
    cd EMP-Connector
    mvn clean install

Build and run the project:

    mvn clean package
    nohup java -jar target/sfdc-streaming-1.0-SNAPSHOT.jar &

That's it! Changes to contacts will be published, in JSON format, to the 'contact-updates' Kafka topic.

[![Publish updates from SFDC to Kafka](https://img.youtube.com/vi/UKwUOlF5GFM/0.jpg)](https://www.youtube.com/watch?v=UKwUOlF5GFM)


