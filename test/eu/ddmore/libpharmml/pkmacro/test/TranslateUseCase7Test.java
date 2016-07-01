package eu.ddmore.libpharmml.pkmacro.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.FileInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.ddmore.libpharmml.ILibPharmML;
import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.IValidationReport;
import eu.ddmore.libpharmml.PharmMlFactory;
import eu.ddmore.libpharmml.dom.IndependentVariable;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.impl.PharmMLVersion;
import eu.ddmore.libpharmml.pkmacro.translation.MacroOutput;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;

public class TranslateUseCase7Test {
	
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
		IndependentVariable time = inputModel.getDom().getListOfIndependentVariable().get(0);
		Translator tl = new Translator();
		tl.setParameter(Translator.KEEP_ORDER, true);
		
		MacroOutput output = tl.translate(inputSM, PharmMLVersion.DEFAULT,time);
		inputModel.getDom().getModelDefinition().getListOfStructuralModel().set(0, output.getStructuralModel());
		inputModel.setParameter(IPharmMLResource.AUTOSET_ID, false);
		testInstance.save(System.out, inputModel);
		
		IValidationReport report = testInstance.getValidator().createValidationReport(inputModel);
		AssertUtil.assertValid(report);
	}
	
	@Test
	public void testKeepBlkId() throws Exception {
		StructuralModel inputSM = inputModel.getDom().getModelDefinition().getListOfStructuralModel().get(0);
		IndependentVariable time = inputModel.getDom().getListOfIndependentVariable().get(0);
		Translator tl = new Translator();
		MacroOutput output = tl.translate(inputSM, PharmMLVersion.DEFAULT,time);
		assertEquals("Same blkId",inputSM.getBlkId(),output.getStructuralModel().getBlkId());
	}
	
	@Test
	public void testSetBlkIdValue() throws Exception {
		final String diffBlockId = "unitTestBlkId";
		StructuralModel inputSM = inputModel.getDom().getModelDefinition().getListOfStructuralModel().get(0);
		IndependentVariable time = inputModel.getDom().getListOfIndependentVariable().get(0);
		Translator tl = new Translator();
		tl.setParameter(Translator.KEEP_BLOCK_ID, false);
		tl.TRANSLATED_BLK_ID = diffBlockId;
		MacroOutput output = tl.translate(inputSM, PharmMLVersion.DEFAULT,time);
		assertNotEquals("Different blkId", inputSM.getBlkId(), output.getStructuralModel().getBlkId());
		assertEquals("BlkId = "+diffBlockId,diffBlockId,output.getStructuralModel().getBlkId());
	}

}
