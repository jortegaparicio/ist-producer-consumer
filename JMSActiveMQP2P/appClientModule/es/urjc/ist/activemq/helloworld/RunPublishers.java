package es.urjc.ist.activemq.helloworld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class RunPublishers {
  
	// Parameter to select the number of publishers running concurrently in the thread pool
	private static final int NPUBLISHERS = 3;	
	
	// Run ActiveMQ service as independent process
	//URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;  // "tcp://localhost:61616"
	
	// Create a new thread pool to run publishers concurrently
	private static ExecutorService PubPool = Executors.newFixedThreadPool(NPUBLISHERS);
		
	
	private static void shutdownAndAwaitTermination(int firstTimeout, int secondTimeout) {
		PubPool.shutdown(); 
		try {

			if (!PubPool.awaitTermination(firstTimeout, TimeUnit.SECONDS)) {
				System.err.println("Uncompleted tasks. forcing closure...");
				PubPool.shutdownNow(); 
				if (!PubPool.awaitTermination(secondTimeout, TimeUnit.SECONDS))
					System.err.println("Unended thread pool");
			}
		} catch (InterruptedException ie) {
			PubPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	public static void main(String[] args) {
		
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
	
		// Running producers over the thread pool
		for (int count = 0; count < NPUBLISHERS; count++) {

			Publisher publisher = new Publisher(connectionFactory);

			PubPool.submit(publisher); 
		}

		// Closing thread pool
		shutdownAndAwaitTermination(60, 60);
				
		// sending CLOSE message to the subscribers

		try {
			Connection connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = session.createTopic("Topic");

			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer closer = session.createProducer(destination);
			closer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create CLOSE message
			TextMessage message = session.createTextMessage("CLOSE");

			// Tell the producer to send the CLOSE message
			closer.send(message);
			System.err.println("Sending CLOSE message " + " to Topic");
			
			closer.close();
			session.close();
			connection.close();

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.err.println("\nEND");
	}
}
