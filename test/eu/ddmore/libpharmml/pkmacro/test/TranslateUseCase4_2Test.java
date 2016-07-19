package eu.ddmore.libpharmml.pkmacro.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
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
import eu.ddmore.libpharmml.dom.commontypes.PharmMLElement;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.impl.PharmMLVersion;
import eu.ddmore.libpharmml.pkmacro.translation.Input;
import eu.ddmore.libpharmml.pkmacro.translation.InputType;
import eu.ddmore.libpharmml.pkmacro.translation.MacroOutput;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;

public class TranslateUseCase4_2Test {
	
	private ILibPharmML testInstance;
	private IPharmMLResource inputModel;
	private IndependentVariable time;
	private Translator translator;
	
	private static final String USECASE_4_2 = "examples/UseCase4_2.xml";
	
	@Before
	public void setUp() throws Exception {
		this.testInstance = PharmMlFactory.getInstance().createLibPharmML();
		this.inputModel = testInstance.createDomFromResource(new FileInputStream(USECASE_4_2));
		this.translator = new Translator();
	}
	
	@After
	public void tearDown() throws Exception {
		this.testInstance = null;
		this.inputModel = null;
		this.translator = null;
	}
	
	private StructuralModel fetchStructuralModel(IPharmMLResource res){
		PharmML dom = res.getDom();
		time = dom.getListOfIndependentVariable().get(0);
		ModelDefinition mdef = dom.getModelDefinition();
		StructuralModel sm = mdef.getListOfStructuralModel().get(0);
		return sm;
	}
	
	@Test
	public void testTranslate() throws Exception {
		StructuralModel sm = fetchStructuralModel(inputModel);
		MacroOutput output = translator.translate(sm, PharmMLVersion.DEFAULT,time);
		
		StructuralModel tl_sm = output.getStructuralModel();
		assertEquals("Number of variables", 3, tl_sm.getListOfStructuralModelElements().size());
		PharmMLElement var1 = tl_sm.getListOfStructuralModelElements().get(0);
		PharmMLElement var2 = tl_sm.getListOfStructuralModelElements().get(1);
		PharmMLElement var3 = tl_sm.getListOfStructuralModelElements().get(2);
		assertThat(var1, instanceOf(DerivativeVariable.class));
		DerivativeVariable d_ad = (DerivativeVariable) var1;
		assertThat(var2, instanceOf(VariableDefinition.class));
		VariableDefinition cc = (VariableDefinition) var2;
		assertThat(var3, instanceOf(DerivativeVariable.class));
		DerivativeVariable d_central = (DerivativeVariable) var3;
		
		assertEquals("Ad1", d_ad.getSymbId());
		assertNotNull(d_ad.getAssign());
		assertEquals("(-([pm]KA*Ad1))",d_ad.getAssign().toMathExpression());
		
		assertEquals("CC", cc.getSymbId());
		assertNotNull(cc.getAssign());
		assertEquals("([sm]CENTRAL/[pm]V)",cc.getAssign().toMathExpression());
		
		assertEquals("CENTRAL", d_central.getSymbId());
		assertNotNull(d_central.getAssign());
		assertEquals("((((-[pm]CL)*[sm]CENTRAL)/[pm]V)+([pm]KA*CENTRAL))",d_central.getAssign().toMathExpression());
		
		List<Input> inputs = output.getListOfInput();
		assertEquals("2 inputs",2,inputs.size());
		// Input[1]
		Input input1 = inputs.get(0);
		assertEquals(1, input1.getNumber());
		assertEquals(InputType.ORAL, input1.getType());
		assertEquals("1", input1.getAdm().valueToString());
		assertEquals("Ad1", input1.getTarget().getSymbId());
		// Input[2]
		Input input2 = inputs.get(1);
		assertEquals(2, input2.getNumber());
		assertEquals(InputType.IV, input2.getType());
		assertEquals("2", input2.getAdm().valueToString());
		assertEquals("CENTRAL", input2.getTarget().getSymbId());
	}

}
