package es.urjc.ist.jms.concurrPtP;

import java.util.concurrent.Callable;
import java.util.Objects;
import javax.jms.*;

/**
 * 
 * <p> The P2PAsyncReceiver class models a JMS asynchronous receiver (consumer) in the producer-consumer scheme.
 * All the implemented methods ensure concurrent access to receive queue messages asynchronously.
 * </p>
 * @authors Juan Antonio Ortega Aparicio & César Borao Moratinos
 * @version 2.0, 17/05/2021
 */
public class AsyncConsumer implements Callable<String>, MessageListener{

	private static final int MILISLEEP = 1000;    // ms sleeping time
	private static final String STOP   = "CLOSE"; // Message to stop Consumer threads

	private QueueConnectionFactory factory;		  // Factory where we create the connections (with Payara)
	private Queue queue;						  // Queue where we receive messages
	private boolean stopFlag;					  // Flag that stops the Threads
	
	private String statusMsg;					  // To return the status message to the pool executor
	
	
	
	/**
	 * Constructor method of P2PAsyncReceiver class with parameters.
	 * It requires the common parameters to all the consumer threads.
	 * 
	 * @param factory the factory where we want to open a new connection
	 * @param queue the queue with the messages
	 */
	public AsyncConsumer(QueueConnectionFactory factory, Queue queue){
		this.factory = factory;
		this.queue = queue;
		stopFlag = false;
		
		// Default status message: if run successfully, this message changes to "SUCCESS"
		statusMsg = "WARNING, consumer thread closed without a 'CLOSE' message: ";
	}


	/**
	 * Overridden method from Callable that establish a new concurrent connection with the Payara Server.
	 * It represents the consumer method in producer/consumer pattern.
	 */
	@Override
	public String call(){

		try {  

			// Create a new connection to the factory
			QueueConnection connection = factory.createQueueConnection();

			// Create session and activate auto-ack
			QueueSession session = 
					connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

			// Creating receiver and link it to a Message Listener (to handle messages asynchronously)
			QueueReceiver receiver = session.createReceiver(queue);
			receiver.setMessageListener(this);

			// Start the connection and print in which Thread is the consumer listening
			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " listening!");

			// While we don't receive the STOP message the thread keep listening
			while (!stopFlag) {
				Thread.sleep(MILISLEEP);
			}

			// When we have received the "CLOSE" message, we close the connection and set the status message
			System.err.println("Closing receiver connection...");
			connection.close(); 

			System.err.println("TRACE: Return Thread " + Thread.currentThread().getId());
			statusMsg = "SUCCESS, consumer thread ended: ";

		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		
		return statusMsg + Thread.currentThread().getId();
	}


	/**
	 * Overridden method to handle received messages (asynchronously)
	 * 
	 * @param msg the message to handle
	 */
	@Override
	public void onMessage(Message msg) {

		try {
			TextMessage m = (TextMessage)msg;

			if (Objects.equals(m.getText(), STOP)){
				System.err.println("No more messages. Closing now listener running in thread: " + Thread.currentThread().getId());

				stopFlag = true; 
				
				// To avoid receive other "CLOSE" messages before closing the consumer connection
				Thread.sleep(MILISLEEP); //MILISLEEP

			} else {
				System.out.println("Listener, Thread " + 
						Thread.currentThread().getId() +
						" message received: " + m.getText());
			}

		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
