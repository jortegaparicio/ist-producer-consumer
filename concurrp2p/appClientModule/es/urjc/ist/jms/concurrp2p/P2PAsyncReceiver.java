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
	private static final String STOP   = "CLOSE"; // Message to stop Consumer threads

	private QueueConnectionFactory factory;		  // Factory where we create the connections
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
	
	public P2PAsyncReceiver(QueueConnectionFactory factory, Queue queue){
		this.factory = factory;
		this.queue = queue;
		stopFlag = false;
		statusMsg = "WARNING, consumer thread closed without a 'CLOSE' message: ";
	}

//	
//	private synchronized void linkListener(QueueReceiver receiver) {
//		try {
//			receiver.setMessageListener(this);
//		} catch (JMSException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Overridden method from Callable that establish a new concurrent connection with the Payara Server.
	 * It represents the consumer concurrent method in producer/consumer pattern.
	 */
	@Override
	public String call(){
		
		try {  
			
			QueueConnection connection = factory.createQueueConnection();
			
			// Create session and activate auto-commit
			QueueSession session = 
					connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			
			// Creating receiver and link it to a Message Listener (to handle messages asynchronously)
			QueueReceiver receiver = session.createReceiver(queue);
			receiver.setMessageListener(this);
//			linkListener(receiver);
			
			
			// Start the connection and print in which Thread is the consumer listening
			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " listening!");
			
			// While we don't receive the STOP message the thread keep listening
			while (!stopFlag) {
				Thread.sleep(MILISLEEP);
			}
			
			// When we have received the "CLOSE" message, we close the connection and return the status message
			System.err.println("Closing receiver connection...");
			connection.close(); 
			statusMsg = "SUCCESS, consumer thread ended: ";
			System.err.println("TRACE: Return Thread " + Thread.currentThread().getId());
			
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
	public synchronized void onMessage(Message msg) {
		
		//synchronized
		try {
			TextMessage m = (TextMessage)msg;
			
			if (Objects.equals(m.getText(), STOP)){
				System.err.println("No more messages. Closing now listener running in thread: " + Thread.currentThread().getId());
			
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
