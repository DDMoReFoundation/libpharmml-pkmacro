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
	
//	protected VariableDefinition targetConcentration;
	
	private Effect(AbstractCompartment target, Operand ke0, DerivativeVariable concentration, 
			VariableDefinition targetConcentration){
		super("toGenerate", null, null, new SymbolRef(concentration.getSymbId()));
		this.ke0 = ke0;
		this.target = target;
		
		// C = Ac/V
//		this.targetConcentration = targetConcentration;
		Equation eq = new Equation();
		eq.setBinop(new Binop(Binoperator.DIVIDE, 
				new SymbolRef(target.getAmount().getSymbId()), 
				target.getVolume()));
		targetConcentration.assign(eq);
		
		// dCe/dt = ke0*(C - Ce)
		Equation eq2 = new Equation();
		eq2.setBinop(new Binop(Binoperator.TIMES,
				ke0,
				new Binop(Binoperator.MINUS, new SymbolRef(targetConcentration.getSymbId()), new SymbolRef(concentration.getSymbId()))
				));
		concentration.assign(eq2);
	}
	
	static Effect fromMacro(Translator tl, EffectMacro macro) throws InvalidMacroException{
		ParamResolver pr = new ParamResolver(macro);
		
		SymbolRef concentrationRef = pr.getValue(EffectMacro.Arg.CONCENTRATION, SymbolRef.class);
//		DerivativeVariable concentration = tl.getVariableFactory().generateDerivativeVariable(
//				concentrationRef.getSymbIdRef());
		DerivativeVariable concentration = resolveDerivativeVariable(tl, concentrationRef);
		
		String cmt = pr.getValue("cmt").getContent().toString();
		AbstractCompartment target = tl.getCompartment(cmt);
		
		Operand ke0 = pr.getValue(EffectMacro.Arg.KE0.toString(), Operand.class);
		
		// fetch target concentration
		Operand targetConcentrationRef = target.getConcentration();
		if(targetConcentrationRef == null){
			throw new InvalidMacroException("Target compartment (cmt="+target.getCmt()+
					") of Effect macro must have a defined concentration parameter.");
		}
		if(!(targetConcentrationRef instanceof SymbolRef)){
			throw new InvalidMacroException("Concentration in compartment "+target.getCmt()+" must be a symbol reference.");
		}
		VariableDefinition targetConcentration = tl.getVariableFactory().fetchVariable(((SymbolRef)targetConcentrationRef).getSymbIdRef());
		if(targetConcentration == null){
			throw new InvalidMacroException("Symbol reference for concentration in compartment "+target.getCmt()+"is not resolved.");
		}
		
		Effect effect = new Effect(target,ke0,concentration,targetConcentration);
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
