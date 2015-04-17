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

	public int getNumber() {
		return number;
	}

	public InputType getType() {
		return type;
	}

	public Scalar getAdm() {
		return adm;
	}

	public DerivativeVariable getTarget() {
		return target;
	}
	
	@Override
	public String toString(){
		return "Input["+number+"] "+type+", adm="+adm+", target="+target;
	}

}
