package com.test;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.DependsOn;
import javax.ejb.MessageDriven;
/**
 * Message-Driven Bean implementation class for: TestMDB
 */
/*@MessageDriven(mappedName = "jms/oQueue",
				messageListenerInterface = MessageListener.class
				,
				activationConfig = {
				    @ActivationConfigProperty(
				                    propertyName = "destinationType",
				                    propertyValue = "javax.jms.Queue"),
				    @ActivationConfigProperty(
				                    propertyName = "destination",
				                    propertyValue = "java:global/jms/test/TestEjb")

				     })*/
@MessageDriven
@DependsOn("")
public class TestMDB implements MessageListener {

	@Resource
    public MessageDrivenContext mdc;
	
	@PostConstruct
	private void init() {
		System.out.println("Initialized!!");
	}

    public void onMessage(Message message) {
        System.out.println(message.toString());
        if( message instanceof TextMessage) {
        	mdc.setRollbackOnly();
        }
    }

}
