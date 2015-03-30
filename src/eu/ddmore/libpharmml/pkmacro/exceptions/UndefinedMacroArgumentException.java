package eu.ddmore.libpharmml.pkmacro.exceptions;

import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;

/**
 * Exception thrown if a required argument is missing in the given macro.
 * @author F. Yvon
 *
 */
public class UndefinedMacroArgumentException extends InvalidMacroException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2237245450228024793L;
	
	private final PKMacro macro;
	private final String argument;
	
	public UndefinedMacroArgumentException(PKMacro macro, String argument){
		super("Missing argument \"" + argument + "\" in macro \"" + macro.getName() + "\".");
		this.macro = macro;
		this.argument = argument;
	}
	
	/**
	 * Gets the incriminated macro at the origin if this exception.
	 * @return The invalid {@link PKMacro} object.
	 */
	public PKMacro getMacro(){
		return macro;
	}
	
	/**
	 * Gets the name of the missing argument.
	 * @return The argument name as {@link String}.
	 */
	public String getArgument(){
		return argument;
	}

}
