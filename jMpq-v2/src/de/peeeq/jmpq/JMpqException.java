package de.peeeq.jmpq;

public class JMpqException extends Exception {
	private static final long serialVersionUID = 1L;

	public JMpqException(String msg){
		super(msg);
	}
	
	public JMpqException(){
		super();
	}
}
