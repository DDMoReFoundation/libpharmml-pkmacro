package eu.ddmore.libpharmml.pkmacro.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.PharmMlFactory;
import eu.ddmore.libpharmml.dom.PharmML;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.PharmMLElement;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.impl.PharmMLVersion;
import eu.ddmore.libpharmml.pkmacro.translation.MacroOutput;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;

public class TranslateUseCase4_2Test {
	
	private ILibPharmML testInstance;
	private IPharmMLResource inputModel;
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
		ModelDefinition mdef = dom.getModelDefinition();
		StructuralModel sm = mdef.getListOfStructuralModel().get(0);
		return sm;
	}
	
	@Test
	public void testTranslate() throws Exception {
		StructuralModel sm = fetchStructuralModel(inputModel);
		MacroOutput output = translator.translate(sm, PharmMLVersion.DEFAULT);
		
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
		assertEquals("(((-[pm]CL)*[sm]CENTRAL)/[pm]V)",d_central.getAssign().toMathExpression());
	}

}
