package plugin.testSite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import console_test.LevenshteinDistance;

import keywordProgramming.ExplanationVector;
import keywordProgramming.FunctionTree;
import keywordProgramming.KeywordProgramming;

/*
 * 1つのタスクを表すクラス。
 */
public class TestSite {

	public static final String TestSiteFolder = "./KeywordProgramming/testSite/";
	public static final String LogSiteFolder = "./KeywordProgramming/logSite/";

	private static List<TestSite> logFiles = new LinkedList<TestSite>();	//オンライン学習用のログファイル
	
	private String className;//クラス名（パッケージ名も）
	private int offset;
	private int original_offset;	//変更前。
    private int line_number_start;
    private int line_number_end;
    private int selected_length;
    //選択文字列
    private String selected_string;
    //選択文字列の順位
    private int selected_string_order;
    //望ましい返り値の型
  	private String desiredReturnType;
  	//入力キーワード
  	private String keywords;
  	//ロケーション
  	/*
  	 * Return文 の中
	 * If文　の中
	 * 宣言文
	 * 代入文
	 * 地の文
	 * メソッドの引数の中
	 * など。
  	 */
  	private String location;
  	
  	//重複を防ぐためにHashSetにした。
  	//現在エディタ内に存在する有効な関数を入れるリスト
  	private HashSet<String> classesInActiveEditor = new HashSet<String>();
  	//現在エディタ内に存在する有効な関数を入れるリスト
  	private HashSet<String> functionsInActiveEditor = new HashSet<String>();
  	
  	//1つのTextSiteに対応する1つのテキストファイル
  	private File txtFile;
  	
  	//選択された
  	private boolean isSelectedTask = false;
  	
  	//ログファイルか否か
  	private boolean isLogFile;
  	
  	//キーワードプログラミングの出力(バッチ学習の時に使用。)
  	public List<FunctionTree> outputfunctionTrees = null;
  	
  	//キーワードプログラミングの出力(オンライン学習の時に使用。)
  	private List<OutputCandidateLog> outputLogList = new ArrayList<OutputCandidateLog>();
  	
  	//1つのファイルfileは１つのTestSiteに対応。
  	public TestSite(File file, boolean isLogFile){
  		txtFile = file;
  		this.isLogFile = isLogFile;
    	FileReader fr;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String db_str;
			int line_number = 0;
			while ((db_str = br.readLine()) != null) {
				
				if(line_number == 0)
					className = db_str;
				else if(line_number == 1)
					offset = Integer.parseInt(db_str);
				else if(line_number == 2)
					original_offset = Integer.parseInt(db_str);
				else if(line_number == 3)
					line_number_start = Integer.parseInt(db_str);
				else if(line_number == 4)
					line_number_end = Integer.parseInt(db_str);
				else if(line_number == 5)
					selected_length = Integer.parseInt(db_str);
				else if(line_number == 6)
					selected_string = db_str;
				else if(line_number == 7)
					keywords = db_str;
				else if(line_number == 8)
					desiredReturnType = db_str;
				else if(line_number == 9)
					location = db_str;
		  		else if(line_number == 10){
			        //class (type)
					String s1[] = db_str.split(";");   // ";"が区切り。
					for(String ss1: s1){
						classesInActiveEditor.add(ss1);
					}
		  		}else{
					//function 1行ごと。
			        functionsInActiveEditor.add(db_str);
		  		}
		  		line_number++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		/*
		 * Logファイルの場合、出力候補群を保存したファイル ".*out.txt" も読み込む
		 */
		if(isLogFile){
			String name = txtFile.getName().replace(".txt", "out.txt");
			String path = txtFile.getParent() + "/" +name;
			File logFile = new File(path);
			if(logFile.exists()){
				try {
					fr = new FileReader(logFile);
					BufferedReader br = new BufferedReader(fr);

					selected_string_order = Integer.parseInt(br.readLine());//先頭行は選択候補の順位。
					
					String log_str;
					while ((log_str = br.readLine()) != null) {
						OutputCandidateLog log = new OutputCandidateLog(log_str, keywords.split("[ 　\t]").length);
						outputLogList.add(log);
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
    }
  	
  	public static void loadLogFiles(int numOfFiles){
  		String path = TestSite.LogSiteFolder;//ログを読み取る
  		
	    File dir = new File(path);
	    if (!dir.exists()) {  
		    return;
		}

	    File[] classFolders = dir.listFiles();
	    for (File folder: classFolders) {
	        File[] files = folder.listFiles();
	        
	        for(File file: files){
	        	//出力文字列ログファイルは除く。
	        	if(file.getName().matches(".*out\\.txt$") == false){
		        	logFiles.add(new TestSite(file, true));
	        	}
	        }
	    }
  		
	    //時間の降順にソートする。
	    Collections.sort(logFiles, new TestSiteComparator());
	    
//	    for(TestSite site: logFiles){
//	    	System.out.println(site.getSaveTime());
//	    }
	   
  	}
  	
  	/*
  	 * ログファイルの追加。
  	 */
  	public static void addLogFile(TestSite site){
  		//先頭に追加する。
  		logFiles.add(0, site);
  	}

  	/*
  	 * ログファイルの取得
  	 * 指定した数のみ、最新の先頭から。
  	 */
  	public static TestSite[] getLogFiles(int numOfFiles){
  		ArrayList<TestSite> list = new ArrayList<TestSite>();
  		for(int i = 0; i < numOfFiles; i++){
  			list.add(logFiles.get(i));
  		}
  		return (TestSite[]) list.toArray(new TestSite[list.size()]);
  	}
  	
    /**
     * 選択テキストからキーワードを自動生成するので、
     * キーワードを引数に必要としない。
     * 
     * @param cn	クラス名
     * @param o		オフセット
     * @param o_ori	変更前のオフセット
     * @param lis	選択開始行
     * @param lie	選択終了行
     * @param len	選択テキスト長
     * @param sct	選択テキスト
     * @param drt	望ましい返り値
     * @param lo	ロケーション
     * @param cls	Typeのリスト
     * @param fus	Functionのリスト
     * @param is_log_file ログファイルか？
     */
    public TestSite(String cn, int o, int o_ori, int lis, int lie, int len, String sct, String drt, String lo, List<String> cls, List<String> fus, boolean is_log_file){
    	className = cn;
    	offset = o;
    	original_offset = o_ori;
        line_number_start = lis;
        line_number_end = lie;
        selected_length = len;
        selected_string = sct;
        keywords = output2InputKeyword(selected_string);
        desiredReturnType = drt;
        location = lo;
        classesInActiveEditor = new HashSet<String>(cls);
        functionsInActiveEditor = new HashSet<String>(fus); 
        isLogFile = is_log_file;
        //ファイル名は「パッケージ名＋クラス名/currentTimeMillis」
      	String savefilename = TestSiteFolder + className + "/" + System.currentTimeMillis() + ".txt";
      	txtFile = new File(savefilename);
    }

	public String toString(){
    	String s = 
    	"className= " + className +
    	"\noffset= " + offset +
    	"\noriginal_offset= " + original_offset +
        "\nline_number_start= " + line_number_start +
        "\nline_number_end= " + line_number_end +
        "\nselected_length= " + selected_length +
        "\nselected_string= " + selected_string +
        "\nkeywords= " + keywords +
        "\ndesiredReturnType= " + desiredReturnType +
        "\nlocation= " + location +
        "\nclassesInActiveEditor= " + classesInActiveEditor +
        "\nfunctionsInActiveEditor= " + functionsInActiveEditor;
    	return s;
    }
    
    public String toDBString(){
    	String s =  className + "\n" + offset + "\n" + original_offset + "\n" + line_number_start + "\n" + line_number_end + "\n" +
    selected_length + "\n" + selected_string + "\n" + keywords + "\n" + desiredReturnType + "\n" + location + "\n";
    	//11行目types
        Iterator<String> it_c = classesInActiveEditor.iterator();
        while(it_c.hasNext()){
        	String ss = it_c.next();
        	s += ss + ";";
        }
        s += "\n";
        //12行以降 functions
        Iterator<String> it_f = functionsInActiveEditor.iterator();
        while(it_f.hasNext()){
        	s += it_f.next() + "\n";
        }
        return s;
    }
    
    
    
    public String getClassName(){
    	return className;
    }
    public void setClassName(String n){
    	className = n;
    }
    public String getPackageName(){
    	int dot = className.lastIndexOf('.');
    	if(dot == -1)
    		return className;
    	return className.substring(0, dot);
    }
    public String getClassSingleName(){
    	int dot = className.lastIndexOf('.');
    	return className.substring(dot);
    }
	public int getOffset(){
    	return offset;
    }
    public void setOffset(int o){
    	offset = o;
    }
    public int getOriginalOffset(){
    	return original_offset;
    }
    public int getLineNumberStart(){
    	return line_number_start;
    }
    public void setLineNumberStart(int s){
    	line_number_start = s;
    }
    public int getLineNumberEnd(){
    	return line_number_end;
    }
    public void setLineNumberEnd(int e){
    	line_number_end = e;
    }
    public int getSelectedLength(){
    	return selected_length;
    }
    public void setSelectedLength(int l){
    	selected_length = l;
    }
    public String getSelectedString(){
    	return selected_string;
    }
    public void setSelectedString(String s){
    	selected_string = s;
    }
    public String getDesiredReturnType(){
    	return desiredReturnType;
    }
    public void setDesiredReturnType(String d){
    	desiredReturnType = d;
    }
    public String getLocation(){
    	return location;
    }
    public String getKeywords(){
    	return keywords;
    }
    public void setKeywords(String k){
    	keywords = k;
    }
    public int getSelectedStringOrder(){
    	return selected_string_order;
    }
    
    public List<FunctionTree> getOutputFunctionTrees(){
    	return outputfunctionTrees;
    }
    
    public long getSaveTime(){
    	String s = txtFile.getName().replace(".txt", "");
    	return Long.parseLong(s);
    }
    
    public void setOutputLogList(List<OutputCandidateLog> outputLogList){
      	this.outputLogList = outputLogList;
    }
	/*
	 * 出力文字列を入力キーワードに変換する
	 * 
	 * 引数outが
	 *  message.replaceAll(space, comma);
	 *  ならば、
	 *  返り値は
	 *   message replace all space comma
	 *  となる。
	 *  
	 *  ピリオド、カンマ、セミコロン、括弧は取り、空白文字に変換する。
	 *  message　replaceAll　space　 comma　　
	 *  
	 *  replaceAllの部分は、大文字の手前に空白文字を挿入してから、
	 *  大文字を小文字に変換する。＞AstUtil.splitNameを使う。
	 *  
	 */
	public String output2InputKeyword(String out){
		if(out == null)
			return null;
		//ピリオド、カンマ、セミコロン、括弧は取り、空白文字に変換する。
		//ダブルクオーテーションと<>も取る。
		String rep1 = out.replaceAll("[\\.\\,\\;\\(\\)\\<\\>\"]", " ");
		//大文字の手前に空白文字を挿入してから、大文字を小文字に変換する。
		String rep2 = ast.AstUtil.splitName(rep1, " ");
		//複数の空白文字が連結していた場合１つにする。
		String rep3 = rep2.replaceAll(" +", " ");
		return rep3.trim();
	}
	
	//keywordの数をカウントする。
	private int countKeywords(){
		
		if(keywords == null)
			return -1;
		else{
			//個々のkeywordに分割
			String []input_keywords = keywords.split("[ 　\t]");
			return input_keywords.length;
		}
	}
	
	public int getNumOfKeywords(){
		return countKeywords();
	}
	
	public int getNumOfLocalFunctions(){
		if(functionsInActiveEditor == null)
			return 0;
		return functionsInActiveEditor.size();
	}
	
	public int getNumOfLocalTypes(){
		if(classesInActiveEditor == null)
			return 0;
		return classesInActiveEditor.size();
	}
	
	public void save(){
		try{
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(txtFile)));
			pw.write(toDBString());
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * キーワードプログラミングのアルゴリズムを実行する。
	 * そして、
	 * 一番selected_stringに編集距離が近い出力文字列の順位を出力する。
	 * 
	 */
	public int runKeywordProgrammingAndGetNearestOutputNumber(int output_size, String state){
		//キーワードプログラミングの実行
		FunctionTree[] ft = KeywordProgramming.execute(keywords, desiredReturnType,  new ArrayList<String>(classesInActiveEditor), new ArrayList<String>(functionsInActiveEditor), state);
		outputfunctionTrees = (ArrayList<FunctionTree>) Arrays.asList(ft);
		//一番selected_stringに編集距離が近い出力文字列の順位を得る。
		return getNearestDistanceOutputNumber(output_size);
	}

	/*
	 * 一番selected_stringに編集距離が近い出力文字列の順位を得る。
	 */
	private int getNearestDistanceOutputNumber(int output_size) {
		
		int best_tree_order = -1;

//		String s = i + ", ";
		String s = "";
		for(double d : ExplanationVector.getWeights()){
			s += d + ", ";
		}
		
		int min_d = 99999;

		if(outputfunctionTrees == null){
			s += min_d + ", " + ", 回答なし ts == null";
//			System.out.println(i + ", 回答なし ts == null, "+ desiredReturnType);
		}else{

			FunctionTree best_tree = null;

			//pop-upに表示された候補の中のうち、
			//output_size 以内の中で、
			//一番答えに近い距離の候補を探す
			if(output_size > outputfunctionTrees.size())
				output_size = outputfunctionTrees.size();//長さがoverしてたら、配列サイズにする。
			for(int j = 0; j < output_size; j++){
				FunctionTree t = outputfunctionTrees.get(j);
				if(t != null){
					
					LevenshteinDistance ld = new LevenshteinDistance() ;
					int d = ld.edit(t.toCompleteMethodString(), selected_string);
					
					if(d < min_d){
						min_d = d;
						best_tree = t;
						best_tree_order = j;
					}
//						System.out.println(j + ", eval=" + t.getEvaluationValue() + ", dist=" + d + ", " + t.toCompleteMethodString());
				}
			}

			if(best_tree == null){
				s += min_d + ", " + best_tree_order + ", " + ", 回答なし best_tree == null";
//				System.out.println(i + ", 回答なし best_tree == null, "+ desiredReturnType);
			}else{
				//System.out.println(i + ", " + min_d + ", " + best_tree.toCompleteMethodString());
				s += min_d + ", " + best_tree_order + ", " + best_tree.toCompleteMethodString() + ", " + best_tree.getEvaluationValue();
//				System.out.println(s);
				best_tree.setSelectedFlg();	//選んだフラグ。
			}

		}
		System.out.println("=== " + selected_string + " に一番編集距離が近い文字列 ===");
		System.out.println(s);
		System.out.println("=== " + selected_string + " に一番編集距離が近い文字列 ===");
		
		return best_tree_order;
	}


	/*
	 * キーワードプログラミングのアルゴリズムを実行する。
	 * そして、正解の順位を出力する。
	 * 正解が出なければ、
	 * -1を返す。
	 */
	public int runKeywordProgrammingAndOutputNumber(int output_size, String state){
		FunctionTree[] ft = KeywordProgramming.execute(keywords, desiredReturnType,  new ArrayList<String>(classesInActiveEditor), new ArrayList<String>(functionsInActiveEditor), state);
		outputfunctionTrees = (ArrayList<FunctionTree>) Arrays.asList(ft);
		//正解の順位を出力する。
		return getAnswerNumber(output_size);
	}

	//キーワードプログラミングの初期化
	public void initKeywordProgramming(){
		outputfunctionTrees = null;
	}
	
	//キーワードプログラミングの終了
	public void closeKeywordProgramming(){
		outputfunctionTrees = null;
	}
	
	/*
	 * キーワードプログラミングのアルゴリズムを実行し、
	 * 出力候補群を返す
	 */
	public List<FunctionTree> runKeywordProgramming(String state){
		//キーワードプログラミングの実行
		FunctionTree[] ft =  KeywordProgramming.execute(keywords, desiredReturnType,  new ArrayList<String>(classesInActiveEditor), new ArrayList<String>(functionsInActiveEditor), state);
		outputfunctionTrees = Arrays.asList(ft);
		return outputfunctionTrees;
	}

	/*
	 * 評価値の再計算
	 */
	public void reCalculateEvaluationValue(){
		if(isLogFile == false){
			for(FunctionTree ft: outputfunctionTrees){
				ft.getEvaluationValue();
			}
		}else{
			for(OutputCandidateLog log: outputLogList){
				log.calculateEvaluationValue();
			}
		}
	}

	/*
	 * 出力候補のソート
	 */
	public void sortFunctionTrees(){
		if(isLogFile == false){
			Collections.sort(outputfunctionTrees);
		}else{
			Collections.sort(outputLogList, new OutputCanditateLogComparator());
		}
	}
	
	/*
	 * 正解の順位を求める。
	 * 順位は0番から。
	 * 
	 * 正解が出なければ、-1を返す。
	 */
	public int getAnswerNumber(int output_size) {
		
		int best_tree_order = -1;

//		String s = i + ", ";
		String s = "";
		for(double d : ExplanationVector.getWeights()){
			s += d + ", ";
		}
		
		if(isLogFile == false){
			//バッチ処理のとき。
			if(outputfunctionTrees == null){
				s += "回答なし ts == null";
			}else{
	
				FunctionTree best_tree = null;
	
				//pop-upに表示された候補の中に正解があるか調べる。
				if(output_size > outputfunctionTrees.size())
					output_size = outputfunctionTrees.size();//長さがoverしてたら、配列サイズにする。
				for(int j = 0; j < output_size; j++){
					FunctionTree t = outputfunctionTrees.get(j);
					if(t != null){
						if(t.toCompleteMethodString().equals(selected_string.trim())){
							best_tree = t;
							best_tree_order = j;
							break;
						}
					}
				}
	
				if(best_tree == null){
					s += best_tree_order + ", 回答なし best_tree == null";
				}else{
					s += best_tree_order + ", " + best_tree.toCompleteMethodString() + ", " + best_tree.getEvaluationValue();
					best_tree.setSelectedFlg();	//選んだフラグ。
				}
	
			}
	//		System.out.println("=== " + selected_string + " が出現した順位 ===");
	//		System.out.println(s);
	//		System.out.println("=== " + selected_string + " が出現した順位 ===");
		}else{
			//オンライン処理のとき。
			if(outputLogList == null){
				s += "回答なし ts == null";
			}else{
	
				OutputCandidateLog best_log = null;
	
				//pop-upに表示された候補の中に正解があるか調べる。
				if(output_size > outputLogList.size())
					output_size = outputLogList.size();//長さがoverしてたら、配列サイズにする。
				for(int j = 0; j < output_size; j++){
					OutputCandidateLog log = outputLogList.get(j);
					if(log != null){
						if(log.getCompleteMethodString().equals(selected_string.trim())){
							best_log = log;
							best_tree_order = j;
							break;
						}
					}
				}
	
				if(best_log == null){
					s += best_tree_order + ", 回答なし best_tree == null";
				}else{
					s += best_tree_order + ", " + best_log.getCompleteMethodString() + ", " + best_log.getEvaluationValue();
				}
	
			}
		}
		return best_tree_order;
	}

	public boolean isSelectedTask() {
		return isSelectedTask;
	}

	public void setSelectedTask(boolean isSelectedTask) {
		this.isSelectedTask = isSelectedTask;
	}
	
	/*
	 * テストサイトのファイルを新規作成
	 */
	public void createNewFile() {
		//フォルダがなければ作成する。
		File dir = txtFile.getParentFile();
		if (!dir.exists()) {  
		    dir.mkdirs();
		}
		
		try{
			txtFile.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(txtFile)));
			pw.write(this.toDBString());
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * テストサイトのファイルを新規作成
	 */
	public void copyAndMoveFileToTestSiteFolder() {
		//ファイルがなければ
		if (!txtFile.exists()) {  
		    return;
		}
		String savefilename = TestSiteFolder + className + "/" + txtFile.getName();
      	File newFile = new File(savefilename);
      	
		//フォルダがなければ作成する。
		File dir = newFile.getParentFile();
		if (!dir.exists()) {  
		    dir.mkdirs();
		}
		
		//ファイルをコピーする。
		try{
			copyFile(txtFile, newFile);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * テストサイトを削除する
	 */
	public void deleteFile() {
		//ファイルがなければ
		if (!txtFile.exists()) {  
		    return;
		}
		txtFile.delete();
	}
	
	/*
	 * 履歴としてテストサイトのファイルを新規作成
	 * 
	 * 保存する場所が違うだけ。
	 */
	public void createNewFileAsLog(String selected_string, int selected_length, int selected_string_order, String keywords) {
		this.selected_string = selected_string;
		this.selected_length = selected_length;
		this.selected_string_order = selected_string_order;
		this.keywords = keywords;
		
		//ファイル名は「パッケージ名＋クラス名/currentTimeMillis」
		long millis = System.currentTimeMillis();
      	String savefilename = LogSiteFolder + className + "/" + millis + ".txt";
      	//出力文字列も保存。
      	String savefilename_o = LogSiteFolder + className + "/" + millis + "out.txt";
      	File  file = new File(savefilename);
      	File  file_o = new File(savefilename_o);
		//フォルダがなければ作成する。
		File dir = file.getParentFile();
		if (!dir.exists()) {  
		    dir.mkdirs();
		}
		
		try{
			file.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.write(this.toDBString());
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		try{
			file_o.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file_o)));
			pw.write(selected_string_order + "\n");	//先頭行は選択した候補の順位。
			for(FunctionTree ft : KeywordProgramming.getOutputFunctionTrees()){
				pw.write(ft.toLogDBString());
			} 
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * ファイル保存日時を取得
	 */
	public String getSaveDate(){
		if(txtFile == null)
			return null;
		else{
			Date date = new Date(getSaveTime());
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			return df.format(date);
		}
	}
	
	//ファイルをコピーする。
	 private void copyFile(File in, File out) throws IOException {
	    FileChannel sourceChannel = new FileInputStream(in).getChannel();
	    FileChannel destinationChannel = new FileOutputStream(out).getChannel();
	    sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
	    sourceChannel.close();
	    destinationChannel.close();
	 }
	 
	 /*
	  * このTestSiteのIDを返す。
	  */
	 public String getId(){
		 return String.valueOf(getSaveTime());
	 }
}
