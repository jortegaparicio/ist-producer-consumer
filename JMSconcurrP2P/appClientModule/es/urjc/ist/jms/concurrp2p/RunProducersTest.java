package es.urjc.ist.jms.concurrp2p;

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
 * <p> The RunProducersTest class models a JMS pool of producers that will run concurrently.
 * This method creates the JMS environment to run sender tasks over the thread pool and pick up the execution results.
 * The implementation allow the producers to send messages to the same queue.
 * <p>
 * @authors Juan Antonio Ortega Aparicio & César Borao Moratinos
 * @version 1.0, 10/05/2021
 * 
 */
public class RunProducersTest {

	// Parameter to select the number of producers running concurrently in the thread pool
	private static final int NPRODUCERS = 30;
	
	// Pool of Queue connections
	private static final String FACTORY_NAME = "Factoria1";

	// Ordered message Queue
	private static final String QUEUE_NAME = "Cola1"; 		

	// Create a new thread pool to run producers concurrently
	private static ExecutorService ProdPool = Executors.newFixedThreadPool(NPRODUCERS);

	// Initialize the result list
	private static List<Future<String>> ProdResultList = new ArrayList<Future<String>>(NPRODUCERS);

	
	/**
	 * Method to close the ExecutorService in two stages: first avoiding running new
	 * tasks in the pool and after, requesting the tasks running to finish.
	 * 
	 * @param firstTimeout Timeout to the first waiting stage.
	 * @param secondTimeout Timeout to the second waiting stage.
	 */
	private static void shutdownAndAwaitTermination(int firstTimeout, int secondTimeout) {
		ProdPool.shutdown(); 
		try {

			if (!ProdPool.awaitTermination(firstTimeout, TimeUnit.SECONDS)) {
				System.err.println("Uncompleted tasks. forcing closure...");
				ProdPool.shutdownNow(); 
				if (!ProdPool.awaitTermination(secondTimeout, TimeUnit.SECONDS))
					System.err.println("Unended thread pool");
			}
		} catch (InterruptedException ie) {
			ProdPool.shutdownNow();
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
		for (Future<String> task : ProdResultList) {
			if(task.isDone()) {
				try {
					System.out.println("Result: " + task.get());
				} catch (InterruptedException | ExecutionException ex) {
					System.out.print("Thread interrupted or" +
							" execution failed");
					ex.printStackTrace();
				}
			} else {
				System.out.println("unfinished task");
			}
		} 
	}

	
	/**
	 * Main method to run the producers program
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

			// Running producers over the thread pool
			for (int count = 0; count < NPRODUCERS; count++) {

				P2PSender sender = new P2PSender(factory, queue);

				// We run a pool thread with the task and add the running result to the result list
				ProdResultList.add(ProdPool.submit(sender)); 
			}
			
		} catch (NamingException e) {
			System.out.println("Producers's Test finished with error: ");
			e.printStackTrace();
		}
		
		// Closing thread pool
		shutdownAndAwaitTermination(60, 60);

		// Recovering results
		recoverResults();
		
		System.err.println("\nEND");
	}
}
