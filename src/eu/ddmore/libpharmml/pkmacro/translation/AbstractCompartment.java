package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.impl.LoggerWrapper;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * Every new macro extending this class means adding a new {@link DerivativeVariable} definition
 * in the variable factory.
 */
abstract class AbstractCompartment extends AbstractMacro implements EquationSource {

	protected final String cmt; // transient for refering
	protected final DerivativeVariable amount;
	protected final Operand volume;
	protected final Operand concentration;
	
	AbstractCompartment(String cmt, DerivativeVariable amount, Operand volume, Operand concentration) {
		this.cmt = cmt;
		this.amount = amount;
		this.volume = volume;
		this.concentration = concentration;
	}

	public String getCmt() {
		return cmt;
	}

	public DerivativeVariable getAmount() {
		return amount;
	}

	public Operand getVolume() {
		return volume;
	}

	public Operand getConcentration() {
		return concentration;
	}

	protected static DerivativeVariable resolveDerivativeVariable(Translator tl, SymbolRef sref) throws InvalidMacroException{
		String symbId = sref.getSymbIdRef();
		VariableFactory vf = tl.getVariableFactory();
		DerivativeVariable dv;
		if(symbId == null){
			throw new InvalidMacroException("Missing symbIdRef attribute in SymbRef.");
		} else {
			dv = vf.fetchDerivativeVariable(symbId);
			if (dv == null){
				// Check if the variable already exists as a normal variable
				VariableDefinition var = vf.fetchVariable(symbId);
				if(var != null){
					LoggerWrapper.getLogger().info("Variable "+symbId+" transformed to DerivativeVariable.");
					dv = vf.transformToDerivativeVariable(var);
				} else {
					dv = vf.generateDerivativeVariable(symbId);
				}				
			}
		}
		return dv;
	}
	
	protected static VariableDefinition resolveVariable(Translator tl, SymbolRef sref) throws InvalidMacroException{
		String symbId = sref.getSymbIdRef();
		VariableFactory vf = tl.getVariableFactory();
		VariableDefinition v;
		if(symbId == null){
			throw new InvalidMacroException("Missing symbIdRef attribute in SymbRef.");
		} else {
			v = vf.fetchVariable(symbId);
			if (v == null){
//				throw new InvalidMacroException("Unresolved SymbRef.");
				v = vf.generateVariable(symbId);
			}
		}
		return v;
	}
		
}
