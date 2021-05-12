package es.urjc.ist.jms.concurrp2p;

import java.util.concurrent.Callable;
import java.util.Objects;
import javax.jms.*;

/**
 * <h1>P2PAsyncReceiver class</h1>
 * 
 * <p> The P2PAsyncReceiver class models a JMS asynchronous receiver (consumer) in the producer-consumer scheme.
 * All the implemented methods ensure concurrent access to receive queue messages asynchronously.
 * <p>
 * @authors Juan Antonio Ortega Aparicio & César Borao Moratinos
 * @version 1.0, 10/05/2021
 */
public class P2PAsyncReceiver implements Callable<String>, MessageListener{

	private static final int MILISLEEP = 1000;    // ms sleeping time
	private static final String STOP   = "CLOSE"; // Message received to stop Consumer threads

	private QueueConnectionFactory factory;
	private Queue queue;		// Queue where we receive messages
	private boolean stopFlag;	// Flag that stops the Threads

	/**
	 * Constructor with arguments. It requires the common parameters to all the consumer threads.
	 * 
	 * @param queue
	 */
	public P2PAsyncReceiver(QueueConnectionFactory factory, Queue queue){
		this.factory = factory;
		this.queue = queue;
		stopFlag = false;
	}

	
	/**
	 * Overridden method from Callable that establish a new concurrent connection with the Payara Server.
	 * It represents the consumer concurrent method in the producer/consumer pattern.
	 */
	@Override
	public String call(){
		
		try {   
			// Create new connection for the consumer thread
			QueueConnection connection = factory.createQueueConnection();
			
			// Create session and activate auto-commit
			QueueSession session = 
					connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			
			// Creating receiver and link it to a Message Listener (to handle messages asynchronously)
			QueueReceiver receiver = session.createReceiver(queue);
			receiver.setMessageListener(this);
			
			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " listening!");
			
			while (!stopFlag) {
				Thread.sleep(MILISLEEP);
			}
			
			System.err.println("TRACE: Return Thread " + Thread.currentThread().getId());
			return "SUCCESS: Consumer in thread " + Thread.currentThread().getId() + " closed";
			
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		
		System.err.println("Closing individual sender...");
		return "WARNING: Consumer in thread " + Thread.currentThread().getId() + " not ended";
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
				System.out.println("No more messages. Closing now thread: " + Thread.currentThread().getId());
				
				//Enable condition to stop current Thread
				stopFlag = true; 
			} else {
				System.out.println("Listener, Thread " + 
						Thread.currentThread().getId() +
						" message received: " + m.getText());
			}
			
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}
}
