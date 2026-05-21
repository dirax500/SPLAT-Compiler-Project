package splat.executor;

import splat.executor.values.Value;

public class ReturnFromCall extends Exception {

	private Value returnVal;
	
	public ReturnFromCall(Value returnVal) {
		this.returnVal = returnVal;
	}
	
	public Value getReturnVal() {
		return returnVal;
	}
}
