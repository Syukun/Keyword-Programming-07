package keywordProgramming;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import state.KpRunningState;

import logging.LogControl;

/**
 * キーワードプログラミングの主要な３つのアルゴリズム
 *  動的計画法(DynamicProgram)、
 *  GetBestExplTreeForFunc、
 *  ExtractTree
 * を記述したクラス。
 * 
 * @author sayuu
 *
 */
public class KeywordProgramming {

	//動的計画法の表の高さ
	public static int HEIGHT = 3;
	
	//動的計画法の表の１つの交点に保持する「根」関数木の個数
	public static int BEST_R = 25;

	//内部データベースのファイル名。　型。
	public static final String SUB_CLASS_FILE_NAME = "sub_class.txt";
	
	//内部データベースのファイル名。　関数。
	public static final String FUNCTION_FILE_NAME = "function.txt";

	//動的計画法によって作成される表を表すオブジェクト、bestRoots
	private static BestRoots bestRoots;
	
	//アルゴリズムに使用される全ての型を保持するTreeMap
	private static TreeMap<String, Type> types = new TreeMap<String, Type>();
	
	//アルゴリズムに使用される全ての関数を保持するArrayList
	private static ArrayList<Function> functions = new ArrayList<Function>();
	
	//内部データベースに存在する全ての型を保持するTreeMap
	private static TreeMap<String, Type> original_types = new TreeMap<String, Type>();
	
	//内部データベースに存在する全ての関数を保持するArrayList
	private static ArrayList<Function> original_functions = new ArrayList<Function>(1);

	/*
	 * 最終的な出力関数木の列。
	 * キーがその関数木の順位。
	 * 値がその関数木。
	 * 評価値順にソートされている。
	 */
	private static FunctionTree[] outputFunctionTrees;

	//入力キーワードのList
	private static List<String> input_keywords;
	
	//inputの単語の内、同一の単語それぞれの個数
	private static HashMap<String, Integer> numOfSameWords = new HashMap<String, Integer>();


	public static BestRoots getBestRoots(){
		return bestRoots;
	}

	public static void setBestRoots(BestRoots br){
		bestRoots = br;
	}

	public static void addType(Type t){
		types.put(t.getName(), t);
	}

	public static TreeMap<String, Type> getTypes(){
		return types;
	}

	public static void addFunction(Function f){
		functions.add(f);
	}

	//Classesファイルを読み込んで、リストに格納
	public static void readClassFileAndAddList(String input_txt_file_name){
		FileReader fr;

		try {
			fr = new FileReader(input_txt_file_name);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while ((s = br.readLine()) != null) {
				Type t = new Type(s);
				types.put(t.getName(), t);
				original_types.put(t.getName(), t);
			}
			br.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	//Functionsファイルを読み込んで、リストに格納
	public static void readFunctionFileAndAddList(String input_txt_file_name){
		FileReader fr;
		try {
			fr = new FileReader(input_txt_file_name);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while ((s = br.readLine()) != null) {
				Function f = new Function(s);
				functions.add(f);
				original_functions.add(f);
			}
			br.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}
	//ファイルの読み込み
	public static void loadFiles(BufferedReader c_r, BufferedReader f_r){
		String s;

		//classesファイルの読み込み
		try {
			while ((s = c_r.readLine()) != null) {
				Type t = new Type(s);
				types.put(t.getName(), t);
				original_types.put(t.getName(), t);
			}
			c_r.close();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		//functionsファイルの読み込み
		try {
			while ((s = f_r.readLine()) != null) {
				Function f = new Function(s);
				functions.add(f);
				original_functions.add(f);
			}
			f_r.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	private static boolean containsType(String appear){
		Collection<Type> values = types.values();
		for(Type type: values){
			if(type.getName().equals(appear)){
				return true;
			}
		}
		return false;
	}

//	public static FunctionTree getOutputFunctionTree(int i){
//		return outputFunctionTrees.get(i);
//	}

	public static FunctionTree[] getOutputFunctionTrees(){
		return outputFunctionTrees;
	}


	//プラグインの中からキーワードプログラミングを動かす。
	public static FunctionTree[] execute(String keywords, String desiredType, List<String> localTypes, List<String> localFunctions, String state){

		long start = System.currentTimeMillis();
		
		ArrayList<Type> new_types = new ArrayList<Type>();
		ArrayList<Function> new_funcs = new ArrayList<Function>();

		if(localTypes != null)
		for(String s: localTypes){
			Type t = new Type(s);
			if(!types.containsKey(t.getName())){
				types.put(t.getName(), t);
				new_types.add(t);
			}else{
				//キーがあるとき、
				//subtypeを追加する。
				types.get(t.getName()).addSubTypes(t.getSubTypes());
			}
		}

		if(localFunctions != null)
		for(String s: localFunctions){
			Function f = new Function(s);
			//新しい関数があった場合のみ、
			if(functions.add(f) == true){
				new_funcs.add(f);
				//新しく出現したクラスのチェック

				//返り値のチェック
				String ret = f.getReturnType();
				Type t = new Type(ret);
				if(!types.containsKey(t.getName())){
					types.put(t.getName(), t);
					new_types.add(t);
				}else{
					//キーがあるとき、
					//subtypeを追加する。
					types.get(t.getName()).addSubTypes(t.getSubTypes());
				}

				//パラメータのチェック
				if(f.getParameters() != null)
				for(String param: f.getParameters()){//無ければ追加
					Type tt = new Type(param);
					if(!types.containsKey(tt.getName())){
						types.put(tt.getName(), tt);
						new_types.add(tt);
					}else{
						//キーがあるとき、
						//subtypeを追加する。
						types.get(tt.getName()).addSubTypes(tt.getSubTypes());
					}
				}
			}
		}

		//キーワードの処理。
		inputKeywords(keywords);
		
		//bestRootsを生成する。
		bestRoots = new BestRoots(types);

		//DynamicProgram アルゴリズムを実行
		DynamicProgram();
		
		//ExtractTree アルゴリズムを実行
		outputFunctionTrees = ExtractTree(desiredType);

		long stop = System.currentTimeMillis();
		
		//結果をコンソールに表示する。
		printResult(desiredType, new_types, new_funcs, stop-start, state);
		
		//bestRootsの中身を表示
//		printBestRoots();
//		printFunctionsList(functions);
		//ログ出力用ファイル
		//createLogFiles(new_types, new_funcs);
	
		//staticなフィールドを全てクリアする
		clearStaticFields();

		return outputFunctionTrees;
	}

		
	/*
	 * TypeとFunctionのログを出力する。
	 */
	private static void createLogFiles(HashSet<Type> new_types, HashSet<Function> new_funcs) {
		String Type_FILE_NAME = "./type.txt";
		String Function_FILE_NAME = "./function.txt";


			File fileL = new File(Type_FILE_NAME);
			File fileC = new File(Function_FILE_NAME);


			try{


				fileL.createNewFile();
				fileC.createNewFile();
				PrintWriter pwL = new PrintWriter(new BufferedWriter(new FileWriter(fileL)));
				PrintWriter pwC = new PrintWriter(new BufferedWriter(new FileWriter(fileC)));

				for(Type t: new_types){
					//System.out.println(t.toDBString());
					pwL.println(t.toDBString());
				}

				for(Function f: new_funcs){
					//System.out.println(t.toDBString());
					pwC.println(f.toDBString());
				}
				pwL.close();
				pwC.close();


			}catch(IOException e){
				e.printStackTrace();
			}
	}

	/*
	 * デバッグ用 結果表示メソッド
	 */
	
	private static void printResult(String desiredType, ArrayList<Type> new_types, ArrayList<Function> new_funcs, long time, String state) {
		LogControl logControl = new LogControl(LogControl.KP);
		
		//コードコンプリーションのときはログ状態の表示を行う。サーチモードのときはログ状態の表示を毎回行わない。邪魔なので。
		if(state.equals(KpRunningState.CODE_COMPLETION))
			logControl.printLogState();
		
		//new_typesに含まれる空文字列削除
		new_types.remove(new Type(""));
		
		logControl.println(">>> start keyword programming >>>");
		logControl.println("");

		logControl.println(" >> 基本情報 >>", LogControl.KP_BASIC);
		logControl.println("  実行にかかった時間= " + time + " ミリ秒。KeywordProgramming.printResult", LogControl.KP_BASIC);
		logControl.println("  BEST_R =" + BEST_R);
		logControl.print("  入力キーワード= ", LogControl.KP_BASIC);
		for(String w: input_keywords)
			logControl.print(w + ", ", LogControl.KP_BASIC);
		logControl.println("");
		logControl.println("  出力候補の望ましい返り値の型= " + desiredType, LogControl.KP_BASIC);
		logControl.println("  入力キーワード数=" + input_keywords.size());
		logControl.println("  総型数= " + types.size(), LogControl.KP_BASIC);
		if(new_types != null)
			logControl.println("  ローカルの型数= " + new_types.size(), LogControl.KP_BASIC);
		logControl.println("  総関数数= " + functions.size(), LogControl.KP_BASIC);
		if(new_funcs != null)
			logControl.println("  ローカルの関数数= " + new_funcs.size(), LogControl.KP_BASIC);
		if(outputFunctionTrees != null)
			logControl.println("  生成された出力の総数= " + outputFunctionTrees.length, LogControl.KP_BASIC);
		
		logControl.println("  特徴の重みの組= (" + ExplanationVector.getWeightString() + ")", LogControl.KP_BASIC);
		
		logControl.println(" << 基本情報 <<", LogControl.KP_BASIC);
		logControl.println("", LogControl.KP_BASIC);
		
		if(new_types != null)
			logControl.println("  ローカルの型数= " + new_types.size(), LogControl.KP_TYPES);
		logControl.println(" >> ローカルの型一覧 >>", LogControl.KP_TYPES);
		if(new_types != null){
			for(Type t: new_types){
				logControl.println("  " + t.toDBString(), LogControl.KP_TYPES);
			}
		}
		logControl.println(" << ローカルの型一覧 <<", LogControl.KP_TYPES);
		logControl.println("", LogControl.KP_TYPES);

		
		if(new_funcs != null)
			logControl.println("  ローカルの関数数= " + new_funcs.size(), LogControl.KP_FUNCTIONS);
		logControl.println(" >> ローカルの関数一覧 >> 出力形式：[親クラス名, isStatic, isFinal, type(field or constructor or method or localvariable), 返り値の型, 名前, ラベル(区切り文字;), 引数の型(何個でも) ]", LogControl.KP_FUNCTIONS);
		if(new_funcs != null){
			for(Function f: new_funcs){
				logControl.println("  " + f.toDBString(), LogControl.KP_FUNCTIONS);
			}
		}
		logControl.println(" << ローカルの関数一覧s << 出力形式：[親クラス名, isStatic, isFinal, type(field or constructor or method or localvariable), 返り値の型, 名前, ラベル(区切り文字;), 引数の型(何個でも) ]", LogControl.KP_FUNCTIONS);
		logControl.println("", LogControl.KP_FUNCTIONS);

		logControl.println(" >> 出力候補一覧 >> 出力形式：[評価値, p(4つの特徴量), e_i(長さはキーワード数に等しい), 出力文字列]", LogControl.KP_RESULTS);
		for(FunctionTree t:outputFunctionTrees){
			if(t != null){
				logControl.println("  " + t.toEvalString() + t.toCompleteMethodString(), LogControl.KP_RESULTS);
//					logControl.out(t.toLogDBString());
			}
		}
		logControl.println(" << 出力候補一覧 << 出力形式：[評価値, p(4つの特徴量), e_i(長さはキーワード数に等しい), 出力文字列]", LogControl.KP_RESULTS);
		logControl.println("", LogControl.KP_RESULTS);

		logControl.println("<<< end keyword programming <<<");
		logControl.close();
	}

	/**
	 * 元のサイズを記憶しておいて、
	 * 元のサイズに戻せば良いだけではないのか？
	 *
	 * HashSetにすると
	 * クリアの必要がなくなる！
	 *
	 * static field のクリア
	 *
	 * 1:typesリストとfunctionsリストのクリア
	 *   新たに追加したローカル情報の削除
	 *
	 * 2:numOfSameWordsのクリア
	 *
	 * @param localTypeSize
	 * @param localFunctionSize
	 */

	/*
	 * retainAll: 重なっている内容のみ保持し、重ならない内容は削除する。
	 */
	public static void clearStaticFields(){
		types.clear();
		types.putAll(original_types);
        //functions.retainAll(original_functions);
		functions.clear();
		functions.addAll(original_functions);
		numOfSameWords.clear();
		bestRoots.clearTable();//これやってもやらなくても変わらないようだ。
//		System.gc();
		//used memoryの測定(単位：KByte)
//		Runtime rt = Runtime.getRuntime();
//		long usedMemory = rt.totalMemory()/1024 - rt.freeMemory()/1024;
//		System.out.println("使用メモリ量= " + usedMemory + "KByte");
	}

	//Classesリストの表示
	public static void printClassList(ArrayList<String> list){
		for(String s: list){
			System.out.println(s);
		}
	}
	//Functionsリストの表示
	public static void printFunctionsList(ArrayList<Function> list){
		for(Function f: list){
			System.out.println(f.toString());
		}
	}

	public static void printBestRoots(){
		for(int i = 0; i < HEIGHT; i++){

			for(Type t : types.values()) {
				FunctionTree[] roots = bestRoots.getRoots(t.getName(), i);
				for(FunctionTree r: roots){
					if(r != null){//このnullチェックは必要
						System.out.print("i= "+ i + ", ");
						System.out.print("ret_t_of_f= "+ t.getName() + ", ");
						System.out.print("f= "+ r.getRoot().getFunction().getName() + ", ");
						System.out.print("parent_t_of_f= "+ r.getRoot().getFunction().getParentClass() + ", ");
						System.out.print("tree= "+ r.toCompleteMethodString() + ", ");
						System.out.print("e= "+ r.getEvaluationValue());
						System.out.println("");
					}
				}
//				int count = bestRoots.getSizeOfRoots(t.getName(), i);
//				if(roots.length > 0)
//					System.out.println(i + ", "+ t.getName() + ", " + count);
			}
			System.out.println("");
		}
	}


	/*
	 * キーワードのついての処理。
	 */
	public static void inputKeywords(String s){
		
		if(s.equals("")){
			input_keywords = new ArrayList<String>();
			return;
		}
			
		//文字列を小文字化する
		String s_lowerCase = s.toLowerCase();
		//keywordに分割
		input_keywords = Arrays.asList(s_lowerCase.split("[ 　\t]"));

		//keywordを同一単語ごとにその個数をカウントする
		for(int i= 0; i < input_keywords.size(); i++){
			String word = input_keywords.get(i);
			int count = 1;
			if(numOfSameWords.containsKey(word)){
				//キーがすでにあるとき、以前までの数と１を足す
				count += numOfSameWords.get(word);
			}
			numOfSameWords.put(word, count);
		}
	}

	/**
	 * procedure DynamicProgram
	 *
	 * this function modifies bestRoots
	 */
	public static void DynamicProgram(){

		/*
		 * "java.lang.Object"のsubTypeを作成。
		 */
//		Type t_obj = types.get("java.lang.Object");
//		for(Type t_sub: types.values())
//			if(!t_sub.getName().equals("java.lang.Object"))
//				t_obj.addSubType(t_sub.getName());

		//for each 1 <= i <= h
		for(int i = 0; i < HEIGHT; i++){
			//for each t in T
			for(Type t : types.values()) {


				// bestRoots(t,i) = 0
				//for each f in F where ret(f) in sub(t)
				for(String subt: t.getSubTypes()){


					for(Function f: functions){

						if(f.getReturnType().equals(subt)){
//							if(i == 2 && t.getName().equals("java.lang.Object") && f.getParentClass().equals("java.util.List<java.lang.String>") && f.getName().equals("add")){
//								System.out.println("jkjk");
//							}
							// e = GetBestExplForFunc(f, i-1)
							FunctionTree tree = GetBestExplTreeForFunc(f, i);
					
							// if e > -∞ then bestRoots(t, i) = bestRoots(t, i)U(e, f)
							if(tree != null && tree.getEvaluationValue() > -9999.0){
								bestRoots.addRoot(tree);
							}
						}
					}
	            }
				//keep the best r roots
				bestRoots.keepBestRoots(t.getName(), i);
            }
		}
	}

	/**
	 * procedure GetBestExplForFunc
	 *
	 *	最低でもRootのみのTreeは取得できる
	 *
	 * @param f
	 * @param h_max
	 * @return
	 */
	public static FunctionTree GetBestExplTreeForFunc(Function f, int h_max){

//		if(f.getName().equals("getFruitList")){
//			System.out.println("aaa");
//		}
		
		/*
		 * 親クラスが"this"かつ、非staticの場合は、レシーバ無しでも起動できるようにする。
		 */
		boolean this_flg = false;
		if(f.getParentClass().equals("this") && f.isStatic() == false){
			this_flg = true;
		}
			
		FunctionTree best_tree = new FunctionTree(f, input_keywords, numOfSameWords);//RootのみのTree
		ExplanationVector e_cumulative = new ExplanationVector(best_tree.e_vec);//best_treeのe_vecをコピーしてnew
		
		//for each p in params(f)
		
		if(f.getParameters() == null)
			return best_tree;	//関数に引数がないとき、rootのみを返す
		else if(h_max == 0)
			return null;	//関数に引数はあるが、高さが0のとき、nullを返す
		
		/*
		 * this_flg=trueのときは、高さ0、引数レシーバのみのときがある。
		 */
		if(this_flg == true && f.getParameters().length == 1)
			return best_tree;
		
		for(String param: f.getParameters()){//引数の順番通りにループする
			//e_best = (-∞, 0, 0, 0, ...) 
			ExplanationVector e_best = new ExplanationVector(input_keywords.size(), -ExplanationVector.INFINITE_VALUE);//空っぽ	
			FunctionTree param_tree = null;
			//for each 1 <= i <= h
			for(int i = 0; i < h_max; i++){//h_max=0の場合はこのループは実行されない
				//for each (e', f') in bestRoots(p, i)
				FunctionTree[] trees = bestRoots.getRoots(param, i);
				if(trees != null){
					for(FunctionTree t: trees){
						if(t != null){
							//if e_cumulative + e' > e_best
							//    e_best = e_cumulative + e'
							
							ExplanationVector tmp = ExplanationVector.add(e_cumulative, t.e_vec);
							if(tmp.compareTo(e_best) == 1){
								e_best.substitution(tmp);//代入
								param_tree = t;
							}

						}
					}
				}
			}
			//e_cumulative = e_best
			e_cumulative.substitution(e_best);
			
			if(param_tree == null)
				return null;	//引数が１つでも埋まらなければ,そのroot関数は選択しない。nullを返す。
			else
				best_tree.addChild(param_tree);
		}

		return best_tree;
	}

	/**
	 *
	 * 動的計画法の表で
	 * 望ましい返り値の型を返す関数木を
	 * 高さ1からHEIGHTまで全て取得し、
	 * 評価値でソートして返す関数
	 *
	 * @param desiredType 望ましい返り値の型
	 * @return outputTrees 評価値でソートされた関数木のリスト
	 */
	public static FunctionTree[] ExtractTree(String desiredType){

		Set<FunctionTree> outputTrees = new HashSet<FunctionTree>(BEST_R * HEIGHT * 4 / 3 + 1);

		for(int i=0; i< HEIGHT; i++){
			FunctionTree[] roots = bestRoots.getRoots(desiredType, i);
			if(roots != null)
				for(FunctionTree t: roots){
					//null要素は削除
					//文字列が重複しないようにする。
					if(t != null){
						outputTrees.add(t);
					}
				}
		}
		outputTrees = new TreeSet<FunctionTree>(outputTrees);
		return outputTrees.toArray(new FunctionTree[0]);
	}

}
