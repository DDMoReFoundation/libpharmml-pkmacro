package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.Assignable;
import eu.ddmore.libpharmml.dom.commontypes.Delay;
import eu.ddmore.libpharmml.dom.commontypes.Interpolation;
import eu.ddmore.libpharmml.dom.commontypes.Interval;
import eu.ddmore.libpharmml.dom.commontypes.Matrix;
import eu.ddmore.libpharmml.dom.commontypes.MatrixSelector;
import eu.ddmore.libpharmml.dom.commontypes.Product;
import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.Sequence;
import eu.ddmore.libpharmml.dom.commontypes.Sum;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.Vector;
import eu.ddmore.libpharmml.dom.commontypes.VectorSelector;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Constant;
import eu.ddmore.libpharmml.dom.maths.Equation;
import eu.ddmore.libpharmml.dom.maths.FunctionCallType;
import eu.ddmore.libpharmml.dom.maths.MatrixUniOp;
import eu.ddmore.libpharmml.dom.maths.Piecewise;
import eu.ddmore.libpharmml.dom.maths.Uniop;
import eu.ddmore.libpharmml.dom.modeldefn.IndividualParameter;
import eu.ddmore.libpharmml.dom.modeldefn.PopulationParameter;
import eu.ddmore.libpharmml.dom.modeldefn.Probability;
import eu.ddmore.libpharmml.dom.modeldefn.SimpleParameter;

@SuppressWarnings("deprecation")
class TransientParameter implements Assignable {
	
	private final String symbolId;
	private Rhs assign;
	
	private SimpleParameter sp;
	private PopulationParameter pp;
	private IndividualParameter ip;
	
	TransientParameter(String symbolId) {
		this.symbolId = symbolId;
	}
	
	TransientParameter(SimpleParameter sp){
		this.sp = sp;
		symbolId = sp.getId();
	}
	
	TransientParameter(PopulationParameter pp){
		this.pp = pp;
		symbolId = pp.getId();
	}
	
	TransientParameter(IndividualParameter ip){
		this.ip = ip;
		symbolId = ip.getId();
	}
	
	public Object getReference(){
		if(sp != null){
			return sp;
		} else if(pp != null) {
			return pp;
		} else if(ip != null) {
			return ip;
		} else {
			return null;
		}
	}
	
	boolean containsReference(){
		return (sp != null || pp != null || ip != null);
	}
	
	String getSymbolId(){
		return symbolId;
	}
	
	void setAssign(Rhs rhs){
		this.assign = rhs;
	}
	
	Rhs getAssign(){
		return assign;
	}
	
	SimpleParameter toSimpleParameter(){
		SimpleParameter sp = new SimpleParameter();
		sp.setSymbId(symbolId);
		sp.setAssign(assign);
		return sp;
	}
	
	PopulationParameter toPopulationParameter(){
		PopulationParameter pp = new PopulationParameter();
		pp.setSymbId(symbolId);
		pp.setAssign(assign);
		return pp;
	}
	
	@Override
	public Rhs assign(Scalar scalar) {
		Rhs rhs = new Rhs(scalar);
		setAssign(rhs);
		return rhs;
	}

	@Override
	@Deprecated
	public Rhs assign(Equation equation) {
		Rhs rhs = new Rhs(equation);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(SymbolRef symbolRef) {
		Rhs rhs = new Rhs(symbolRef);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Sequence sequence) {
		Rhs rhs = new Rhs(sequence);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Vector vector) {
		Rhs rhs = new Rhs(vector);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Interpolation interpolation) {
		Rhs rhs = new Rhs(interpolation);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Matrix matrix) {
		Rhs rhs = new Rhs(matrix);
		setAssign(rhs);
		return rhs;
	}
	
	@Override
	public Rhs assign(Constant constant) {
		Rhs rhs = new Rhs(constant);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Interval interval) {
		Rhs rhs = new Rhs(interval);
		setAssign(rhs);
		return rhs;
	}
	
	@Override
	public Rhs assign(Binop binop) {
		Rhs rhs = new Rhs(binop);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Uniop uniop) {
		Rhs rhs = new Rhs(uniop);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Piecewise piecewise) {
		Rhs rhs = new Rhs(piecewise);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(FunctionCallType functionCall) {
		Rhs rhs = new Rhs(functionCall);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Sum sum) {
		Rhs rhs = new Rhs(sum);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Product product) {
		Rhs rhs = new Rhs(product);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Delay delay) {
		Rhs rhs = new Rhs(delay);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(VectorSelector vectorSelector) {
		Rhs rhs = new Rhs(vectorSelector);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(MatrixSelector matrixSelector) {
		Rhs rhs = new Rhs(matrixSelector);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(MatrixUniOp matrixUniop) {
		Rhs rhs = new Rhs(matrixUniop);
		setAssign(rhs);
		return rhs;
	}

	@Override
	public Rhs assign(Probability probability) {
		Rhs rhs = new Rhs(probability);
		setAssign(rhs);
		return rhs;
	}
	

}
