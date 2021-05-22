package es.urjc.ist.jms.concurrPtP;

import java.util.concurrent.Callable;
import javax.jms.*;

/**
 * <p> The P2PSender class models a JMS producer in the producer-consumer scheme.
 * All the implemented methods ensure concurrent access to send messages to the queue.
 * </p>
 * @authors Juan Antonio Ortega Aparicio & César Borao Moratinos
 * @version 1.0, 10/05/2021
 */
public class Producer implements Callable<String> {		

	private static final String QUEUE_NAME   = "Cola1";  // Queue name
	private static final int    NMESSAGE    = 3;         // Number of messages 
	private static final int    MILISLEEP   = 1000;      // ms sleeping time      

	private QueueConnectionFactory factory;				 // Factory where we create the connections
	private Queue queue;						  		 // Queue where we receive messages
	private String statusMsg;					  		 // To return the status message to the pool executor


	
	/**
	 * Constructor method of P2PSender class with parameters
	 * It requires the common parameters to all the sender threads.
	 * 
	 * @param factory the factory where we want to open a new connection
	 * @param queue the queue where we want to send the messages
	 */
	public Producer(QueueConnectionFactory factory, Queue queue) {
		this.factory = factory;
		this.queue = queue;
		statusMsg = "ERROR: Sender in thread: ";
	}

	
	/**
	 * Method that overrides call() method from @see java.util.concurrent.Callable<V>.
	 * It represents the producer method in producer/consumer pattern.
	 */
	@Override
	public String call() {

		try {
			// Create new connection for the producer thread
			QueueConnection connection = factory.createQueueConnection();

			// Create session and activate auto-ack
			QueueSession session = connection.createQueueSession(false,
					QueueSession.AUTO_ACKNOWLEDGE);	

			// Create new sender
			QueueSender sender = session.createSender(queue);

			// Creating and sending messages to the queue
			TextMessage msg = session.createTextMessage();
			for(int i = 0; i < NMESSAGE; i++){

				msg.setText("Message number " + i + " to " + "Cola1");
				sender.send(msg);
				System.out.println("Thread " + Thread.currentThread().getId() + 
						". Sending message " + i + " to " + QUEUE_NAME);

				Thread.sleep(MILISLEEP);
			}
			System.err.println("Sending message to close connection...");

			// End message "CLOSE" must be handled to end the consumer that have received it
			msg.setText("CLOSE");
			sender.send(msg);

			// Closes the connection, the session and the receiver and set the status message
			connection.close();  
			statusMsg = "SUCCESS, sender in thread: ";

		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} 

		System.err.println("Closing sender connection ...");
		return statusMsg + Thread.currentThread().getId();
	}
}
