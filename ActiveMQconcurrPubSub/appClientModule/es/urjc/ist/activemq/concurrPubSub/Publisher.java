package es.urjc.ist.activemq.concurrPubSub;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * 
 * <p> The Publisher class models a publisher in the Publisher/Subscriber pattern.</p>
 * @authors César Borao Moratinos & Juan Antonio Ortega Aparicio
 * @version 1.0, 16/05/2021
 */
public class Publisher implements Runnable {
	
	private static final String TOPIC_NAME = "Topic"; 	 // Name of our Topic
	private static final int NMESSAGES = 3; 			 // Number of messages to send to the topic
	private static final String STOP = "CLOSE";			 // Stop message content
	
	private ActiveMQConnectionFactory connectionFactory; // Factory that we use in the communication 
	
	
	/**
	 * Constructor method of Publisher class.
	 * 
	 * @param connectionFactory
	 */
	public Publisher(ActiveMQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	
	/**
	 * Overridden method from Runnable that starts a new publisher execution
	 * It represents the subscriber concurrent method in the publisher/subscriber pattern.
	 */
    public void run() {
 
        try {
            // Create a new connection
            Connection connection = connectionFactory.createConnection();
            
            // Starting new connection
            connection.start();

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the Topic
            Destination destination = session.createTopic(TOPIC_NAME);

            // Create a MessageProducer from the Session to the Topic
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            // Creating and publishing messages into the Topic
            for (int i = 0; i < NMESSAGES; i++) {
            	 // Create new message
                TextMessage message = session.createTextMessage("Message " + i + " to " + TOPIC_NAME);
                
                // Tell the producer to send the message
                producer.send(message);
                
                System.out.println("Thread: "+ Thread.currentThread().getId() + ". Sending message " + i + " to " + TOPIC_NAME);
            }

            // Sending "CLOSE" message to indicate the subscribers the end of the communication
            TextMessage message = session.createTextMessage(STOP);
            producer.send(message);
            
            // Closing the publisher, session and connection
            producer.close();
            session.close();
            connection.close();
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
}
