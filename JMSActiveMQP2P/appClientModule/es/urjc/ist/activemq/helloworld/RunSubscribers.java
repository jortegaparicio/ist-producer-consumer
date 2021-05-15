
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

public class RunSubscribers {
  
	// Parameter to select the number of subscribers running concurrently in the thread pool
	private static final int NSUBS = 3;	
	
	//URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;  // "tcp://localhost:61616"
	
	// Create a new thread pool to run publishers concurrently
	private static ExecutorService SubPool = Executors.newFixedThreadPool(NSUBS);
		
	
	private static void shutdownAndAwaitTermination(int firstTimeout, int secondTimeout) {
		SubPool.shutdown(); 
		try {

			if (!SubPool.awaitTermination(firstTimeout, TimeUnit.SECONDS)) {
				System.err.println("Uncompleted tasks. forcing closure...");
				SubPool.shutdownNow(); 
				if (!SubPool.awaitTermination(secondTimeout, TimeUnit.SECONDS))
					System.err.println("Unended thread pool");
			}
		} catch (InterruptedException ie) {
			SubPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	
	public static void main(String[] args) {
		
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

		// Running producers over the thread pool
		for (int count = 0; count < NSUBS; count++) {

			Subscriber consumer = new Subscriber(connectionFactory);

			SubPool.submit(consumer); 
		}
		
		// Closing thread pool
		shutdownAndAwaitTermination(60, 60);

		System.err.println("\nEND");
	}
}
