package eu.ddmore.libpharmml.pkmacro.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.SymbolType;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameter;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;

public class VariableFactory {
	
	public static String DEPOT_PREFIX = "Ad";
	public static String ABSORPTION_PREFIX = "Aa";
	public static String CENTRAL_CMT_PREFIX = "Ac";
	public static String PERIPH_CMT_PREFIX = "Ap";
		
	private final Map<String, AtomicInteger> variables_count;
	
	private final List<CommonVariableDefinition> variables;
	private final List<SimpleParameter> parameters;
	
	VariableFactory(StructuralModel sm){
		variables_count = new HashMap<String, AtomicInteger>();
		variables = new ArrayList<CommonVariableDefinition>();
		parameters = new ArrayList<SimpleParameter>();
		
		for(JAXBElement<? extends CommonVariableDefinition> v : sm.getCommonVariable()){
			storeVariable(v.getValue());
		}
		for(SimpleParameter p : sm.getSimpleParameter()){
			storeParameter(p);
		}
	}
	
	DerivativeVariable generateDerivativeVariable(String prefix){
		DerivativeVariable dv = new DerivativeVariable();
		dv.setSymbId(generateVariableName(prefix));
		dv.setSymbolType(SymbolType.REAL);
		variables.add(dv);
		return dv;
	}
	
	VariableDefinition generateVariable(String prefix){
		VariableDefinition v = new VariableDefinition();
		v.setSymbId(generateVariableName(prefix));
		v.setSymbolType(SymbolType.REAL);
		variables.add(v);
		return v;
	}
	
	SimpleParameter generateParameter(String prefix){
		SimpleParameter p = new SimpleParameter();
		p.setSymbId(generateVariableName(prefix));
		parameters.add(p);
		return p;
	}

	private String generateVariableName(String name){
		if(!variables_count.containsKey(name)){
			variables_count.put(name, new AtomicInteger(0));
		}
		return name + variables_count.get(name).incrementAndGet();
	}
	
	SymbolRef createAndReferNewParameter(String name){
		SimpleParameter param = generateParameter(name);
		SymbolRef symbRef = new SymbolRef(param.getSymbId());
		return symbRef;
	}
	
	void storeVariable(CommonVariableDefinition variable){
		VariableName varName = parseVariableName(variable.getSymbId());
		if(!variables_count.containsKey(varName.getPrefix())){
			variables_count.put(varName.getPrefix(), new AtomicInteger(0));
		}
		variables_count.get(varName.getPrefix()).incrementAndGet();
		
		variables.add(variable);
	}
	
	void storeParameter(SimpleParameter p){
		VariableName varName = parseVariableName(p.getSymbId());
		if(!variables_count.containsKey(varName.getPrefix())){
			variables_count.put(varName.getPrefix(), new AtomicInteger(0));
		}
		variables_count.get(varName.getPrefix()).incrementAndGet();
		
		parameters.add(p);
	}
	
	List<CommonVariableDefinition> getDefinedVariables(){
		return this.variables;
	}
	
	List<SimpleParameter> getDefinedParameters(){
		return parameters;
	}
	
	VariableName parseVariableName(String raw){
		Pattern p = Pattern.compile("(\\w+)(\\d*)");
		Matcher m = p.matcher(raw);
		if(m.find()){
			String prefix = m.group(1);
			String index = m.group(2);
			Integer intIndex;
			if(index != null && index.matches("\\d+")){
				intIndex = Integer.valueOf(index);
			} else {
				intIndex = 0;
			}
			return new VariableName(prefix, intIndex);
		} else {
			return null;
		}
	}
	
	DerivativeVariable fetchDerivativeVariable(String symbId){
		for(CommonVariableDefinition v : variables){
			if(v instanceof DerivativeVariable){
				if(v.getSymbId().equals(symbId)){
					return (DerivativeVariable) v;
				}
			}
		}
		return null;
	}
	
	VariableDefinition fetchVariable(String symbId){
		for(CommonVariableDefinition v : variables){
			if(v instanceof VariableDefinition){
				if(v.getSymbId().equals(symbId)){
					return (VariableDefinition) v;
				}
			}
		}
		return null;
	}
	
	DerivativeVariable transformToDerivativeVariable(VariableDefinition v){
		DerivativeVariable dv = new DerivativeVariable(v.getSymbId(), v.getSymbolType());
		variables.remove(v);
		variables.add(dv);
		return dv;
	}
	
	class VariableName {
		private String prefix;
		private Integer index;
		
		public VariableName(String prefix, Integer index) {
			this.prefix = prefix;
			this.index = index;
		}
		
		String getPrefix(){
			return prefix;
		}
		
		Integer getIndex(){
			return index;
		}
	}
}
