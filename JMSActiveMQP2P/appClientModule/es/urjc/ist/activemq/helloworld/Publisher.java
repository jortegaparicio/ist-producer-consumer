package es.urjc.ist.activemq.helloworld;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Publisher implements Runnable {
	
	private static final String TOPIC_NAME = "Topic"; 
	private static final int NMESSAGES = 3; 
	
	private ActiveMQConnectionFactory connectionFactory;
	
	
	public Publisher(ActiveMQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
    public void run() {
    	
//    	System.out.println("Default broker URL is: " + url);
//    	System.out.println();
    	
        try {
            // Create a ConnectionFactory
        	// The following line creates a new ActiveMQConnectionFactory using an embedded broker
        	// inside the same JVM as the application we run. Note the property broker.persistent=false
        	// en este caso es un broker embebido y virtual
            // ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
            
            // Alternatively, we can use the default URL for a connection with an external broker
            // in the same system or in another one, using TCP for network transport
        	//     ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            
            // hay que hacer start en el consumer y en el producer -> en payara solo en el start
            connection.start();

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            // Destination destination = session.createQueue("Test");
            Destination destination = session.createTopic(TOPIC_NAME);

            // Create a MessageProducer from the Session to the Topic or Queue
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            for (int i = 0; i < NMESSAGES; i++) {
            	 // Create a messages
                TextMessage message = session.createTextMessage("Message " + i + " to " + TOPIC_NAME);
                
                // Tell the producer to send the message
                producer.send(message);
                
                System.out.println("Thread: "+ Thread.currentThread().getId() + ". Sending message " + i + " to " + TOPIC_NAME);
            }

            // Clean up
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
