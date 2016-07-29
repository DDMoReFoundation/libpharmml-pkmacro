package eu.ddmore.libpharmml.pkmacro.test;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.PharmMlFactory;
import eu.ddmore.libpharmml.dom.IndependentVariable;
import eu.ddmore.libpharmml.dom.PharmML;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.PharmMLElement;
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

public class TranslateExamplesTest {
	
	private ILibPharmML testInstance;
	
	private static final String EXAMPLE1 = "examples/PKmacros_advan1.xml";
	private static final String EXAMPLE7 = "examples/PKmacros_advan12.xml";
	private static final String EXAMPLE12 = "examples/PKmacros_example12.xml";
	private static final String EXAMPLE13 = "examples/PKmacros_example13.xml";
	
	private IndependentVariable time;
	
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
		time = dom.getListOfIndependentVariable().get(0);
		ModelDefinition mdef = dom.getModelDefinition();
		StructuralModel sm = mdef.getListOfStructuralModel().get(0);
		return sm;
	}
	
	@Test
	public void translateExample1() throws Exception {
		StructuralModel sm = fetchStructuralModel(EXAMPLE1);
		Translator tl = new Translator();
		MacroOutput mo = tl.translate(sm, sm.getUnmarshalVersion(),time);
		
		// test ODE
		PharmMLElement var1 = mo.getStructuralModel().getListOfStructuralModelElements().get(1);
		assertThat("2nd variable is Derivative", var1, instanceOf(DerivativeVariable.class));
		DerivativeVariable Ac = (DerivativeVariable) var1;
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
		MacroOutput mo = tl.translate(sm, sm.getUnmarshalVersion(),time);
		
		// ODEs
		PharmMLElement var1 = mo.getStructuralModel().getListOfStructuralModelElements().get(1);
		assertThat(var1, instanceOf(DerivativeVariable.class));
		DerivativeVariable Ac = (DerivativeVariable) var1;
		assertEquals("Ac", Ac.getSymbId());
		assertEquals("[1] dAc/dt = [pm1]ka2 x Ad2 + [pm1]ka3 x Ad3 + [pm1]ka4 x Ad4 - [pm1]k x Ac", 
				Utils.variableToString(Ac));
		
		DerivativeVariable Ad2 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(2);
		assertEquals("Ad2", Ad2.getSymbId());
		assertEquals("[2] dAd2/dt = - [pm1]ka2 x Ad2", 
				Utils.variableToString(Ad2));
		
		DerivativeVariable Ad3 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(3);
		assertEquals("Ad3", Ad3.getSymbId());
		assertEquals("[3] dAd3/dt = - [pm1]ka3 x Ad3", 
				Utils.variableToString(Ad3));
		
		DerivativeVariable Ad4 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(4);
		assertEquals("Ad4", Ad4.getSymbId());
		assertEquals("[4] dAd4/dt = - [pm1]ka4 x Ad4", 
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
		MacroOutput mo = tl.translate(sm, sm.getUnmarshalVersion(),time);
		
		// Es
		VariableDefinition C1 = (VariableDefinition) mo.getStructuralModel().getListOfStructuralModelElements().get(0);
		assertEquals("C1 = Ac1 / [pm1]V1", Utils.variableToString(C1));
		
		DerivativeVariable Ac1 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(3);
		assertEquals("[1] dAc1/dt = - [pm1]k12 x Ac1 + [pm1]k21 x Ap1 + ka x Aa4 + ZeroOrderRate5 - [pm1]k x Ac1", 
				Utils.variableToString(Ac1));
		
		DerivativeVariable Ac3 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(4);
		assertEquals("[3] dAc3/dt = - [pm1]Vm x Ac3 / [pm1]Km + Ac3", 
				Utils.variableToString(Ac3));
		
		DerivativeVariable Ap1 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(5);
		assertEquals("[2] dAp1/dt = [pm1]k12 x Ac1 - [pm1]k21 x Ap1", 
				Utils.variableToString(Ap1));
		
		DerivativeVariable Aa4 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(6);
		assertEquals("[4] dAa4/dt = EXP(LOG(F1 x Dose1) + LOG(Ktr) + n1 x LOG(Ktr x t - t_Dose1) - Ktr x t - t_Dose1 - LOG(FACTORIAL(n1))) - ka x Aa4", 
				Utils.variableToString(Aa4));
		
		DerivativeVariable Ad5 = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(9);
		assertEquals("[5] dAd5/dt = - ZeroOrderRate5", 
				Utils.variableToString(Ad5));
		
		VariableDefinition ZeroOrderRate5 = (VariableDefinition) mo.getStructuralModel().getListOfStructuralModelElements().get(10);
		assertEquals("ZeroOrderRate5 = if (Ad5 gt 0) { LastDoseAmountToAd5 / [pm1]Tk0 } else { 0 }", 
				Utils.variableToString(ZeroOrderRate5));
		
		DerivativeVariable Ce = (DerivativeVariable) mo.getStructuralModel().getListOfStructuralModelElements().get(12);
		assertEquals("[6] dCe/dt = [pm1]ke0 x C1 - Ce", 
				Utils.variableToString(Ce));
		
		// Inputs
		assertInputEquals(mo.getListOfInput().get(0), InputType.ORAL, 1, "Dose1");
		assertInputEquals(mo.getListOfInput().get(1), InputType.ORAL, 3, Ad5.getSymbId());
		assertInputEquals(mo.getListOfInput().get(2), InputType.IV, 2, Ac3.getSymbId());
	}
	
	@Test
	public void translateExample7() throws Exception {
		StructuralModel sm = fetchStructuralModel(EXAMPLE7);
		Translator tl = new Translator();
		MacroOutput mo = tl.translate(sm, sm.getUnmarshalVersion(),time);
		
		List<PharmMLElement> sm_elements = mo.getStructuralModel().getListOfStructuralModelElements();
		assertEquals("Number of SM elements", 5, sm_elements.size());
		
		PharmMLElement var1 = sm_elements.get(0);
		assertThat(var1, instanceOf(VariableDefinition.class));
		VariableDefinition cc = (VariableDefinition) var1;
		assertEquals("Cc = Ac / [pm1]V", Utils.variableToString(cc));
		
		PharmMLElement var2 = sm_elements.get(1);
		assertThat(var2, instanceOf(DerivativeVariable.class));
		DerivativeVariable d_ac = (DerivativeVariable) var2;
		assertEquals("[1] dAc/dt = - [pm1]k12 x Ac + [pm1]k21 x Ap1 - [pm1]k13 x Ac + [pm1]k31 x Ap2 + [pm1]ka x Ad4 - [pm1]k x Ac", Utils.variableToString(d_ac));
		
		PharmMLElement var3 = sm_elements.get(2);
		assertThat(var3, instanceOf(DerivativeVariable.class));
		DerivativeVariable d_ap1 = (DerivativeVariable) var3;
		assertEquals("[2] dAp1/dt = [pm1]k12 x Ac - [pm1]k21 x Ap1", Utils.variableToString(d_ap1));
		
		PharmMLElement var4 = sm_elements.get(3);
		assertThat(var4, instanceOf(DerivativeVariable.class));
		DerivativeVariable d_ap2 = (DerivativeVariable) var4;
		assertEquals("[3] dAp2/dt = [pm1]k13 x Ac - [pm1]k31 x Ap2", Utils.variableToString(d_ap2));
		
		PharmMLElement var5 = sm_elements.get(4);
		assertThat(var5, instanceOf(DerivativeVariable.class));
		DerivativeVariable d_ad4 = (DerivativeVariable) var5;
		assertEquals("[4] dAd4/dt = - [pm1]ka x Ad4", Utils.variableToString(d_ad4));
	}
	
	private void assertInputEquals(Input actual, InputType inputType, Integer adm, String target){
		assertEquals("Input type", inputType, actual.getType());
		assertEquals("adm", adm.intValue(), ((IntValue) actual.getAdm()).getValue().intValue());
		assertEquals("target", target, actual.getTarget().getSymbId());
	}

}
