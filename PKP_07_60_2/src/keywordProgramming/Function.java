package keywordProgramming;


/**
 * 1つのFunctionを表すクラス
 * @author sayuu
 *
 */
public class Function {

	//以下データベースの順番通り。
	private String parent_class;//0, ローカル変数の時や、Members of enclosing class の時は""
	private boolean isStatic;//1,
	private boolean isFinal;//2,
	private String type;//3, [field or constructor or method or localvariable]
	private String return_type;//4
	private String name;//5
	private String[] labels;//6
	private String[] parameters;//7以降, パラメータなしの時はnull
	private String DBString;
	
	/**
	 * コンストラクタ
	 * 引数は内部データベース(function.txt)の1行そのまま
	 * 
	 * @param function_string function.txtの1行そのまま
	 */
	public Function(String function_string){
		DBString = function_string;
		String s[] = function_string.split(",");
		parent_class = s[0];
		isStatic = s[1].equals("static");
		isFinal = s[2].equals("final");
		type = s[3];
		return_type = s[4];
		name = s[5];
		labels = s[6].split(";");

		if(s.length <= 7)//パラメータなし。
			return;
		parameters = new String[s.length - 7];
		System.arraycopy(s, 7, parameters, 0, s.length - 7);
	}

	public String toString(){
		String s = "Function[ret= " + return_type+", name= "+name;
		if(parameters != null){
			s += ", param= ";
			for(String p:parameters){
				s += "," + p;
			}
		}
		s += ", parent= " + parent_class + ", label= ";
		for(String l:labels){
			s += "," + l;
		}
		s += "]";
		return s;
	}
	
	public String toDBString(){
		return DBString;
	}
	
	public String toDBString2(){
		return 	"KeywordProgramming.functions.add(new Function(\""+ DBString +"\"));";
	}
	
	public String[] getLabels(){
		return labels;
	}

	public String getParentClass(){
		return parent_class;
	}

	public String getName(){
		return name;
	}

	public String getReturnType(){
		return return_type;
	}

	public String[] getParameters(){
		return parameters;
	}

	public boolean isStatic(){
		return isStatic;
	}

	public boolean isFinal(){
		return isFinal;
	}

	public String getFunctionType(){
		return type;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
	        return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Function o = (Function) obj;
		return this.DBString.equals(o.DBString);
	}
	
	@Override
	public int hashCode(){
		return this.DBString.hashCode();
	}
}
