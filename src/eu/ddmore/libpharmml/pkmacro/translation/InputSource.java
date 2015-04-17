package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

interface InputSource {
	
	void generateInputs(InputList inputList) throws InvalidMacroException;

}
