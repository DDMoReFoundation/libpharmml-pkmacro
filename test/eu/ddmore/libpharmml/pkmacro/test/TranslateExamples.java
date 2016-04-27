package eu.ddmore.libpharmml.pkmacro.test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.PharmMlFactory;
import eu.ddmore.libpharmml.dom.PharmML;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Uniop;
import eu.ddmore.libpharmml.dom.maths.Unioperator;
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.pkmacro.translation.Input;
import eu.ddmore.libpharmml.pkmacro.translation.InputType;
import eu.ddmore.libpharmml.pkmacro.translation.MacroOutput;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;
import eu.ddmore.libpharmml.pkmacro.translation.Utils;

public class TranslateExamples {
	
	private ILibPharmML testInstance;
	
	private static final String EXAMPLE1 = "examples/PKmacros_advan1.xml";
	private static final String EXAMPLE12 = "examples/PKmacros_example12.xml";
	private static final String EXAMPLE13 = "examples/PKmacros_example13.xml";
	
	@Before
	public void setUp() throws Exception {
		this.testInstance = PharmMlFactory.getInstance().createLibPharmML();
	}
	
	@After
	public void tearDown() throws Exception {
		this.testInstance = null;
	}
	
	private StructuralModel fetchStructuralModel(String fileName) throws FileNotFoundException{
		IPharmMLResource res = testInstance.createDomFromResource(
				new FileInputStream(fileName));

		PharmML dom = res.getDom();
		ModelDefinition mdef = dom.getModelDefinition();
		StructuralModel sm = mdef.getListOfStructuralModel().get(0);
		return sm;
	}
	
	@Test
	public void translateExample1() throws Exception {
		StructuralModel sm = fetchStructuralModel(EXAMPLE1);
		Translator tl = new Translator();
		MacroOutput mo = tl.translate(sm, sm.getUnmarshalVersion());
		
		// test ODE
		DerivativeVariable Ac = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(1);
		assertEquals("Ac", Ac.getSymbId());
		Uniop uniop = Ac.getAssign().getUniop();
		assertEquals(Unioperator.MINUS, uniop.getOperator());
		Binop binop = (Binop) uniop.getValue();
		assertEquals(Binoperator.TIMES, binop.getOperator());
		assertEquals("k", ((SymbolRef) binop.getOperand1()).getSymbIdRef());
		assertEquals("pm1", ((SymbolRef) binop.getOperand1()).getBlkIdRef());
		assertEquals("Ac", ((SymbolRef) binop.getOperand2()).getSymbIdRef());
		
		// test inputs
		Input input = mo.getListOfInput().get(0);
		assertInputEquals(input, InputType.IV, 1, "Ac");
	}
	
	@Test
	public void translateExample12() throws Exception {
		StructuralModel sm = fetchStructuralModel(EXAMPLE12);
		Translator tl = new Translator();
		MacroOutput mo = tl.translate(sm, sm.getUnmarshalVersion());
		
		// ODEs
		DerivativeVariable Ac = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(1);
		assertEquals("Ac", Ac.getSymbId());
		assertEquals("dAc/dt = [pm1]ka2 x Ad2 + [pm1]ka3 x Ad3 + [pm1]ka4 x Ad4 - [pm1]k x Ac", 
				Utils.variableToString(Ac));
		
		DerivativeVariable Ad2 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(2);
		assertEquals("Ad2", Ad2.getSymbId());
		assertEquals("dAd2/dt = - [pm1]ka2 x Ad2", 
				Utils.variableToString(Ad2));
		
		DerivativeVariable Ad3 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(3);
		assertEquals("Ad3", Ad3.getSymbId());
		assertEquals("dAd3/dt = - [pm1]ka3 x Ad3", 
				Utils.variableToString(Ad3));
		
		DerivativeVariable Ad4 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(4);
		assertEquals("Ad4", Ad4.getSymbId());
		assertEquals("dAd4/dt = - [pm1]ka4 x Ad4", 
				Utils.variableToString(Ad4));
		
		// Inputs
		assertInputEquals(mo.getListOfInput().get(0), InputType.IV, 1, Ac.getSymbId());
		assertInputEquals(mo.getListOfInput().get(1), InputType.ORAL, 2, Ad2.getSymbId());
		assertInputEquals(mo.getListOfInput().get(2), InputType.ORAL, 3, Ad3.getSymbId());
		assertInputEquals(mo.getListOfInput().get(3), InputType.ORAL, 4, Ad4.getSymbId());
	}
	
	@Test
	public void translateExample13() throws Exception {
		StructuralModel sm = fetchStructuralModel(EXAMPLE13);
		Translator tl = new Translator();
		tl.setParameter(Translator.KEEP_ORDER, false);
		MacroOutput mo = tl.translate(sm, sm.getUnmarshalVersion());
		
		// Es
		VariableDefinition C1 = (VariableDefinition) mo.getStructuralModel().getListOfStructuralModelElements().get(0);
		assertEquals("C1 = Ac1 / [pm1]V1", Utils.variableToString(C1));
		
		DerivativeVariable Ac1 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(3);
		assertEquals("dAc1/dt = - [pm1]k12 x Ac1 + [pm1]k21 x Ap1 + ka x Aa4 + ZeroOrderRate5 - [pm1]k x Ac1", 
				Utils.variableToString(Ac1));
		
		DerivativeVariable Ac3 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(4);
		assertEquals("dAc3/dt = - [pm1]Vm x Ac3 / [pm1]Km + Ac3", 
				Utils.variableToString(Ac3));
		
		DerivativeVariable Ap1 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(5);
		assertEquals("dAp1/dt = [pm1]k12 x Ac1 - [pm1]k21 x Ap1", 
				Utils.variableToString(Ap1));
		
		DerivativeVariable Aa4 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(6);
		assertEquals("dAa4/dt = EXP(LOG(F1 x Dose1) + LOG(Ktr) + n1 x LOG(Ktr x t - t_Dose1) - Ktr x t - t_Dose1 - LOG(FACTORIAL(n1))) - ka x Aa4", 
				Utils.variableToString(Aa4));
		
		DerivativeVariable Ad5 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(9);
		assertEquals("dAd5/dt = - ZeroOrderRate5", 
				Utils.variableToString(Ad5));
		
		VariableDefinition ZeroOrderRate5 = (VariableDefinition) mo.getStructuralModel().getListOfStructuralModelElements().get(10);
		assertEquals("ZeroOrderRate5 = if (Ad5 gt 0) { LastDoseAmountToAd5 / [pm1]Tk0 } else { 0 }", 
				Utils.variableToString(ZeroOrderRate5));
		
		DerivativeVariable Ce = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(12);
		assertEquals("dCe/dt = [pm1]ke0 x C1 - Ce", 
				Utils.variableToString(Ce));
		
		// Inputs
		assertInputEquals(mo.getListOfInput().get(0), InputType.ORAL, 1, "Dose1");
		assertInputEquals(mo.getListOfInput().get(1), InputType.ORAL, 3, Ad5.getSymbId());
		assertInputEquals(mo.getListOfInput().get(2), InputType.IV, 2, Ac3.getSymbId());
	}
	
	private void assertInputEquals(Input actual, InputType inputType, Integer adm, String target){
		assertEquals("Input type", inputType, actual.getType());
		assertEquals("adm", adm.intValue(), ((IntValue) actual.getAdm()).getValue().intValue());
		assertEquals("target", target, actual.getTarget().getSymbId());
	}

}
