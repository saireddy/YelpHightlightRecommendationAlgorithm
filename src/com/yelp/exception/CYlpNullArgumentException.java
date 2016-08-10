package com.yelp.exception;

/**
 * Exception class thrown when one of the arguments to a method is null
 * @author bsaireddy
 *
 */
public class CYlpNullArgumentException extends Exception {
	
	//todo add more API based on requirements
	public CYlpNullArgumentException()
	{
		super();
	}

	public CYlpNullArgumentException(String message)
	{
		super(message);
	}

}
