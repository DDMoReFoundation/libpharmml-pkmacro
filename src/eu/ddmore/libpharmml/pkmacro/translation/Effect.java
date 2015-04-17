package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Equation;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.EffectMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class Effect extends AbstractCompartment {
	
	final protected Operand ke0;
	final protected AbstractCompartment target;
	
	private VariableDefinition concentration_equation;
	
	private Effect(AbstractCompartment target, DerivativeVariable amount, Operand ke0, VariableDefinition concentration) throws InvalidMacroException{
		super("toGenerate", amount, null, new SymbolRef(concentration.getSymbId()));
		this.ke0 = ke0;
		this.target = target;
		
		// C = Ac/V
		this.concentration_equation = concentration;
		Equation eq = new Equation();
		eq.setBinop(new Binop(Binoperator.DIVIDE, 
				new SymbolRef(target.getAmount().getSymbId()), 
				target.getVolume()));
		concentration_equation.assign(eq);
		
		// dCe/dt = ke0*(C - Ce)
		Equation eq2 = new Equation();
		Operand targetConcentration = target.getConcentration();
		if(targetConcentration == null){
			throw new InvalidMacroException("Target compartment (cmt="+target.getCmt()+
					") of Effect macro must have a defined concentration parameter.");
		}
		eq2.setBinop(new Binop(Binoperator.TIMES,
				ke0,
				new Binop(Binoperator.MINUS, targetConcentration, new SymbolRef(amount.getSymbId()))
				));
		amount.assign(eq2);
	}
	
	static Effect fromMacro(Translator tl, EffectMacro macro) throws InvalidMacroException{
		ParamResolver pr = new ParamResolver(macro);
		
		SymbolRef concentrationRef = pr.getValue("concentration", SymbolRef.class);
		DerivativeVariable concentration = tl.getVariableFactory().generateDerivativeVariable(concentrationRef.getSymbIdRef());
		
		String cmt = pr.getValue("cmt").getContent().toString();
		AbstractCompartment target = tl.getCompartment(cmt);
		
		Operand ke0 = pr.getValue(EffectMacro.Arg.KE0.toString(), Operand.class);
		
		SymbolRef c = pr.getValue(EffectMacro.Arg.CONCENTRATION.toString(), SymbolRef.class);
		VariableDefinition v = resolveVariable(tl, c);
		
		Effect effect = new Effect(target,concentration,ke0,v);
		tl.addCompartment(effect);
		
		return effect;
	}

	public Operand getKe0() {
		return ke0;
	}

	public Operand getConcentration() {
		return concentration;
	}
	
//	@Override
//	public List<AbstractEquation> getEquations() {
//		List<AbstractEquation> list = new ArrayList<AbstractEquation>();
//		list.add(ode);
//		list.add(concentration_equation);
//		return list;
//	}

}
