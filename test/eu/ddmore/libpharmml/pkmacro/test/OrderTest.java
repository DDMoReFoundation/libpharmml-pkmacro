package eu.ddmore.libpharmml.pkmacro.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;

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
import eu.ddmore.libpharmml.dom.modeldefn.ModelDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.AbsorptionMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.CompartmentMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacroList;
import eu.ddmore.libpharmml.impl.PharmMLVersion;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;

public class OrderTest {

	private ILibPharmML testInstance;
	private IPharmMLResource inputModel;
	private IndependentVariable time;
	private Translator translator;
	
	private static final String USECASE_3 = "examples/UseCase3.xml";
	
	@Before
	public void setUp() throws Exception {
		this.testInstance = PharmMlFactory.getInstance().createLibPharmML();
		this.inputModel = testInstance.createDomFromResource(new FileInputStream(USECASE_3));
		this.translator = new Translator();
	}
	
	@After
	public void tearDown() throws Exception {
		this.testInstance = null;
		this.inputModel = null;
		this.translator = null;
	}
	
	@Test
	public void testUseCase3() throws Exception{
		
		StructuralModel sm = fetchStructuralModel(inputModel);
		CompartmentMacro compartmentMacro = null;
		AbsorptionMacro absorptionMacro = null;
		for(PharmMLElement el : sm.getListOfStructuralModelElements()){
			if(el instanceof PKMacroList){
				for(PKMacro macro : ((PKMacroList) el).getListOfMacro()){
					if(macro instanceof CompartmentMacro){
						compartmentMacro = (CompartmentMacro) macro;
					}
					else if(macro instanceof AbsorptionMacro){
						absorptionMacro = (AbsorptionMacro) macro;
					}
				}
			}
		}
		
		StructuralModel tl_sm = translator.translate(sm, PharmMLVersion.DEFAULT, time).getStructuralModel();
		for(PharmMLElement el : tl_sm.getListOfStructuralModelElements()){
			if(el instanceof DerivativeVariable){
				DerivativeVariable dv = (DerivativeVariable) el;
				if(dv.getSymbId().equals("CENTRAL")){
					assertEquals(Integer.valueOf(3),dv.getOrder());
					assertTrue(dv.isOriginatedFromMacro());
					assertNotNull(dv.getOriginMacro());
					assertEquals(compartmentMacro, dv.getOriginMacro());
				}
				if(dv.getSymbId().equals("PCA")){
					assertEquals(Integer.valueOf(1),dv.getOrder());
					assertFalse(dv.isOriginatedFromMacro());
					assertNull(dv.getOriginMacro());
				}
				if(dv.getSymbId().equals("Ad1")){
					assertEquals(Integer.valueOf(2),dv.getOrder());
					assertTrue(dv.isOriginatedFromMacro());
					assertNotNull(dv.getOriginMacro());
					assertEquals(absorptionMacro, dv.getOriginMacro());
				}
			}
		}
		
	}
	
	private StructuralModel fetchStructuralModel(IPharmMLResource res){
		PharmML dom = res.getDom();
		time = dom.getListOfIndependentVariable().get(0);
		ModelDefinition mdef = dom.getModelDefinition();
		StructuralModel sm = mdef.getListOfStructuralModel().get(0);
		return sm;
	}
	
}
