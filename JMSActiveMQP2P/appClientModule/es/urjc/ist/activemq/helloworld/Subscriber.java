package es.urjc.ist.activemq.helloworld;

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

public class Subscriber implements Runnable, ExceptionListener, MessageListener{
	
	//URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    // private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;  // "tcp://localhost:61616"
	private static final String TOPIC_NAME = "Topic"; 
	
	private ActiveMQConnectionFactory connectionFactory;
	
	private static final String STOP = "CLOSE";
	private static final int MILISLEEP = 1000;
	
	private boolean stopFlag;
	
	public Subscriber(ActiveMQConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		stopFlag = false;
	}
	
    public void run() {
    	
        try {
        	
        	System.out.println("Thread "+ Thread.currentThread().getId() + " subscribed!");
           
        	// Create a ConnectionFactory
        	// The following line creates a new ActiveMQConnectionFactory using an embedded broker
        	// inside de same JVM as the application we run. Note the property broker.persistent=false
            //ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
            
            // Alternatively, we can use the default URL for a connection with an external broker
            // in the same system or in another one, using TCP for network transport
            // ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
            
            connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            // Destination destination = session.createQueue("Test");
            Destination destination = session.createTopic(TOPIC_NAME);

            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(destination);
           
            consumer.setMessageListener(this);

            // Wait for messaages
            while(!stopFlag) {
            		
            	Thread.sleep(MILISLEEP);
//            	Message message = consumer.receive(3000);
//            
//            	if (message instanceof TextMessage) {
//            		TextMessage textMessage = (TextMessage) message;
//            		if (Objects.equals(textMessage.getText(), STOP)) {
//            			stopFlag = true;
//            			System.err.print("No more messages. Closing now listener running in thread:" + Thread.currentThread().getId() + "\n");
//            		} else {
//            			System.out.println("Listener, Thread " + Thread.currentThread().getId() + " message received: " + textMessage.getText());
//
//            		}  
//            	}
            }
           
            System.err.println("TRACE: Return Thread: " + Thread.currentThread().getId());
            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
    
   
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
        System.out.println("JMS Exception occured.  Shutting down client.");
    }
}
