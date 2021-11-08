package com.test;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String CONNECTION_FACTORY 	= "openejb:Resource/jms/ConnectionFactory";
	private static final String ORIGINAL_QUEUE 		= "openejb:Resource/jms/oQueue";
	private static final String DEADLETTER_QUEUE 	= "openejb:Resource/jms/eQueue";
	
	private String _queueJndiName = DEADLETTER_QUEUE;
    private QueueConnection _connection;
    private Queue _queueDLQ;
    private Queue _queueOriginal;
    private QueueSession _session;
    private MessageProducer _producer;
    /**
     * Default constructor. 
     */
    public TestServlet() {
    }
    
    @Override
    public void init() throws ServletException {
    	try {
			InitialContext iniCtx = new InitialContext();
			
			Object tmp = iniCtx.lookup(CONNECTION_FACTORY);
            QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
            _connection = qcf.createQueueConnection();
            _queueDLQ = (Queue) iniCtx.lookup(_queueJndiName);
            _queueOriginal = (Queue) iniCtx.lookup(ORIGINAL_QUEUE);
            _connection.start();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				_connection.close();
			} catch (JMSException e1) {
				e1.printStackTrace();
			}
		}
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.err.println("javax.jms.ConnectionFactory=" + System.getProperty("Resource/javax.jms.ConnectionFactory"));
		QueueSession session = null;
		MessageConsumer consumer = null;
        StringBuilder data = new StringBuilder();
        try {
			session = _connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			consumer = session.createConsumer(_queueDLQ);
			
			while(true) {
				Message m = consumer.receive(10000);
				if(m != null) {
					System.out.println(m.toString());
					data.append(m.toString());
					retryMessage(m);
				} else {
					break;
				}
			}
			 //session.commit();
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
			try {
				session.close();
				consumer.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
        response.getWriter().append("Served data: ").append(data);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			_session = _connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			_producer = _session.createProducer(_queueOriginal);
			TextMessage tm = _session.createTextMessage(request.getParameter("text").replaceAll("[^A-Za-z0-9]", ""));
			tm.setStringProperty("ConnectionID", "ID1");
			_producer.send(tm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		try {
			_connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void retryMessage(Message message) throws JMSException {
		try {
		  TextMessage tm = _session.createTextMessage( ((TextMessage) message).getText());
	      tm.setStringProperty("ConnectionID", message.getStringProperty("ConnectionID"));
	       _producer.send(tm);
			message.acknowledge();
		} catch (JMSException e) {
			e.printStackTrace();
		}
}
}
