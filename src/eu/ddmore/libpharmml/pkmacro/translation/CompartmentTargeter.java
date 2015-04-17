package eu.ddmore.libpharmml.pkmacro.translation;

/**
 * Interface implemented by any macro that modifies an existing variable from a compartment.
 */
interface CompartmentTargeter {
	
	/**
	 * Method executed at the late stage of the translation in order to make sure that
	 * the targeted compartment has been already created before.
	 */
	void modifyTargetODE();
	
}
