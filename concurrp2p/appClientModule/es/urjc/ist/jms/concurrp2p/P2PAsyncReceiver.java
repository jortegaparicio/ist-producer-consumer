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

	private QueueConnection connection;
	private Queue queue;
	private boolean stopFlag;

	/**
	 * Constructor with arguments. It requires the common parameters to all the consumer threads.
	 * 
	 * @param connection
	 * @param queue
	 */
	public P2PAsyncReceiver(QueueConnection connection, Queue queue){
		this.connection = connection;
		this.queue = queue;
		stopFlag = false;
	}

	@Override
	public String call(){
		
		try {   
			// Create session and activate auto-commit
			QueueSession session = 
					connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			
			// Creating receiver and link it to a Message Listener (to handle messages asynchronously)
			QueueReceiver receiver = session.createReceiver(queue);
			receiver.setMessageListener(this);
			connection.start();
			System.out.println("Thread " + Thread.currentThread().getId() + " listening!");
			
			while (!stopFlag) {
				Thread.sleep(1000);
			}
			System.err.println("TRACE: Return Thread");
			return "SUCCESS: Consumer in thread " + Thread.currentThread().getId();
			
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		return "FAIL: Consumer in thread " + Thread.currentThread().getId();
	}

	/**
	 * Method override to handle received messages (asynchronously)
	 * 
	 * @param msg the message to handle
	 */
	@Override
	public void onMessage(Message msg) {
		try {
			TextMessage m = (TextMessage)msg;
			
			if (Objects.equals(m.getText(), "CLOSE")){
				System.out.println("No more messages. Closing now thead: " + Thread.currentThread().getId());
				
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
