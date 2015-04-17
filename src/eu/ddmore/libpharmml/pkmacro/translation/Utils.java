package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Equation;
import eu.ddmore.libpharmml.dom.maths.ExpressionValue;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.maths.Uniop;
import eu.ddmore.libpharmml.dom.maths.Unioperator;

public class Utils {
	
	static void addOperand(DerivativeVariable var, Binoperator op, Operand operand){
		Rhs rhs = var.getAssign();
		if(rhs == null || rhs.getEquation() == null){
			rhs = var.assign(new Equation());
		}
		Equation eq = rhs.getEquation();
		Operand content = getContent(eq);
		if(content == null){
			if(op.equals(Binoperator.MINUS) && operand instanceof ExpressionValue){
				Uniop uniop = new Uniop(Unioperator.MINUS, (ExpressionValue) operand);
				eq.setUniop(uniop);
			} else if(op.equals(Binoperator.PLUS)) {
				if(operand instanceof SymbolRef){
					eq.setSymbRef((SymbolRef) operand);
				} else if(operand instanceof Binop){
					eq.setBinop((Binop) operand);
				} else if(operand instanceof Scalar){
					eq.setScalar(operand.toJAXBElement());
				} else if(operand instanceof Uniop){
					eq.setUniop((Uniop) operand);
				} else {
					throw new RuntimeException("Unsupported operation.");
				}
			}
		} else {
			Binop newBinop = new Binop(op, content, operand);
			rhs.getEquation().setBinop(newBinop);
		}
	}
	
	private static Operand getContent(Equation eq){
		Operand content;
		if(eq.getBinop() != null){
			content = eq.getBinop();
		} else if(eq.getUniop() != null){
			content = eq.getUniop();
		} else if(eq.getSymbRef() != null){
			content = eq.getSymbRef();
		} else if(eq.getScalar() != null && eq.getScalar().getValue() instanceof Scalar){
			content = (Scalar) eq.getScalar().getValue();
		} else {
			content = null;
		}
		return content;
	}
	
	public static String variableToString(DerivativeVariable dv){
		StringBuilder sb = new StringBuilder();
		sb.append(dv.getSymbId()+" = ");
		if(dv.getAssign().getEquation() != null){
			if(dv.getAssign().getEquation().getBinop() != null){
				sb.append(dv.getAssign().getEquation().getBinop());
			} else if (dv.getAssign().getEquation().getUniop() != null){
				sb.append(dv.getAssign().getEquation().getUniop());
			}
		}
		return sb.toString();
	}

}
