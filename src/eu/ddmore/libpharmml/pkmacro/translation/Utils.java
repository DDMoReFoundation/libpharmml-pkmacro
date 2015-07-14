/*******************************************************************************
 * Copyright (c) 2015 European Molecular Biology Laboratory,
 * Heidelberg, Germany.
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of
 * the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations 
 * under the License.
 *******************************************************************************/
package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Condition;
import eu.ddmore.libpharmml.dom.maths.Equation;
import eu.ddmore.libpharmml.dom.maths.ExpressionValue;
import eu.ddmore.libpharmml.dom.maths.LogicBinOp;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.maths.Piece;
import eu.ddmore.libpharmml.dom.maths.Piecewise;
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
		sb.append("d"+dv.getSymbId()+"/dt = ");
		if(dv.getAssign() != null && dv.getAssign().getEquation() != null){
			if(dv.getAssign().getEquation().getBinop() != null){
				sb.append(binopToString(dv.getAssign().getEquation().getBinop()));
			} else if (dv.getAssign().getEquation().getUniop() != null){
				sb.append(uniopToString(dv.getAssign().getEquation().getUniop()));
			}
		}
		return sb.toString();
	}
	
	public static String variableToString(VariableDefinition v){
		StringBuilder sb = new StringBuilder();
		sb.append(v.getSymbId()+" = ");
		if(v.getAssign() != null && v.getAssign().getEquation() != null){
			if(v.getAssign().getEquation().getBinop() != null){
				sb.append(binopToString(v.getAssign().getEquation().getBinop()));
			} else if (v.getAssign().getEquation().getUniop() != null){
				sb.append(uniopToString(v.getAssign().getEquation().getUniop()));
			} else if (v.getAssign().getEquation().getPiecewise() != null){
				sb.append(piecewiseToString(v.getAssign().getEquation().getPiecewise()));
			}
		}
		return sb.toString();
	}
	
	public static String binopToString(Binop binop){
		StringBuilder sb = new StringBuilder();
		Operand op1 = binop.getOperand1();
		Operand op2 = binop.getOperand2();
		
		String operand1String = operandToString(op1);
		String operand2String = operandToString(op2);
		Binoperator operator = binop.getOperator();
		
		String string;
		switch (operator) {
			case ATAN2:
				string = "atan2( "+operand1String+" , "+operand2String+" )";
				break;
			case DIVIDE:
				string = operand1String+" / "+operand2String;
				break;
			case LOGX:
				string = operand1String+"log "+operand2String;
				break;
			case MAX:
				string = "max{ "+operand1String+" , "+operand2String+" }";
				break;
			case MIN:
				string = "min{ "+operand1String+" , "+operand2String+" }";
				break;
			case MINUS:
				string = operand1String+" - "+operand2String;
				break;
			case PLUS:
				string = operand1String+" + "+operand2String;
				break;
			case POWER:
				string = operand1String+" ^ "+operand2String;
				break;
			case REM:
				string = operand1String+" % "+operand2String;
				break;
			case ROOT:
				string = operand2String+"root( "+operand1String+" )";
				break;
			case TIMES:
				string = operand1String+" x "+operand2String;
				break;
			default:
				string = "" + operand1String + operator + operand2String;
				break;
		}
		
		sb.append(string);
		
		return sb.toString();
	}
	
	public static String uniopToString(Uniop uniop){
		StringBuilder sb = new StringBuilder();
		
		ExpressionValue content = uniop.getValue();
		Unioperator operator = uniop.getOperator();
		
		switch (operator){
		case MINUS:
			sb.append("- "+expressionValueToString(content));
			break;
		default:
			sb.append(operator+"("+expressionValueToString(content)+")");
			break;
			
		}
		return sb.toString();
	}
	
	public static String operandToString(Operand op){
		StringBuilder sb = new StringBuilder();
		if(op instanceof SymbolRef){
			SymbolRef sref = (SymbolRef) op;
			if(sref.getBlkIdRef() != null){
				sb.append("["+sref.getBlkIdRef()+"]");
			}
			sb.append(sref.getSymbIdRef());
		} else if(op instanceof Scalar){
			sb.append(((Scalar) op).valueToString());
		} else if(op instanceof Binop){
			sb.append(binopToString((Binop) op));
		} else if(op instanceof Uniop){
			sb.append(uniopToString((Uniop) op));
		} else {
			sb.append("Unknown operand");
		}
		return sb.toString();
	}
	
	public static String expressionValueToString(ExpressionValue ev){
		if(ev instanceof Operand){
			return operandToString((Operand) ev);
		} else {
			return "ERROR";
		}
	}
	
	public static String objectToString(Object o){
		if(o instanceof CommonVariableDefinition){
			return ((CommonVariableDefinition) o).getSymbId();
		} else if (o instanceof Scalar){
			return ((Scalar) o).valueToString();
		} else {
			return String.valueOf(o);
		}
	}
	
	public static String piecewiseToString(Piecewise pw){
		StringBuilder sb = new StringBuilder();
		for(Piece p : pw.getPiece()){
			Condition condition = p.getCondition();
			if(condition.getOtherwise() == null){
				String conditionString;
				if(condition.getLogicBinop() != null){
					LogicBinOp lb = condition.getLogicBinop();
					conditionString = objectToString(lb.getContent().get(0).getValue())+" "+lb.getOp()+" "+objectToString(lb.getContent().get(1).getValue());
				} else {
					conditionString = null;
				}
				sb.append("if ("+conditionString+") { "+expressionValueToString(p.getValue())+" } else ");
			} else {
				sb.append("{ "+expressionValueToString(p.getValue())+" }");
			}
		}
		return sb.toString();
	}

}
