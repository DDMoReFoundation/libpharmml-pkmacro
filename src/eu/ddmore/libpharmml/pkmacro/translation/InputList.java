package eu.ddmore.libpharmml.pkmacro.translation;

import java.util.ArrayList;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;

/**
 * List for storing {@link Input} objects and setting the inputNumber attribute automatically.
 */
class InputList extends ArrayList<Input> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6494052873392851577L;
	
	/**
	 * Add a new {@link Input} object to the list with the right inputNumber attribute value.
	 * @param type
	 * @param adm
	 * @param target
	 * @return The created and added {@link Input} object.
	 */
	Input createInput(InputType type, Scalar adm, DerivativeVariable target){
		Input input = new Input(size()+1, type, adm, target);
		add(input);
		return input;
	}

}
