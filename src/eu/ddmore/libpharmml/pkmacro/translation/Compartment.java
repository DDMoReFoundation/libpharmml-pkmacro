package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.CompartmentMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class Compartment extends AbstractCompartment {
	
	Compartment(String cmt, DerivativeVariable amount, Operand volume, Operand concentration) {
		super(cmt, amount, volume, concentration);
	}

	static Compartment fromMacro(Translator tr, CompartmentMacro macro) throws InvalidMacroException{
		ParamResolver resolver = new ParamResolver(macro);
		
		// Required parameters
		Rhs rhs_cmt = resolver.getValue("cmt");
		SymbolRef s = resolver.getValue("amount", SymbolRef.class);
		DerivativeVariable dv = resolveDerivativeVariable(tr, s);
		
		// Optionals
		Operand volume;
		if(resolver.contains("volume")){
			volume = resolver.getValue("volume",Operand.class);
		} else {
			volume = null;
		}
		Operand concentration;
		if(resolver.contains("concentration")){
			concentration = resolver.getValue("concentration",Operand.class);
		} else {
			concentration = null;
		}
		String cmt = rhs_cmt.getContent().toString();
		Compartment comp = new Compartment(cmt, dv, volume, concentration);
		tr.addCompartment(comp);
		return comp;
	}
	
}
