package eu.ddmore.libpharmml.pkmacro.test;

import java.io.FileInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.IValidationReport;
import eu.ddmore.libpharmml.PharmMlFactory;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.impl.PharmMLVersion;
import eu.ddmore.libpharmml.pkmacro.translation.MacroOutput;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;

public class TranslateUseCase7 {
	
	private ILibPharmML testInstance;
	private IPharmMLResource inputModel;
	
	private static final String USECASE_10 = "examples/UseCase7.xml";
	
	@Before
	public void setUp() throws Exception {
		this.testInstance = PharmMlFactory.getInstance().createLibPharmML();
		this.inputModel = testInstance.createDomFromResource(new FileInputStream(USECASE_10));
	}
	
	@After
	public void tearDown() throws Exception {
		this.testInstance = null;
		this.inputModel = null;
	}
	
	@Test
	public void testKeepOrder() throws Exception {
		StructuralModel inputSM = inputModel.getDom().getModelDefinition().getListOfStructuralModel().get(0);
		Translator tl = new Translator();
		tl.setParameter(Translator.KEEP_ORDER, true);
		
		MacroOutput output = tl.translate(inputSM, PharmMLVersion.V0_7_3);
		inputModel.getDom().getModelDefinition().getListOfStructuralModel().set(0, output.getStructuralModel());
		inputModel.setParameter(IPharmMLResource.AUTOSET_ID, false);
		testInstance.save(System.out, inputModel);
		
		IValidationReport report = testInstance.getValidator().createValidationReport(inputModel);
		AssertUtil.assertValid(report);
	}

}
