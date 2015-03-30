package eu.ddmore.libpharmml.pkmacro.exceptions;

/**
 * Exception thrown during the translation process if a macro misses required
 * elements.
 * @author F. Yvon
 *
 */
public class InvalidMacroException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1263071353887741794L;
	
	private final String message;
	
	public InvalidMacroException(String message){
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

}
