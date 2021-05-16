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

/**
 * <h1>RunPublishers class</h1>
 * 
 * <p> The RunPublishers class launches N concurrent publishers for 
 * the publisher/subscriber pattern.
 * <p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 16/05/2021
 */
public class RunPublishers {
  
	// Parameter to select the number of publishers running concurrently in the thread pool
	private static final int NPUBLISHERS = 3;	
	
	// Run ActiveMQ service as independent process. The URL of the JMS server is on "tcp://localhost:61616"
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;  
	
	// Create a new thread pool to run publishers concurrently
	private static ExecutorService PubPool = Executors.newFixedThreadPool(NPUBLISHERS);
		
	
	/**
	 * Method to close the ExecutorService in two stages: first avoiding running new
	 * tasks in the pool and after, requesting the tasks running to finish.
	 * 
	 * @param firstTimeout Timeout to the first waiting stage.
	 * @param secondTimeout Timeout to the second waiting stage.
	 */
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
	
	
	/**
	 * This method waits all threads to finish and then it closes all connections
	 * 
	 * @param connection This is the connection that this method will close
	 * @param connectionFactory This is the connectionFactory where open a new connection
	 */
	private static void closeConnection(ActiveMQConnectionFactory connectionFactory) {
		
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
			System.err.println("Sending CLOSE message to Topic");
			
			closer.close();
			session.close();
			connection.close();

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method to run the RunPublishers program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			// Create a new connectionFactory with ActiveMQ service, with broker in localhost
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		
			// Running publishers over the thread pool
			for (int count = 0; count < NPUBLISHERS; count++) {

				// Asynchronous publisher is created here 
				Publisher publisher = new Publisher(connectionFactory);
				
				// Run publisher
				PubPool.submit(publisher); 
			}

			// Closing thread pool
			shutdownAndAwaitTermination(60, 60);
					
			// sending CLOSE message to the subscribers
			closeConnection(connectionFactory);
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			closeConnection(new ActiveMQConnectionFactory(url));
		}
		
		System.err.println("\nEND");
	}
}
