package plugin.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import plugin.activator.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public static String BEST_R = "BEST_R";
	public static String HEIGHT = "HEIGHT";
	
	//内部DB以外にプロジェクト中に含まれるクラスもインポートするかどうか
	public static String IMPORT_TYPES_FROM = "IMPORT_TYPES_FROM";
		//自分のクラスのみインポート
	public static String IMPORT_TYPES_FROM_MY_CLASS = "IMPORT_TYPES_FROM_MY_CLASS";
		//自分の所属するパッケージからインポート
	public static String IMPORT_TYPES_FROM_MY_PACKAGE = "IMPORT_TYPES_FROM_MY_PACKAGE";
		//自分の所属するプロジェクトからインポート
	public static String IMPORT_TYPES_FROM_MY_PROJECT = "IMPORT_TYPES_FROM_MY_PROJECT";
	
	public static String INITIAL_WEIGHT_0 = "INITIAL_WEIGHT_0";
	public static String INITIAL_WEIGHT_1 = "INITIAL_WEIGHT_1";
	public static String INITIAL_WEIGHT_2 = "INITIAL_WEIGHT_2";
	public static String INITIAL_WEIGHT_3 = "INITIAL_WEIGHT_3";
	
	//ローカルサーチ関連
	public static String LOCAL_BEST_R = "LOCAL_BEST_R";	//ローカルサーチ時のBEST_R
	public static String LOCAL_STEP_WIDTH_0 = "LOCAL_STEP_WIDTH_0";
	public static String LOCAL_STEP_WIDTH_1 = "LOCAL_STEP_WIDTH_1";
	public static String LOCAL_STEP_WIDTH_2 = "LOCAL_STEP_WIDTH_2";
	public static String LOCAL_STEP_WIDTH_3 = "LOCAL_STEP_WIDTH_3";
		//ローカルサーチ　オンライン学習関連
	public static String LOCAL_ONLINE_FLAG = "LOCAL_ONLINE_FLAG";
	public static String LOCAL_ONLINE_NUMBER_OF_PAST_LOG = "LOCAL_ONLINE_NUMBER_OF_PAST_LOG";
	public static String LOCAL_ONLINE_NUMBER_OF_STEPS = "LOCAL_ONLINE_NUMBER_OF_STEPS";
		//ローカルサーチ　バッチ学習関連
	public static String LOCAL_BATCH_FLAG_CONVERGENCE = "LOCAL_BATCH_FLAG_CONVERGENCE";	//収束するまで実行するか
	public static String LOCAL_BATCH_NUMBER_OF_STEPS = "LOCAL_BATCH_NUMBER_OF_STEPS";

	//Log出力関連
	public static String LOG_KEYWORD_PROGRAMMING = "LOG_KEYWORD_PROGRAMMING";
	public static String LOG_KEYWORD_PROGRAMMING_BASIC = "LOG_KEYWORD_PROGRAMMING_BASIC";
	public static String LOG_KEYWORD_PROGRAMMING_TIME = "LOG_KEYWORD_PROGRAMMING_TIME";
	public static String LOG_KEYWORD_PROGRAMMING_FUNCTIONS = "LOG_KEYWORD_PROGRAMMING_FUNCTIONS";
	public static String LOG_KEYWORD_PROGRAMMING_TYPES = "LOG_KEYWORD_PROGRAMMING_TYPES";
	public static String LOG_KEYWORD_PROGRAMMING_RESULTS = "LOG_KEYWORD_PROGRAMMING_RESULTS";
	
	public static String LOG_LOCAL_SEARCH = "LOG_LOCAL_SEARCH";
	public static String LOG_LOCAL_SEARCH_STEP_BY_STEP = "LOG_LOCAL_SEARCH_STEP_BY_STEP";
	public static String LOG_LOCAL_SEARCH_NEIGHBOURS = "LOG_LOCAL_SEARCH_NEIGHBOURS";
	
	public static String LOG_GRID_SEARCH = "LOG_GRID_SEARCH";
	
	public static String LOG_EXPORT_FLAG = "LOG_EXPORT_FLAG";
	public static String LOG_EXPORT_FILE = "LOG_EXPORT_FILE";
	public static final String LogFileFolder = "./KeywordProgramming/logFile/";

	
	//グリッドサーチ関連
	public static String GRID_BEST_R = "GRID_BEST_R";	//グリッドサーチ時のBEST_R

	public static String GRID_COUNT_FOR_KP = "GRID_COUNT_FOR_KP";
		//特徴1のみ固定値。指定できる。
	public static String GRID_WEIGHT_1_CONSTANT = "GRID_WEIGHT_1_CONSTANT";

	public static String GRID_START_WEIGHT_0 = "GRID_START_WEIGHT_0";
	public static String GRID_START_WEIGHT_1 = "GRID_START_WEIGHT_1";
	public static String GRID_START_WEIGHT_2 = "GRID_START_WEIGHT_2";
	public static String GRID_START_WEIGHT_3 = "GRID_START_WEIGHT_3";
	
	public static String GRID_END_WEIGHT_0 = "GRID_END_WEIGHT_0";
	public static String GRID_END_WEIGHT_1 = "GRID_END_WEIGHT_1";
	public static String GRID_END_WEIGHT_2 = "GRID_END_WEIGHT_2";
	public static String GRID_END_WEIGHT_3 = "GRID_END_WEIGHT_3";
	
	public static String GRID_STEP_WIDTH_0 = "GRID_STEP_WIDTH_0";
	public static String GRID_STEP_WIDTH_1 = "GRID_STEP_WIDTH_1";
	public static String GRID_STEP_WIDTH_2 = "GRID_STEP_WIDTH_2";
	public static String GRID_STEP_WIDTH_3 = "GRID_STEP_WIDTH_3";
	
	//タスク追加関連
	//現在のキーワードプログラミングでは正解を出すことができないタスクを排除して一括追加する。
	
	//匿名クラス定義 排除 AnonymousClassDeclaration
	public static String TASK_IMPORT_EXCLUDE_ANONYMOUS_CLASS_DECLARATION = "EXCLUDE_ANONYMOUS_CLASS_DECLARATION";
	//出力関数木の引数に定数が含まれている　LiteralsInArguments
	public static String TASK_IMPORT_EXCLUDE_LITERALS = "TASK_IMPORT_EXCLUDE_LITERALS";
	//関数のラベルについて、連続して大文字が続くものが存在するとき。UpperCaseContiguous
	public static String TASK_IMPORT_EXCLUDE_CONTIGUOUS_UPPER_CASES = "TASK_IMPORT_EXCLUDE_CONTIGUOUS_UPPER_CASES";
	//ロケーションが型キャスト（ダウンキャスト）のタスクを除外.
	public static String TASK_IMPORT_EXCLUDE_TYPE_DOWN_CAST = "TASK_IMPORT_EXCLUDE_TYPE_DOWN_CAST";
	
	@Override
	public void initializeDefaultPreferences() {
		// TODO Auto-generated method stub
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.setDefault(BEST_R, 50);
		store.setDefault(HEIGHT, 3);
		
		store.setDefault(IMPORT_TYPES_FROM, IMPORT_TYPES_FROM_MY_CLASS);
//		store.setDefault(IMPORT_TYPES_FROM_MY_CLASS, );
//		store.setDefault(IMPORT_TYPES_FROM_MY_PACKAGE, false);
//		store.setDefault(IMPORT_TYPES_FROM_MY_PROJECT, false);

		//"（入力例：\"-0.05, 1, -0.01, 0.001\"）\n"+
		
		store.setDefault(INITIAL_WEIGHT_0, 0.05);
		store.setDefault(INITIAL_WEIGHT_1, 1.0);
		store.setDefault(INITIAL_WEIGHT_2, 0.01);
		store.setDefault(INITIAL_WEIGHT_3, 0.001);
		
		//"（入力例：\"0.04, 0.2, 0.02, 0.02\"）\n"+

		store.setDefault(LOCAL_BEST_R, 10000);

		store.setDefault(LOCAL_STEP_WIDTH_0, 0.04);
		store.setDefault(LOCAL_STEP_WIDTH_1, 0.2);
		store.setDefault(LOCAL_STEP_WIDTH_2, 0.02);
		store.setDefault(LOCAL_STEP_WIDTH_3, 0.02);
		
		store.setDefault(LOCAL_ONLINE_FLAG, true);
		store.setDefault(LOCAL_ONLINE_NUMBER_OF_PAST_LOG, 3);
		store.setDefault(LOCAL_ONLINE_NUMBER_OF_STEPS, 1);
		
		store.setDefault(LOCAL_BATCH_FLAG_CONVERGENCE, false);
		store.setDefault(LOCAL_BATCH_NUMBER_OF_STEPS, 10);
		
		store.setDefault(LOG_KEYWORD_PROGRAMMING, false);
		store.setDefault(LOG_KEYWORD_PROGRAMMING_BASIC, true);
		store.setDefault(LOG_KEYWORD_PROGRAMMING_TIME, false);
		store.setDefault(LOG_KEYWORD_PROGRAMMING_FUNCTIONS, false);
		store.setDefault(LOG_KEYWORD_PROGRAMMING_RESULTS, false);
		store.setDefault(LOG_KEYWORD_PROGRAMMING_TYPES, false);
		
		store.setDefault(LOG_LOCAL_SEARCH, true);
		store.setDefault(LOG_LOCAL_SEARCH_STEP_BY_STEP, false);
		store.setDefault(LOG_LOCAL_SEARCH_NEIGHBOURS, false);
		
		store.setDefault(LOG_GRID_SEARCH, true);
		
		store.setDefault(LOG_EXPORT_FLAG, false);
		store.setDefault(LOG_EXPORT_FILE, LogFileFolder);
		
		store.setDefault(GRID_BEST_R, 10000);

		store.setDefault(GRID_WEIGHT_1_CONSTANT, 1.0);
		
		store.setDefault(GRID_START_WEIGHT_0, 5.0);
		store.setDefault(GRID_START_WEIGHT_1, 1.0);
		store.setDefault(GRID_START_WEIGHT_2, 5.0);
		store.setDefault(GRID_START_WEIGHT_3, 0.0);
		
		store.setDefault(GRID_END_WEIGHT_0, 0.0);
		store.setDefault(GRID_END_WEIGHT_1, 1.0);
		store.setDefault(GRID_END_WEIGHT_2, 0.0);
		store.setDefault(GRID_END_WEIGHT_3, 0.5);
		
		//"（入力例：\"0.04, 0.2, 0.02, 0.02\"）\n"+
		
		store.setDefault(GRID_STEP_WIDTH_0, 0.1);
		store.setDefault(GRID_STEP_WIDTH_1, 0.1);
		store.setDefault(GRID_STEP_WIDTH_2, 0.1);
		store.setDefault(GRID_STEP_WIDTH_3, 0.1);
		
		store.setDefault(GRID_COUNT_FOR_KP, 100);
		
		store.setDefault(TASK_IMPORT_EXCLUDE_ANONYMOUS_CLASS_DECLARATION, true);
		store.setDefault(TASK_IMPORT_EXCLUDE_CONTIGUOUS_UPPER_CASES, true);
		store.setDefault(TASK_IMPORT_EXCLUDE_LITERALS, true);
		store.setDefault(TASK_IMPORT_EXCLUDE_TYPE_DOWN_CAST, true);

	}

}
