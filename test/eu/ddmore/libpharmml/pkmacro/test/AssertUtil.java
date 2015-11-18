package eu.ddmore.libpharmml.pkmacro.test;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.util.Iterator;

import org.junit.Assert;

import eu.ddmore.libpharmml.IValidationError;
import eu.ddmore.libpharmml.IValidationReport;
import eu.ddmore.libpharmml.dom.commontypes.BooleanValue;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.RealValue;
import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.StringValue;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VectorValue;

public class AssertUtil extends Assert {
	
	public static void assertAssignValue(Boolean expected, Rhs actual){
		assertNotNull(actual);
		assertNotNull(actual.getScalar());
		Object value = actual.getScalar();
		assertThat(value, instanceOf(BooleanValue.class));
		assertEquals(expected.booleanValue(), ((BooleanValue)value).booleanValue());
	}
	
	public static void assertAssignValue(Integer expected, Rhs actual){
		assertNotNull(actual);
		assertNotNull(actual.getScalar());
		Object value = actual.getScalar();
		assertThat(value, instanceOf(IntValue.class));
		assertEquals(expected.intValue(), ((IntValue)value).getValue().intValue());
	}
	
	public static void assertAssignValue(Double expected, Rhs actual){
		assertNotNull(actual);
		assertNotNull(actual.getScalar());
		Object value = actual.getScalar();
		assertThat(value, instanceOf(RealValue.class));
		assertEquals(expected, (Double) ((RealValue)value).getValue());
	}
	
	public static void assertAssignValue(String expected, Rhs actual){
		assertNotNull(actual);
		assertNotNull(actual.getScalar());
		Object value = actual.getScalar();
		assertThat(value, instanceOf(StringValue.class));
		assertEquals(expected, ((StringValue)value).getValue());
	}
	
	public static void assertScalarEquals(Integer expected, IntValue actual){
		assertNotNull(actual);
		assertNotNull(actual.getValue());
		assertEquals(expected, Integer.valueOf(actual.getValue().intValue()));
	}
	
	public static void assertScalarEquals(String expected, StringValue actual){
		assertNotNull(actual);
		assertNotNull(actual.getValue());
		assertEquals(expected, actual.getValue());
	}
	
	public static void assertScalarEquals(Double expected, RealValue actual){
		assertNotNull(actual);
		assertNotNull(actual.getValue());
		assertEquals(expected, Double.valueOf(actual.getValue()));
	}
	
	public static void assertSymbRefEquals(SymbolRef expected, SymbolRef actual){
		assertNotNull(actual);
		assertEquals(expected.getBlkIdRef(), actual.getBlkIdRef());
		assertEquals(expected.getSymbIdRef(), actual.getSymbIdRef());
	}
	
	public static void assertVectorValueEquals(VectorValue expected, VectorValue actual){
		assertThat(actual, instanceOf(expected.getClass()));
		if(expected instanceof Scalar && actual instanceof Scalar){
			assertEquals(((Scalar) expected).valueToString(), ((Scalar) actual).valueToString());
		}
	}
	
	public static void assertValid(IValidationReport report){
		if(!report.isValid()){
			StringBuilder sb = new StringBuilder("Report contains "+report.numErrors()+" errors:");
			Iterator<IValidationError> it = report.errorIterator();
			while(it.hasNext()){
				sb.append("\n");
				sb.append(it.next());
			}
			fail(sb.toString());
		}
	}
	
	public static void assertInvalid(int expectedNumError, IValidationReport report){
		assertFalse("Invalid model", report.isValid());
		if(expectedNumError != report.numErrors()){
			Iterator<IValidationError> it = report.errorIterator();
			while(it.hasNext()){
				System.err.println(it.next());
			}
			System.err.println("");
		}
		assertEquals(expectedNumError, report.numErrors());
	}
	
}
