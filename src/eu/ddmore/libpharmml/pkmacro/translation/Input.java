package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;

/**
 * Class defining a PK input.
 */
public class Input {
	
	private final int number;
	private final InputType type;
	private final Scalar adm;
	private final DerivativeVariable target;

	Input(int number, InputType type, Scalar adm, DerivativeVariable target) {
		this.number = number;
		this.type = type;
		this.adm = adm;
		this.target = target;
	}

	/**
	 * Gets the number of this input within the list it comes from.
	 * @return An integer starting from 1 to *.
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Gets the type of input, either IV or ORAL.
	 * @return An {@link InputType} item.
	 */
	public InputType getType() {
		return type;
	}

	/**
	 * Gets the type of administration.
	 * @return The type of administration as a {@link Scalar}.
	 */
	public Scalar getAdm() {
		return adm;
	}

	/**
	 * Gets the target variable of the input.
	 * @return The target as a {@link DerivativeVariable}.
	 */
	public DerivativeVariable getTarget() {
		return target;
	}
	
	/**
	 * Returns a string representation of this input with the following format:<br>
	 * Input[&lt;n>] &lt;Type>, adm=&lt;i>, target=&lt;variable>
	 */
	@Override
	public String toString(){
		return "Input["+number+"] "+type+", adm="+adm+", target="+target.getSymbId();
	}

}
