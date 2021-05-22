package es.urjc.ist.activemq.concurrPubSub;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Objects;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * <p> The Subscriber class models a publisher in the Publisher/Subscriber pattern.</p>
 * 
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 16/05/2021
 */
public class AsyncSubscriber implements Runnable, ExceptionListener, MessageListener{
	
	
	private static final String TOPIC_NAME = "Topic"; 	  // Name of our Topic
	private static final String STOP = "CLOSE";			  // Stop message content	
	private static final int MILISLEEP = 1000;			  // Milliseconds to sleep thread
	
	private ActiveMQConnectionFactory connectionFactory;  // Factory that we use in the communication
	private boolean stopFlag;							  // Flag to end the connection
	
	
	/**
	 * Constructor method to Subscriber class
	 * @param connectionFactory
	 */
	public AsyncSubscriber(ActiveMQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		stopFlag = false;
	}
	
	/**
	 * Method to run a new subscriber. It uses the @see java.util.concurrent.Runnable to start the message receiving.
	 */
    public void run() {
    
    	System.out.println("Thread "+ Thread.currentThread().getId() + " subscribed!");
    	
        try {
        	
            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
            
            connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination
            Destination destination = session.createTopic(TOPIC_NAME);

            // Create a MessageConsumer from the Session to the Topic
            MessageConsumer consumer = session.createConsumer(destination);
           
            // consumer linked to a Message Listener to handle messages
            consumer.setMessageListener(this);

            // Wait for messages
            while(!stopFlag) {
            	Thread.sleep(MILISLEEP);
            }
           
            System.err.println("TRACE: Return Thread: " + Thread.currentThread().getId());
            
            // Closing consumer, session and connection
            consumer.close();
            session.close();
            connection.close();
            
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
    
    
    /**
	 * Overridden method to handle received messages (asynchronously)
	 * 
	 * @param msg the message to handle
	 */
    @Override
    public void onMessage(Message msg) {
    	try {
    		
    		TextMessage textMessage = (TextMessage) msg;
    		String text = textMessage.getText();
    		if (Objects.equals(text, STOP)) {
    			System.err.println("No more messages. Closing now listener running in thread: " + Thread.currentThread().getId());
    			stopFlag = true;
    		} else {
    			System.out.println("Listener, Thread " + Thread.currentThread().getId() + " message received: " + textMessage.getText());
    		}
    	} catch (JMSException ex){
    		ex.printStackTrace();
    	}
    }
    
    public synchronized void onException(JMSException ex) {
        System.err.println("JMS Exception occured.  Shutting down client.");
    }
}
