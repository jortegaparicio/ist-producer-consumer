package es.urjc.ist.jms.concurrp2p;

import java.util.concurrent.Callable;

import javax.jms.*;

/**
 * <h1>P2PSender class</h1>
 * 
 * <p> The P2PSender class models a JMS producer in the producer-consumer scheme.
 * All the implemented methods ensure concurrent access to send messages to the queue
 * <p>
 * @authors Juan Antonio Ortega Aparicio & César Borao Moratinos
 * @version 1.0, 10/05/2021
 */

public class P2PSender implements Callable<String> {		

	// Queue name
	private static final String queueName = "Cola1"; 

	// Common parameters to all the sender threads
	private QueueConnection connection;
	private Queue queue;

	/**
	 * Constructor with arguments. It requires the common parameters to all the sender threads.
	 * 
	 * @param connection
	 * @param queue
	 */
	public P2PSender(QueueConnection connection, Queue queue) {
		this.connection = connection;
		this.queue = queue;
	}

	@Override
	public String call() {

		try {
			// Create session and activate auto-commit
			QueueSession session = connection.createQueueSession(false,
					QueueSession.AUTO_ACKNOWLEDGE);	

			QueueSender sender = session.createSender(queue);

			// Creating and sending messages to the queue
			TextMessage msg = session.createTextMessage();
			for(int i = 0; i < 3; i++){

				msg.setText("Mensaje number " + i + " to " + "Cola1");
				sender.send(msg);
				System.err.println("Sending message " + i + " to " + queueName);
				Thread.sleep(1000);
			}
			System.err.println("Sending message to close connection...");

			// End message "CLOSE" must be handled to end the consumer that have received it
			msg.setText("CLOSE");
			sender.send(msg);

			// Closes the connection, the session and the receiver
			connection.close();  
			System.err.println("Closing sender...");
			return "SUCCESS: Sender in thread " + Thread.currentThread().getId();

		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} 
		return "FAIL: Sender in thread " + Thread.currentThread().getId();
	}
}
