package keywordProgramming;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * ExplanationVectorを表すクラス
 * @author sayuu
 *
 */
public class ExplanationVector{
	
	//関数を評価する特徴の数
	public static final int FEATURE_NUM = 4;

	//特徴の重みベクトル
	private static double[] w_arr = {-0.05, 1, -0.01, 0.001};		//先行研究初期値。
	
	//重みの更新幅
	private static double[] w_step_arr = {0.04, 0.2, 0.02, 0.02};

	//キーワードの個数
	public static int keywords_length;
	
	//キーワードごとの特徴量
	private double[] keyword_p;
	
	//全ての要素(e0とe_arr)を足したもの.大小比較にはこれを使う.	
	private double sum;
	
	//特徴ベクトル
	private double p[] = new double [FEATURE_NUM];	

	public static double INFINITE_VALUE = 9999.9;
	
	/**
	 * コンストラクタ
	 * @param keywords_length キーワード数
	 */
	public ExplanationVector(int keywords_length){
		ExplanationVector.keywords_length = keywords_length;
		this.keyword_p = new double[keywords_length];
	}

	/**
	 * コンストラクタ
	 * @param keywords_length キーワード数
	 * @param sum ExplanationVectorの要素の合計値
	 */
	public ExplanationVector(int keywords_length, double sum){
		ExplanationVector.keywords_length = keywords_length;
		this.keyword_p = new double[keywords_length];
		this.sum = sum;
	}

	/**
	 * コンストラクタ
	 * @param e コピーしたいExplanationVector
	 */
	public ExplanationVector(ExplanationVector e){
		this.keyword_p = new double[keywords_length];
		substitution(e);
	}

	/**
	 * 比較のための
	 * ExplanationVectorの各要素の合計値を計算する
	 * 
	 */
	public void calcSum(){
		sum = 0.0;
		//キーワードアレーからキーワードの特徴量を算出。
		double sum_k = 0.0;
		for(double k: keyword_p)
			sum_k += k;
		p[1] = sum_k;

		//各特徴の合計値。
		for(int i = 0; i < FEATURE_NUM; i++){
			sum += w_arr[i] * p[i];
		}

	}

	/**
	 * 比較のための
	 * ExplanationVectorの各要素の合計値を
	 * 計算し、取得する。
	 * 
	 * @return ExplanationVectorの各要素の合計値
	 */
	public double calcAndGetSum(){
		calcSum();
		return sum;
	}
	
	public double getSum(){
		return sum;
	}

	/**
	 * ExplanationVectorに子ノード付け加えた際の値を計算する。
	 * 
	 * @param child　付け加えられる子ノード
	 */
	public void add(ExplanationVector child){
		//キーワードアレーからキーワードの特徴量を算出。
		double sum_k = 0.0;
		for(int i = 0; i < keyword_p.length; i++){
			keyword_p[i] += child.keyword_p[i];
			if(keyword_p[i] > 1)
				keyword_p[i] = 1;
			sum_k += keyword_p[i];
		}

		//各特徴量を合算(単純な親子の合計)
		for(int i = 0; i < FEATURE_NUM; i++){
			p[i] += child.p[i];
		}
		p[1] = sum_k;


		calcSum();
	}

	/**
	 * 親のExplanationVectorに子を付け加えた際の値を計算して
	 * その結果を保持した新しいExplanationVectorを返す。
	 * 
	 * @param parent　親のExplanationVector
	 * @param child　子のExplanationVector
	 * @return 計算結果を保持した新しいExplanationVector
	 */
	public static ExplanationVector add(ExplanationVector parent,  ExplanationVector child){
		ExplanationVector new_e = new ExplanationVector(parent);
		//キーワードアレーからキーワードの特徴量を算出。
		double sum_k = 0.0;
		for(int i = 0; i < new_e.keyword_p.length; i++){
			new_e.keyword_p[i] = parent.keyword_p[i] + child.keyword_p[i];
			if(new_e.keyword_p[i] > 1)
				new_e.keyword_p[i] = 1;
			sum_k += new_e.keyword_p[i];
		}

		//各特徴量を合算(単純な親子の合計)
		for(int i = 0; i < FEATURE_NUM; i++){
			new_e.p[i] = parent.p[i] + child.p[i];
		}
		new_e.p[1] = sum_k;
		new_e.calcSum();
		return new_e;
	}

	/**
	 * このオブジェクトに引数で与えられたExplanationVectorの
	 * 各要素の値を全てコピーする。
	 * 
	 * @param e 内容をコピーしたいExplanationVector
	 */
	public void substitution(ExplanationVector e){
		sum = e.sum;
		for(int i = 0; i < FEATURE_NUM; i++){
			p[i] = e.p[i];
		}
		for(int i = 0; i < keyword_p.length; i++){
			keyword_p[i] = e.keyword_p[i];
		}
	}

	/**
	 * 入力キーワードからExplanationVectorを計算する。
	 * 
	 * @param input_words 入力キーワードのList
	 * @param numOfSameWordsIn 個々のキーワードがそれぞれ何個入力クエリに含まれているかを表すHashMap
	 * @param ft ExplanationVectorを計算したいFunctionTree
	 * @return 各要素に値が入力され、各要素の合計値の計算がなされたExplanationVector
	 */
	public static ExplanationVector calcExplanationVector(List<String> input_words, HashMap<String, Integer> numOfSameWordsIn, FunctionTree ft){
		ExplanationVector e = new ExplanationVector(input_words.size());
		Function f = ft.getRoot().getFunction();
		
		/*
		 * 評価値の計算は、
		 * 木のrootの評価値だけを計算して、
		 * すでに求まっている子の値に足せばよい。
		 */

		//初期化
		for(int i = 0; i < FEATURE_NUM; i++){
			e.p[i] = 0;
		}

		//木全体のノード数
		e.p[0] = 1;	//root1つなので、1.

		//キーワードに一致するラベルの数

		//f.labelにinputと同一のキーワードがある時の計算
		//同一単語の出現個数をそれぞれx, yとするとe_i = max(x/y, 1)とする.
		for(int i=0; i < input_words.size(); i++){
			for(String flab: f.getLabels()){
				if(input_words.get(i).equals(flab)){
					e.keyword_p[i] = (double)(ft.numOfSameWordsFunc.get(flab)) / numOfSameWordsIn.get(flab);
					if(e.keyword_p[i] > 1)
						e.keyword_p[i] = 1;
				}
			}
		}


		//キーワードに一致しないラベルの数
		for(String flab: f.getLabels()){
			if(numOfSameWordsIn.containsKey(flab) == false){
				e.p[2]++;
			}
		}

		//このfがローカル変数、メンバ変数（関数）ならばe_0を+0.001する(fはコンテキストに近い方が望ましいため)
		//（後で書く）
		if( f.getParentClass().equals("") || f.getParentClass().equals("this")){
			e.p[3]++;
		}

		e.calcSum();
		return e;
	}
	
	public static double[] getWeights(){
		return w_arr;
	}
	
	/*
	 * "-0.05, 1, -0.01, 0.001"
	 * の形式の文字列を返す。
	 */
	public static String getWeightString(){
		String str = "";
		for(int i = 0; i < w_arr.length; i++){
			str += String.valueOf(w_arr[i]);
			if(i < w_arr.length-1)
				str += ", ";
		}
		return str;
	}

	/*
	 * "-0.05, 1, -0.01, 0.001"
	 * の形式の文字列を設定する。
	 */
	public static void setWeightString(String input){
		String[] s_arr = input.split(",");
		for(int i = 0; i < w_arr.length; i++){
			w_arr[i] = Double.parseDouble(s_arr[i].trim());
		}
	}
	
	/*
	 * 1つの特徴の重みの設定
	 */
	public static void setWeight(double w, int i){
		w_arr[i] = w;
	}

	
	public static void setWeights(double []w){
		w_arr = Arrays.copyOf(w, w.length);
	}

	public static double[] getSteps(){
		return w_step_arr;
	}
	
	/*
	 * "-0.05, 1, -0.01, 0.001"
	 * の形式の文字列を返す。
	 */
	public static String getStepString(){
		String str = "";
		for(int i = 0; i < w_step_arr.length; i++){
			str += String.valueOf(w_step_arr[i]);
			if(i < w_step_arr.length-1)
				str += ", ";
		}
		return str;
	}
	
	/*
	 * "-0.05, 1, -0.01, 0.001"
	 * の形式の文字列を設定する。
	 */
	public static void setStepString(String input){
		String[] s_arr = input.split(",");
		for(int i = 0; i < w_step_arr.length; i++){
			w_step_arr[i] = Double.parseDouble(s_arr[i].trim());
		}
	}
	
	/*
	 * 1つの特徴の重みの更新幅(ステップ幅)の設定
	 */
	public static void setStep(double w, int i){
		w_step_arr[i] = w;
	}

	public double getFeature(int i){
		return p[i];
	}

	public double getKeywordFeature(int i){
		return keyword_p[i];
	}

	//thisのsumが大きければ1、小さければ-1.
	public int compareTo(ExplanationVector o) {
		if(o.getSum() != -INFINITE_VALUE)
			o.calcSum();
		// TODO 自動生成されたメソッド・スタブ
		if(this.sum > o.getSum())
			return 1;
		else if(this.sum < o.getSum())
			return -1;
		return 0;
	}


	//各特長が正(1)の特徴か、負(-1)の特徴か調べるメソッド
	/*
	 * 各特徴は、
	 * 0: 木を構成するノードの数					 Negative
	 * 1: キーワードに一致するラベルの数（もしくは関数の数） Positive
	 * 2: キーワードに一致しないラベルの数 			 Negative
	 * 3: コンテキストに近い関数の数 				 Positive
	 */
	public static boolean isPositive(int i){
		if(i == 0)
			return false;
		else if(i == 1)
			return true;
		else if(i == 2)
			return false;
		else 
			return true;
	}
}
