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
 * <h1>RunConsumersTest class</h1>
 * 
 * <p> The RunConsumersTest class models a JMS pool of consumers that will run concurrently.
 * This method creates the JMS environment to run consumer tasks over the thread pool and pick up the execution results.
 * The implementation allow the consumers to receive messages of a common queue.
 * <p>
 * @authors Juan Antonio Ortega Aparicio & César Borao Moratinos
 * @version 1.0, 10/05/2021
 */
public class RunConsumersTest {

	// Parameter to select the number of consumers running concurrently in the thread pool
	private static final int NCONSUMERS = 2;
	
	// Create a new thread pool to run producers concurrently
	private static ExecutorService ConsPool = Executors.newFixedThreadPool(NCONSUMERS);

	// Pool of Queue connections
	private static final String factoryName = "Factoria1"; 	

	// Ordered message Queue
	private static final String queueName = "Cola1"; 

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
		ConsPool.shutdown(); 
		try {

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
	 * It uses the {@link java.util.concurrent.Callable<V>} to know the result of the 
	 * producers' execution.
	 */
	private static void recoverResults() {
		for (Future<String> task : ConsResultList) {
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
	
	public static void main(String[] args) {

		// Recover initial context (JNDI)	
		try {
			InitialContext jndi = new InitialContext();
			
			// Reference to connection factory
			QueueConnectionFactory factory = 
					(QueueConnectionFactory)jndi.lookup(factoryName);
			
			// Reference to message queue
			Queue queue = (Queue)jndi.lookup(queueName);

			// Running consumers over the thread pool
			for (int count = 0; count < NCONSUMERS; count++) {

				P2PAsyncReceiver receiver = new P2PAsyncReceiver(factory, queue);
				
				// We run a pool thread with the task and add the result to the result list
				ConsResultList.add(ConsPool.submit(receiver)); 
			}
			
		} catch (NamingException ex) {
			System.out.println("Consumer's Test finished with error: ");
			ex.printStackTrace();
		
		// Closing thread pool
		shutdownAndAwaitTermination(60, 60);

		// Recovering results
		recoverResults();
		}
	}
}