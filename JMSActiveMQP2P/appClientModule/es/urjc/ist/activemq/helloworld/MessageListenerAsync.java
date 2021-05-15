package es.urjc.ist.activemq.helloworld;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MessageListenerAsync implements MessageListener{
	
	
	 @Override
	    public void onMessage(Message message) {
	    	try {
	    		
	    		if (message instanceof TextMessage) {

	    			TextMessage textMessage = (TextMessage) message;
	    			String text = textMessage.getText();
	    			System.out.println("Received: " + text);
	    		} else {
	    			System.out.println("Received: " + message);
	    		}
	    		
//	    		TextMessage textMessage = (TextMessage) msg;
//	    		String text = textMessage.getText();
//	    		System.err.println(text);
//	    		if () {
//	    			System.err.print("No more messages. Closing now listener running in thread: " + Thread.currentThread().getId());
//	    			stopFlag = true;
//	    		} else {
//	    			System.out.println("Listener, Thread " + Thread.currentThread().getId() + " message received: " + textMessage.getText());
//	    		}
	    	} catch (JMSException ex){
	    		ex.printStackTrace();
	    	}
	    }
}
