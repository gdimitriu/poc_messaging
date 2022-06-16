# poc_messaging
Proof of concept for Messaging in java


-- from_activeMQ are some examples from official site to test if the system works correctly.

-- for package : server.solace.generals
  * If the GeneralServer is used the following should be done on solace server:
  * create durable queue: alliesgenerals with owner=default, non-exclusive and all other permission=consume
  * create durable queue: wehrmachtgenerals with owner=default, non-exclusive and all other permission=consume
  * create durable topic: command with no owner, exclusive, readonly for others
  * If the GeneralServerWithProvision is used it will create authomatically the queues.
  
 -- The generals examples are done to see different options for messaging:
 * The GeneralServer will create the queues and topic or sqs and sns with subscribe and then wait for the queue to be fill and then start the battle
 * The Populate* will populate the respective queue and then send a topic to the server that it has fil the queue   
 * The server.hazelcast.generals is the hazelcast implementation.
 * The server.activemq.generals is the activeMQ JMS provider implementation.
 * The server.solace.generals is the Solace JMS provider implementation.
 * The server.awsjms.generals is the AWS with SQS and SNS but with jms library for SQS.

-- for package: from_jms_activemq (examples from Java Messaging Service book)
 * Class name starting with Q use Queue
 * Other class using topic
 * ValidateJNDIArtemisTransactionManager is to test tx on Artemis (does not support it)

-- for package mqtt there are examples from
 * https://blogs.oracle.com/javamagazine/post/java-mqtt-iot-message-queuing
 * https://github.com/ericjbruno/mqtt_sender
 * https://github.com/ericjbruno/mqtt_listener