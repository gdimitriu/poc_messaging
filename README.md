# poc_messaging
Proof of concept for Messaging in java

-- from_activeMQ are some examples from official site to test if the system works correctly.

-- for package : server.solace.generals
  * If the GeneralServer is used the following should be done on solace server:
  * create durable queue: alliesgenerals with owner=default, non-exclusive and all other permission=consume
  * create durable queue: wehrmachtgenerals with owner=default, non-exclusive and all other permission=consume
  * create durable topic: command with no owner, exclusive, readonly for others
  * If the GeneralServerWithProvision is used it will create authomatically the queues.