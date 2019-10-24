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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

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
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Tag;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import server.activemq.generals.Constants;
import server.hazelcast.generals.General;

/**
 * @author Gabriel Dimitriu
 *
 */
public class PopulateWehrmacht {
	//list of generals
	private List<General> generals = new ArrayList<>(); 
	/** SQS queue client */
	private SQSConnection sqsConnection;
	/** SNS topic client */
	private AmazonSNS snsClient;
	/**
	 * 
	 */
	public PopulateWehrmacht() {
		Random random = new Random();
		generals.add(new General("Erwing", "Rommel", "1", 1944, random.nextInt()));
		generals.add(new General("Gerd", "Rundstedt", "2", 1953, random.nextInt()));
		generals.add(new General("Erich", "Manstein", "3", 1973, random.nextInt()));
	}

	/**
	 * create the connection to the sqs as jms and sns
	 * @throws JMSException
	 */
	public void createConnection() throws JMSException {
		//connect to the sns ans sqs
		AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
		sqsConnection = new SQSConnectionFactory(new ProviderConfiguration(), AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build()).createConnection();
		snsClient = AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}
	
	/**
	 * populate the queue with the generals.
	 * @throws JMSException
	 */
	public void populateQueue() throws JMSException {
		Session session = null;
		try {
			session = sqsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(Constants.WEHRMACHT + ".fifo");
			MessageProducer producer = session.createProducer(queue);
			for (General general : generals) {
				ObjectMessage message = session.createObjectMessage(general);
				message.setStringProperty("JMSXGroupID", "default");
				producer.send(message);
			}
		} catch (JMSException ex) {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException ex1) {
					ex1.printStackTrace();
				}
			}
		}
		CreateTopicRequest topicRequest = new CreateTopicRequest().withName(Constants.COMMAND).withTags(new Tag().withKey("Name").withValue(Constants.COMMAND));
		String commandTopicArn = snsClient.createTopic(topicRequest).getTopicArn();
		PublishRequest publishRequest = new PublishRequest().withMessage(Constants.COMMAND_WEHRMACHT).withTopicArn(commandTopicArn).withSubject(Constants.COMMAND_WEHRMACHT);
		snsClient.publish(publishRequest);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PopulateWehrmacht wehrmacht = new PopulateWehrmacht();
		try {
			wehrmacht.createConnection();
			wehrmacht.populateQueue();
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}
	

}
