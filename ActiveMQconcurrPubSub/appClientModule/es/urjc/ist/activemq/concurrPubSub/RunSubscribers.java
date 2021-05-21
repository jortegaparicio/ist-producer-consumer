
package es.urjc.ist.activemq.concurrPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
//  * <h1>RunSubscribers class</h1>
/**
 * 
 * <p> The RunSubscribers class launches N concurrent subscribers for 
 * the publisher/subscriber pattern.
 * <p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 16/05/2021
 */
public class RunSubscribers {
  
	// Parameter to select the number of subscribers running concurrently in the thread pool
	private static final int NSUBS = 10;	
	
	// Run ActiveMQ service as independent process. The URL of the JMS server is on "tcp://localhost:61616"
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL; 
	
    // Create a new thread pool to run subscribers concurrently
	private static ExecutorService SubPool = Executors.newFixedThreadPool(NSUBS);
	
		
	/**
	 * Method to close the ExecutorService in two stages: first avoiding running new
	 * tasks in the pool and after, requesting the tasks running to finish.
	 * 
	 * @param firstTimeout Timeout to the first waiting stage.
	 * @param secondTimeout Timeout to the second waiting stage.
	 */
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
	
	/**
	 * Main method to run the RunSubscribers program
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			// Create a new connectionFactory with ActiveMQ service, with broker in localhost
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

			// Running subscribers over the thread pool
			for (int count = 0; count < NSUBS; count++) {

				// Asynchronous subscriber is created here 
				Subscriber subscriber = new Subscriber(connectionFactory);

				// Run subscriber
				SubPool.submit(subscriber); 
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			
			// Closing thread pool
			shutdownAndAwaitTermination(60, 60);
		}

		System.err.println("\nEND");
	}
}
