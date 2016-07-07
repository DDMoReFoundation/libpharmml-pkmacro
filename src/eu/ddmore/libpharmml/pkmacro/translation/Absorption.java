/*******************************************************************************
 * Copyright (c) 2015-2016 European Molecular Biology Laboratory,
 * Heidelberg, Germany.
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of
 * the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations 
 * under the License.
 *******************************************************************************/
package eu.ddmore.libpharmml.pkmacro.translation;

import java.util.List;

import eu.ddmore.libpharmml.dom.MasterObjectFactory;
import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.RealValue;
import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Condition;
import eu.ddmore.libpharmml.dom.maths.ExpressionValue;
import eu.ddmore.libpharmml.dom.maths.LogicBinOp;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.maths.Otherwise;
import eu.ddmore.libpharmml.dom.maths.Piece;
import eu.ddmore.libpharmml.dom.maths.Piecewise;
import eu.ddmore.libpharmml.dom.maths.Uniop;
import eu.ddmore.libpharmml.dom.maths.Unioperator;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.AbsorptionOralMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;
import eu.ddmore.libpharmml.util.ChainedList;

/**
 * <p>Macro class for the translation of {@link AbsorptionOralMacro} objects.
 * 
 * <p>Different cases can occur depending on the arguments used in the input macros. In every case,
 * a new depot compartment Ad is created.
 * 
 * <p><h3>Case 1: zero order absorption</h3>
 * 
 * <pre><center><code>absorption/oral(adm=j, cmt=i, Tk0)</code></center>
 * - adds "- ZeroInputRate[i]" to the new ODE string
 * - adds "+ ZeroInputRate[i]" to the "i" target compartment ODE
 * with ZeroInputRate[i] defined by the following conditional statement as a new algebraic equation (AE):
 * if ( Ad[i]>0 ) { ZeroOrderRate[i] = LastDoseAmountToAd[i]/Tk0 } else { ZeroOrderRate[i]=0 }
 * </pre>
 * 
 * <p><h3>case 2: first order absorption with "ka" argument</h3>
 * 
 * <pre><center><code>oral(adm=a, cmt=i, ka)</code></center>
 * - adds "+ ka*cmtAmount[new "j"]" to the target compartment "i"
 * - adds "- ka*cmtAmount[new "j"]" to the current depot compartment ODE
 * </pre>
 * 
 * <p><h3>Case 3: transit compartments absorption with additional "Ktr" and "Mtt"</h3>
 * 
 * <pre><center><code>oral(adm=a, cmt=i, ka, Ktr, Mtt)</code></center>
 * - new absorption compartment, e.g. "Aa" 
 * - adds "+ ka*Aa" to the target compartment "i"
 * - adds the following new ODE "dAa/dt = exp[log(F*Dose)) + log(Ktr) + n*log(Ktr*(t-t_Dose)) - Ktr*(t-t_Dose) - log(n!)] - ka*Aa" 
 * - Note: in this case only the "Aa" compartment in newly created - the new "Ad" compartment as described earlier is redundant.
 * for simplicity (ii) assumes there is only one administration defined with "Ktr/Mtt", otherwise need more compartments and related ODs, i.e. Aa1 with "dAa1/dt=..."
 * - target "Dose"</pre>
 * 
 * <p><h2>Example:</h2>
 * 
 * <pre>
 * compartment(cmt=1, amount=Ac, concentration=Cc, volume=V)
 * oral(adm=1, cmt=1, Tk0)
 * 
 * gives:
 * 
 * dAc/dt= + ZeroOrderRate2
 * dAd2/dt= - ZeroOrderRate2
 * if (Ad2>0) {ZeroOrderRate2 = LastDoseAmountToAd2/Tk0} else {ZeroOrderRate2=0}
 * 
 * Input[1]: ORAL administration, adm=1, target=Ad2
 * 
 * </pre>
 * 
 * @author Florent Yvon
 */
class Absorption extends AbstractCompartment implements CompartmentTargeter, InputSource {

	protected final VariableFactory vf;
	protected final Scalar adm;
	protected final Operand Tlag;
	protected final Operand Tk0;
	protected final Operand ka;
	protected final Operand Ktr;
	protected final Operand Mtt;
	protected final Operand p;
	
	protected final AbstractCompartment target;
	
	protected final Type type;
	protected CommonVariableDefinition inputTarget;
	
	// Zero-order variables. Must be set if type == Type.ZERO_ORDER
	protected VariableDefinition zeroOrderRate = null;
	protected VariableDefinition lastDoseAmountToAd = null;
		
	/**
	 * Minimal contructor.
	 * @param adm
	 * @param tlag
	 * @param tk0
	 * @param ka
	 * @param ktr
	 * @param mtt
	 * @param p
	 * @param target The target compartment of this absorption.
	 * @param type
	 * @param cmt
	 * @param amount The variable of this compartment (Ad).
	 * @param vf The {@link VariableFactory} used in the current translation process.
	 */
	protected Absorption(Scalar adm, Operand tlag, Operand tk0, Operand ka, Operand ktr, Operand mtt, Operand p,
			AbstractCompartment target, Type type, Integer cmt, DerivativeVariable amount, VariableFactory vf) {
		super(cmt, amount, null, null);
		this.adm = adm;
		Tlag = tlag;
		Tk0 = tk0;
		this.ka = ka;
		Ktr = ktr;
		Mtt = mtt;
		this.target = target;
		this.type = type;
		this.p = p;
		this.vf = vf;
		
		if(type.equals(Type.TRANSIT)){
			generateTransitODE();
		} else if (type.equals(Type.FIRST_ORDER)) {
			generateFirstOrderODE();
		} else {
			generateZeroOrderODE();
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
		Operand p = null;
		if(pr.contains(AbsorptionOralMacro.Arg.P)){
			p = pr.getValue(AbsorptionOralMacro.Arg.P, Operand.class);
		}
		
		AbstractCompartment target = cf.getCompartment(
				pr.getValue("cmt",IntValue.class).getValue().intValue());
		
		Integer cmt = cf.lowestAvailableId();
		
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
		
		
		Absorption abs = new Absorption(adm, Tlag, Tk0, ka, Ktr, Mtt, p, target,
				type, cmt, amount, vf);
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
			Utils.addOperand(target.getAmount(),Binoperator.PLUS, new SymbolRef(zeroOrderRate.getSymbId()));
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
	
	protected void generateZeroOrderODE(){
		// Create variables
		zeroOrderRate = vf.createVariable("ZeroOrderRate", Integer.valueOf(getCmt()));
		lastDoseAmountToAd = vf.createVariable("LastDoseAmountToAd", Integer.valueOf(getCmt()));
		
		// dAd/dt = -ZeroInputRate
		amount.assign(new Uniop(Unioperator.MINUS, new SymbolRef(zeroOrderRate.getSymbId())));

		// if (Ad > 0) { ZeroOrderRate = LastDoseAmountToAd / Tk0 } else { ZeroOrderRate = 0 }
		Piecewise pw = new Piecewise();
		
		// Ad > 0
		Piece piece_Ad_gt_0 = new Piece();
		Condition condition_Ad_gt_0 = new Condition();
		LogicBinOp logic = new LogicBinOp();
		logic.setOp("gt");
		logic.getContent().add(MasterObjectFactory.COMMONTYPES_OF.createDerivativeVariable(amount));
		logic.getContent().add(MasterObjectFactory.COMMONTYPES_OF.createInt(new IntValue(0)));
		condition_Ad_gt_0.setLogicBinop(logic);
		piece_Ad_gt_0.setCondition(condition_Ad_gt_0);
		piece_Ad_gt_0.setValue(new Binop(
				Binoperator.DIVIDE, 
				new SymbolRef(lastDoseAmountToAd.getSymbId()), 
				Tk0));
	
		// Else
		Piece piece_else = new Piece();
		piece_else.setValue(new IntValue(0));
		Condition condition_else = new Condition();
		condition_else.setOtherwise(new Otherwise());
		piece_else.setCondition(condition_else);
		
		pw.getListOfPiece().add(piece_Ad_gt_0);
		pw.getListOfPiece().add(piece_else);
		
		zeroOrderRate.assign(pw);
		
		inputTarget = amount;
	}
	
	protected void generateFirstOrderODE(){
		Uniop uniop = new Uniop();
		uniop.setOperator(Unioperator.MINUS);
		Binop binop = new Binop(Binoperator.TIMES, ka, new SymbolRef(amount.getSymbId()));
		uniop.setValue(binop);
		amount.assign(uniop);
		inputTarget = amount;
	}
	
	protected void generateTransitODE(){		
		VariableDefinition dose = vf.generateVariable("Dose");
		SymbolRef doseRef = new SymbolRef(dose.getSymbId());
		VariableDefinition t_dose = vf.generateVariable("t_Dose");
		SymbolRef t_doseRef = new SymbolRef(t_dose.getSymbId());
		SymbolRef n = createN(Ktr, Mtt);
		Operand f;
		if(p != null){
			f = p;
		} else {
			f = vf.createAndReferNewParameter("F", ParameterType.INDIVIDUAL, new Rhs(new RealValue(1)));
		}
		SymbolRef t = vf.createTimeReference();
		
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
		amount.assign(rootBinop);
		inputTarget = dose;
	}
	
//	/**
//	 * Generates the parameter Ktr (transfer rate) with the assignment Ktr = n+1/Mtt.
//	 * @param n The number of transit compartment
//	 * @param Mtt The mean transit time between compartments.
//	 * @return A {@link SymbolRef} object to the generated parameter.
//	 */
//	private SymbolRef createKTR(Operand n,Operand Mtt){
//		Binop nPlus1 = new Binop(Binoperator.PLUS, n, new RealValue(1));
//		return vf.createAndReferNewParameter("Ktr", ParameterType.INDIVIDUAL, new Rhs(new Binop(Binoperator.DIVIDE, nPlus1, Mtt)));
//	}
	
	/**
	 * Creates the parameter "n" (number of transit compartments) with the assignment n = Ktr x Mtt - 1.
	 * @param ktr The transfer rate
	 * @param mtt The mean transit time
	 * @return A {@link SymbolRef} object to the generated parameter "n".
	 */
	private SymbolRef createN(Operand ktr,Operand mtt){
		Binop ktr_x_mtt = new Binop(Binoperator.TIMES, ktr, mtt);
		Binop ktr_x_mtt_min_1 = new Binop(Binoperator.MINUS, ktr_x_mtt, new RealValue(1));
		return vf.createAndReferNewParameter("n", ParameterType.INDIVIDUAL, new Rhs(ktr_x_mtt_min_1));
	}

	@Override
	public void generateInputs(InputList inputList) throws InvalidMacroException {
		inputList.createInput(InputType.ORAL, adm, inputTarget, Tlag, p);
	}

	@Override
	List<CommonVariableDefinition> getVariables() {
		return new ChainedList<CommonVariableDefinition>()
				.addIfNotNull(amount)
				.addIfNotNull(zeroOrderRate)
				.addIfNotNull(lastDoseAmountToAd);
	}
	
}
