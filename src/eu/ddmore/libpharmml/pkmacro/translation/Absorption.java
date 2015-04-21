package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Equation;
import eu.ddmore.libpharmml.dom.maths.ExpressionValue;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.maths.Uniop;
import eu.ddmore.libpharmml.dom.maths.Unioperator;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.AbsorptionOralMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class Absorption extends AbstractCompartment implements CompartmentTargeter, InputSource {

	protected final Scalar adm;
	protected final Operand Tlag;
	protected final Operand Tk0;
	protected final Operand ka;
	protected final Operand Ktr;
	protected final Operand Mtt;
	
	protected final AbstractCompartment target;
	
	protected final Type type;
	protected CommonVariableDefinition inputTarget;
	
	// Zero-order variables. Must be set if type == Type.ZERO_ORDER
	protected VariableDefinition zeroOrderRate = null;
	protected VariableDefinition lastDoseAmountToAd = null;
		
	protected Absorption(Scalar adm, Operand tlag, Operand tk0, Operand ka, Operand ktr, Operand mtt, 
			AbstractCompartment target, Type type, String cmt, DerivativeVariable amount, VariableFactory vf) {
		super(cmt, amount, null, null);
		this.adm = adm;
		Tlag = tlag;
		Tk0 = tk0;
		this.ka = ka;
		Ktr = ktr;
		Mtt = mtt;
		this.target = target;
		this.type = type;
		
		if(type.equals(Type.TRANSIT)){
			generateTransitODE(vf);
		} else if (type.equals(Type.FIRST_ORDER)) {
			generateFirstOrderODE(vf);
		} else {
			generateZeroOrderODE(vf);
		}
	}

	static Absorption fromMacro(CompartmentFactory cf, VariableFactory vf, AbsorptionOralMacro macro) throws InvalidMacroException{
		ParamResolver pr = new ParamResolver(macro);
		
		Scalar adm;
		if(pr.contains(AbsorptionOralMacro.Arg.TYPE)){
			adm = pr.getValue(AbsorptionOralMacro.Arg.TYPE, Scalar.class);
		} else {
			adm = pr.getValue(AbsorptionOralMacro.Arg.ADM, Scalar.class);
		}
		
		Operand Tlag = null;
		if(pr.contains(AbsorptionOralMacro.Arg.TLAG)){
			Tlag = pr.getValue(AbsorptionOralMacro.Arg.TLAG, Operand.class);
		}
		Operand Tk0 = null;
		if(pr.contains(AbsorptionOralMacro.Arg.TK0)){
			Tk0 = pr.getValue(AbsorptionOralMacro.Arg.TK0, Operand.class);
		}
		Operand ka = null;
		if(pr.contains(AbsorptionOralMacro.Arg.KA)){
			ka = pr.getValue(AbsorptionOralMacro.Arg.KA, Operand.class);
		}
		Operand Ktr = null;
		if(pr.contains(AbsorptionOralMacro.Arg.KTR)){
			Ktr = pr.getValue(AbsorptionOralMacro.Arg.KTR, Operand.class);
		}
		Operand Mtt = null;
		if(pr.contains(AbsorptionOralMacro.Arg.MTT)){
			Mtt = pr.getValue(AbsorptionOralMacro.Arg.MTT, Operand.class);
		}
		
		AbstractCompartment target = cf.getCompartment(
				pr.getValue(AbsorptionOralMacro.Arg.CMT).getContent().toString());
		
		Integer cmt = cf.compartmentsSize()+1;
		
		Type type = null;
		DerivativeVariable amount;
		if(Tk0 != null){
			type = Type.ZERO_ORDER;
			amount = vf.createDerivativeVariable(VariableFactory.DEPOT_PREFIX, cmt);
		} else if(ka != null && (Ktr == null || Mtt == null)){
			type = Type.FIRST_ORDER;
			amount = vf.createDerivativeVariable(VariableFactory.DEPOT_PREFIX, cmt);
		} else if(Ktr != null && Mtt != null){
			type = Type.TRANSIT;
			amount = vf.createDerivativeVariable(VariableFactory.ABSORPTION_PREFIX, cmt);
		} else {
			throw new InvalidMacroException("Absorption/Oral macro must have the following prameters: "
					+ "Tk0, ka or [Ktr and Mtt]");
		}
		
		
		Absorption abs = new Absorption(adm, Tlag, Tk0, ka, Ktr, Mtt, target,
				type, cmt.toString(), amount, vf);
		cf.addCompartment(abs);
		return abs;
	}
	
	enum Type {
		ZERO_ORDER,
		FIRST_ORDER,
		TRANSIT
	}
	

	@Override
	public void modifyTargetODE() {
		switch (type) {
		case ZERO_ORDER:
			Utils.addOperand(target.getAmount(),Binoperator.PLUS, Tk0);
			break;
		case FIRST_ORDER:
			Binop binop = new Binop(Binoperator.TIMES, ka, new SymbolRef(amount.getSymbId()));
			Utils.addOperand(target.getAmount(),Binoperator.PLUS, binop);
			break;
		default: // transit
			SymbolRef absorptionComp = new SymbolRef(amount.getSymbId());
			Utils.addOperand(target.getAmount(),Binoperator.PLUS, new Binop(Binoperator.TIMES, ka, absorptionComp));
			break;
		}
	}
	
	protected void generateZeroOrderODE(VariableFactory varFac){
		// Create variables
		
		
		Equation eq = new Equation();
		Uniop uniop = new Uniop(Unioperator.MINUS, (ExpressionValue) Tk0);
		eq.setUniop(uniop);
		amount.assign(eq);
		inputTarget = amount;
	}
	
	protected void generateFirstOrderODE(VariableFactory varFac){
		Equation eq = new Equation();
		Uniop uniop = new Uniop();
		uniop.setOperator(Unioperator.MINUS);
		Binop binop = new Binop(Binoperator.TIMES, ka, new SymbolRef(amount.getSymbId()));
		uniop.setValue(binop);
		eq.setUniop(uniop);
		amount.assign(eq);
		inputTarget = amount;
	}
	
	protected void generateTransitODE(VariableFactory varFac){		
		VariableDefinition dose = varFac.generateVariable("Dose");
		SymbolRef doseRef = new SymbolRef(dose.getSymbId());
		VariableDefinition t_dose = varFac.generateVariable("t_Dose");
		SymbolRef t_doseRef = new SymbolRef(t_dose.getSymbId());
		SymbolRef n = varFac.createAndReferNewParameter("n");
		SymbolRef f = varFac.createAndReferNewParameter("F");
		SymbolRef t = new SymbolRef("t"); // must be defined as IndependentVariable
		
		// log(F*Dose)
		Uniop logFDose = new Uniop();
		logFDose.setOperator(Unioperator.LOG);
		logFDose.setValue(new Binop(Binoperator.TIMES, f, doseRef));
		
		// log(Ktr)
		Uniop logKtr = new Uniop();
		logKtr.setOperator(Unioperator.LOG);
		if(Ktr instanceof ExpressionValue){
			logKtr.setValue((ExpressionValue) Ktr);
		}
		
		// n*log(Ktr*(t-t_Dose))
		Binop nlogKtrttDose = new Binop(
				Binoperator.TIMES, 
				n, 
				new Uniop(
						Unioperator.LOG,
						new Binop(
								Binoperator.TIMES,
								Ktr,
								new Binop(Binoperator.MINUS, t, t_doseRef)
								)
				)
				);
		
		// - Ktr*(t-t_Dose)
		Binop ktrttDose = new Binop(
				Binoperator.TIMES, 
				Ktr, 
				new Binop(Binoperator.MINUS, t, t_doseRef));
		
		// - log(n!)
		Uniop logn = new Uniop();
		logn.setOperator(Unioperator.LOG);
		Uniop fact_n = new Uniop();
		fact_n.setOperator(Unioperator.FACTORIAL);
		fact_n.setValue(n);
		logn.setValue(fact_n);
		
		// - ka*Aa
		Binop kaAa = new Binop(Binoperator.TIMES, ka, new SymbolRef(amount.getSymbId()));
		
		// final equation
		Binop rootBinop = new Binop(
				Binoperator.MINUS, 
				new Uniop(
						Unioperator.EXP, 
						new Binop(
								Binoperator.MINUS, 
								new Binop(
										Binoperator.MINUS, 
										new Binop(
												Binoperator.PLUS, 
												new Binop(
														Binoperator.PLUS, 
														logFDose, 
														logKtr), 
												nlogKtrttDose), 
										ktrttDose), 
								logn)), 
				kaAa);
		Equation rootEq = new Equation();
		rootEq.setBinop(rootBinop);
		amount.assign(rootEq);
		inputTarget = dose;
	}

	@Override
	public void generateInputs(InputList inputList) throws InvalidMacroException {
		inputList.createInput(InputType.ORAL, adm, inputTarget);
	}
	
}
