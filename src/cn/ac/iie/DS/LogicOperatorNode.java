package cn.ac.iie.DS;

enum LOGOPT {
	AND,
	OR,
	NOT,
	LEAF
}
public class LogicOperatorNode {
	public LOGOPT opt;
	public LogicOperatorNode leftOperand;
	public LogicOperatorNode ritghOperand;
//	public LogicOperand operand;
	public String str;
	
	public LogicOperatorNode() {
//		this.operand=null;
		this.opt = null;
		this.leftOperand = null;
		this.ritghOperand = null;
		this.str = null;
	}
}
