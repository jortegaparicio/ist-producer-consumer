package es.urjc.ist.jms.concurrPtP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * <p> The RunConsumersTest class models a JMS pool of consumers that will run concurrently.
 * This method creates the JMS environment to run consumer tasks over the thread pool and pick up the execution results.
 * The implementation allow the consumers to receive messages of a common queue.
 * </p>
 * @authors Juan Antonio Ortega Aparicio & César Borao Moratinos
 * @version 1.0, 10/05/2021
 */
public class RunConsumers {

	// Parameter to select the number of consumers running concurrently in the thread pool
	private static final int NCONSUMERS = 10;

	// Pool of Queue connections
	private static final String FACTORY_NAME = "Factoria1"; 	

	// Ordered message Queue
	private static final String QUEUE_NAME = "Cola1"; 
	
	// Timeout values to end the thread pool
	private static final int FIRST_TIMEOUT = 60;
	private static final int SECOND_TIMEOUT = 60;
	
	// Create a new thread pool to run producers concurrently
	private static ExecutorService ConsPool = Executors.newFixedThreadPool(NCONSUMERS);

	// Initialize the result list
	private static List<Future<String>> ConsResultList = new ArrayList<Future<String>>(NCONSUMERS);
	
	
	/**
	 * Method to close the ExecutorService in two stages: first avoiding running new
	 * tasks in the pool and after, requesting the tasks running to finish.
	 * 
	 * @param firstTimeout Timeout to the first waiting stage.
	 * @param secondTimeout Timeout to the second waiting stage.
	 */
	private static void shutdownAndAwaitTermination(int firstTimeout, int secondTimeout) {
	
		try {
			ConsPool.shutdown(); 
			if (!ConsPool.awaitTermination(firstTimeout, TimeUnit.SECONDS)) {
				System.err.println("Uncompleted tasks. forcing closure...");
				ConsPool.shutdownNow(); 
				if (!ConsPool.awaitTermination(secondTimeout, TimeUnit.SECONDS))
					System.err.println("Unended thread pool");
			}
		} catch (InterruptedException ie) {
			ConsPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	
	/**
	 * Method to fill the result list with the threads' execution results.
	 * It uses the @see java.util.concurrent.Callable<V> to know the result of the 
	 * producers' execution.
	 */
	private static void recoverResults() {
		
		System.out.println("\nExecution Summary: ");
		for (Future<String> task : ConsResultList) {
			if(task.isDone()) {
				try {
					System.out.println("Result: " + task.get());
				} catch (InterruptedException | ExecutionException ex) {
					System.err.print("Thread interrupted or" +
							" execution failed");
					ex.printStackTrace();
				}
			} else {
				System.err.println("unfinished task");
			}
		} 
	}
	
	
	/**
	 * Main method to run the consumers program
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			
			// Recover initial context (JNDI)	
			InitialContext jndi = new InitialContext();
			
			// Reference to connection factory
			QueueConnectionFactory factory = 
					(QueueConnectionFactory)jndi.lookup(FACTORY_NAME);
			
			// Reference to message queue
			Queue queue = (Queue)jndi.lookup(QUEUE_NAME);

			// Running consumers over the thread pool
			for (int count = 0; count < NCONSUMERS; count++) {

				AsyncConsumer receiver = new AsyncConsumer(factory, queue);
				
				// We run a pool thread with the task and add the result to the result list
				ConsResultList.add(ConsPool.submit(receiver)); 
			}
			
		} catch (NamingException ex) {
			System.err.println("Consumer's Test finished with error: ");
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing thread pool
			shutdownAndAwaitTermination(FIRST_TIMEOUT, SECOND_TIMEOUT);

			// Recovering results
			recoverResults();
		}
		
		System.err.println("\nEND");
	}
}