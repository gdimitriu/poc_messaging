/**
    Copyright (c) 2019 Gabriel Dimitriu All rights reserved.
	DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of poc_messaging project.

    poc_messaging is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    poc_messaging is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with poc_messaging.  If not, see <http://www.gnu.org/licenses/>.
 */

package server.awsjms.generals;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.Tag;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.google.gson.Gson;

import server.activemq.generals.Constants;
import server.hazelcast.generals.General;

/**
 * @author Gabriel Dimitriu
 *
 */
public class GeneralsServer {

	/** SQS queue client */
	private SQSConnection sqsConnection;
	/** SNS topic client */
	private AmazonSNS snsClient;
	
	private String wehrmachtGeneralsUrl;
	private String alliesGeneralsUrl;
	private String commandQueueUrl;
	private String commandTopicArn;
	private String commandSubscriptionArn;
	/**
	 * @throws JMSException 
	 * 
	 */
	public GeneralsServer() throws JMSException {
		//connect to the sns ans sqs
		AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
		sqsConnection = new SQSConnectionFactory(new ProviderConfiguration(), AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build()).createConnection();
		snsClient = AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GeneralsServer server = new GeneralsServer();
			server.createFiFoQueues();
			server.createCommandStructure();
			server.waitForBattle();
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * this will create the queue for wehrmacht and allies where the generals will lie
	 * @throws JMSException 
	 */
	public void createFiFoQueues() throws JMSException {
		AmazonSQSMessagingClientWrapper sqsClient = sqsConnection.getWrappedAmazonSQSClient();
		CreateQueueRequest request = new CreateQueueRequest(Constants.WEHRMACHT + ".fifo")
				.addAttributesEntry("DelaySeconds","0")
				.addAttributesEntry("MessageRetentionPeriod","86400")
				.addAttributesEntry("ContentBasedDeduplication", "true")
				.addAttributesEntry("FifoQueue", "true");
		wehrmachtGeneralsUrl = sqsClient.createQueue(request).getQueueUrl();
		request = new CreateQueueRequest(Constants.ALLIES + ".fifo")
				.addAttributesEntry("DelaySeconds","0")
				.addAttributesEntry("MessageRetentionPeriod","86400")
				.addAttributesEntry("ContentBasedDeduplication", "true")
				.addAttributesEntry("FifoQueue", "true");
		alliesGeneralsUrl = sqsClient.createQueue(request).getQueueUrl();				
	}

	/**
	 * this will create the queue and topics for commands
	 */
	public void createCommandStructure() throws JMSException {
		AmazonSQSMessagingClientWrapper sqsClient = sqsConnection.getWrappedAmazonSQSClient();
		CreateQueueRequest queueRequest = new CreateQueueRequest(Constants.COMMAND)
				.addAttributesEntry("DelaySeconds","0")
				.addAttributesEntry("MessageRetentionPeriod","86400");
		commandQueueUrl = sqsClient.createQueue(queueRequest).getQueueUrl();
		CreateTopicRequest topicRequest = new CreateTopicRequest().withName(Constants.COMMAND).withTags(new Tag().withKey("Name").withValue(Constants.COMMAND));
		commandTopicArn = snsClient.createTopic(topicRequest).getTopicArn();
		commandSubscriptionArn = Topics.subscribeQueue(snsClient, sqsConnection.getWrappedAmazonSQSClient().getAmazonSQSClient(), commandTopicArn, commandQueueUrl);
	}
	
	private General readGeneral(Message german) throws JMSException {
		if (german instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage) german;
			return (General)objectMessage.getObject();
	    }
		return null;
	}
	
	/**
	 * wait to allied and wehrmacht to publish their generals and then begin battle
	 */
	public void waitForBattle() {
		System.out.println("Wait to start the battle !");
		Session session = null;
		try {
			session = sqsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(Constants.COMMAND);
			MessageConsumer consumer = session.createConsumer(queue);
			sqsConnection.start();
			int requestReceived = 0;
			while(requestReceived != 2) {
				Message receiveMessage = consumer.receive(10000);
				if (receiveMessage == null) {
					continue;
				}
				Map<String,String> fields = new Gson().fromJson(((TextMessage) receiveMessage).getText(), new HashMap<>().getClass());
				if (fields.containsKey("Message") && Constants.COMMAND_ALLIES.equals(fields.get("Message")) || Constants.COMMAND_WEHRMACHT.equals(fields.get("Message"))) {
						requestReceived++;
						System.out.println("Command received " + fields.get("Message"));
				}
			}
		} catch (JMSException ex) {
			ex.printStackTrace();
			return;
		}
		System.out.println("Start the battle!");
		try {
			Queue queueW = session.createQueue(Constants.WEHRMACHT  + ".fifo");
			Queue queueA = session.createQueue(Constants.ALLIES + ".fifo");
			MessageConsumer consumerW = session.createConsumer(queueW);
			MessageConsumer consumerA = session.createConsumer(queueA);
			General german = readGeneral(consumerW.receive());;
			General allied = readGeneral(consumerA.receive());
			while(german != null && allied != null) {						
				if (german.getScore() < allied.getScore()) {
					System.out.println("allied victory: " + allied.getFirstName() + ":" + allied.getSecondName() + " vs " + german.getFirstName() + ":" + german.getSecondName());
				} else {
					System.out.println("german victory: " + german.getFirstName() + ":" + german.getSecondName() + " vs " + allied.getFirstName() + ":" + allied.getSecondName());
				}
				german = readGeneral(consumerW.receiveNoWait());
				allied = readGeneral(consumerA.receiveNoWait());
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
