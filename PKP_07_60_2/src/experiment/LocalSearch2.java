package experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import plugin.activator.Activator;
import plugin.preference.PreferenceInitializer;
import plugin.testSite.TestSite;

import keywordProgramming.ExplanationVector;
import keywordProgramming.KeywordProgramming;
import logging.LogControl;

public class LocalSearch2{

	//評価値の順に並べた関数木の順位
	private int selected_tree_num;

	//重みの更新幅
	private double [] step_w;

	//選択した候補が最上位に来る重みの組み合わせ
	private double [] best_w;

	//計算した結果出た候補の最小順位
	private int min_order;

	private TestSite[] testSites;
	
	//正解が出現したタスク数の最大合計値
	//private int max_sum_ans_tasks;
	
	//スコアの最大値
	private double max_score;
	
	//各タスクの出力候補群のList
	//private List<FunctionTree[]> functionTreeLists = new ArrayList<FunctionTree[]>();
	
	private boolean isOnline;	//オンライン学習か、否か
	
	private boolean flg_log_step_by_step;	//1ステップごとにログを出すか否かのフラグ
	private boolean flg_log_neighbours;		//1ステップごとに各近傍のログを出すか否かのフラグ
		
	private int times_of_steps;	//指定ステップ回数
	
	private int counter_step;	//ステップ数カウンタ
	private int counter_neighbour;	//近傍カウンタ
	
	LogControl logControl;	//ログ出力管理オブジェクト
	
	/*
	 * コンストラクタ
	 */
	public LocalSearch2(TestSite[] sites, boolean isOnline){
		testSites = sites;
		this.isOnline = isOnline;
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		flg_log_step_by_step = store.getBoolean(PreferenceInitializer.LOG_LOCAL_SEARCH_STEP_BY_STEP);
		flg_log_neighbours = store.getBoolean(PreferenceInitializer.LOG_LOCAL_SEARCH_NEIGHBOURS);
		KeywordProgramming.BEST_R = store.getInt(PreferenceInitializer.LOCAL_BEST_R);

		if(isOnline){
			times_of_steps = store.getInt(PreferenceInitializer.LOCAL_ONLINE_NUMBER_OF_STEPS);
		}else{
			times_of_steps = store.getInt(PreferenceInitializer.LOCAL_BATCH_NUMBER_OF_STEPS);
		}
	}
	
	public void run(int numOfSteps, String state, IProgressMonitor monitor){
		
		long start = System.currentTimeMillis();

		//開始時の特徴の重みベクトル
		double []start_w = Arrays.copyOf(ExplanationVector.getWeights(), ExplanationVector.getWeights().length);
		this.step_w = ExplanationVector.getSteps();		//特徴の重みの更新幅
		this.best_w = Arrays.copyOf(start_w, start_w.length);	//選択した候補に最小の順位を与える重みの組

		logControl = new LogControl(LogControl.LOCAL_SEARCH);
		
		//クラス名ごとに分ける。
		String currentClassName = testSites[0].getClassName();
		List<TestSite> list = new ArrayList<TestSite>();
		List<List<TestSite>> list_groupby_class = new ArrayList<List<TestSite>>();
		
		for(TestSite site: testSites){
			if(!site.getClassName().equals(currentClassName)){
				currentClassName = site.getClassName();
				list_groupby_class.add(new ArrayList<TestSite>(list));
				list.clear();
			}
			list.add(site);
			//最後に
			if(site.getId().equals(testSites[testSites.length-1].getId())){
				list_groupby_class.add(new ArrayList<TestSite>(list));
			}
		}
		
		for(List<TestSite> site_list: list_groupby_class){
			//各タスクの出力候補群の中で、正解が出現した順位
			List<Integer> answerOrders = new ArrayList<Integer>();
			runKeywordProgrammingForAllTasks(monitor, site_list, answerOrders, state);
			
			if(monitor != null)
				monitor.worked(4);
			
			//スコアの計算
			max_score = getScoreOfAnswerAppearancedOrder(answerOrders);
		
			List<Result> result_list = new ArrayList<Result>(); 
			for(int i = 0; i < site_list.size(); i++){
				String str = site_list.get(i).getSelectedString();
				int odr = answerOrders.get(i);
				int numKey = site_list.get(i).getNumOfKeywords();
				int numLT = site_list.get(i).getNumOfLocalTypes();
				int numLF = site_list.get(i).getNumOfLocalFunctions();
				//generics を排除
				if(!str.contains("<"))
					result_list.add(new Result(site_list.get(i).getId(), str, odr, numKey, numLT, numLF));
			}
			
			int sum_zero = 0;
			int sum_m_one = 0;
			int sum_others = 0;
			int sumKey = 0;
			int sumLT = 0;
			int sumLF = 0;
			
			List<Result> result_list_others = new ArrayList<Result>(); 
			
	        for(Result result: result_list) {
//				logControl.println(result.fSelectedString + "\t" + result.fAnswerOrder);
				sumKey += result.fNumOfKeywords; 
				sumLT += result.fNumOfLocalTypes;
				sumLF += result.fNumOfLocalFunctions;
				
				if(result.fAnswerOrder == 0)
					sum_zero++;
				else if(result.fAnswerOrder == -1)
					sum_m_one++;
				else{
					sum_others++;
					result_list_others.add(result);
				}
						
	        }
	        
			printResults(site_list, result_list, sum_zero, sum_m_one, sum_others, sumKey,
					sumLT, sumLF, result_list_others);
		}
	}

	private void printResults(List<TestSite> site_list, List<Result> result_list, int sum_zero,
			int sum_m_one, int sum_others, int sumKey, int sumLT, int sumLF,
			List<Result> result_list_others) {
		logControl.println("BEST_R = " + KeywordProgramming.BEST_R);
		logControl.print("現在の特徴の重み = ");		
		for(int i = 0; i < 4; i++){
			logControl.print(String.valueOf(best_w[i]) + ", ");
		}
		logControl.println("");
		logControl.println("");
		
		logControl.println(site_list.get(0).getPackageName());
		logControl.println(site_list.get(0).getClassName());
		
		logControl.println("総数\t" + result_list.size());
		logControl.println("正解出現数\t\t" + (result_list.size() - sum_m_one));
		logControl.println("出現しなかった数\t\t\t" + sum_m_one);
		logControl.println("1番目に出た数\t\t\t\t" + sum_zero);
		logControl.println("2番目以降に出た数\t\t\t\t\t" + sum_others);
		logControl.println("  > 2番目以降に出たもの");
		for(Result result: result_list_others){
			logControl.println(result.fSelectedString + "\t" + result.fAnswerOrder);
		}
		logControl.println("  > 2番目以降に出たもの");

		logControl.println("平均キーワード数\t" + ((double)sumKey/result_list.size()));
		logControl.println("平均ローカル型数\t" + ((double)sumLT/result_list.size()));
		logControl.println("平均ローカル関数数\t" + ((double)sumLF/result_list.size()));
		logControl.println("総キーワード数\t" + (sumKey));
		logControl.println("総ローカル型数\t" + (sumLT));
		logControl.println("総ローカル関数数\t" + (sumLF));
		
		logControl.println("");
		logControl.println("");
	}
	
	/*
	 * すべてのTestSiteに対してKPを行い、
	 * 出力候補のListを取得する。
	 * 
	 * 正解候補が出現した順位も保存する。
	 * 
	 */
	public static void runKeywordProgrammingForAllTasks(IProgressMonitor monitor, List<TestSite> testSites, List<Integer> answerOrders, String state){
		for(int i = 0; i < testSites.size(); i++){
			//ユーザーが処理をキャンセルした
			if(monitor != null && monitor.isCanceled()) {
			    return;
			}
			if(monitor != null)
				monitor.setTaskName("タスクId= "+ testSites.get(i).getId() +"(" +(i+1)+ "/" + testSites.size() + ") について、キーワードプログラミングのアルゴリズムにより出力候補群を生成中");
			testSites.get(i).initKeywordProgramming();
			testSites.get(i).runKeywordProgramming(state);
			answerOrders.add(testSites.get(i).getAnswerNumber(9999));
		}
	}
	
	/*
	 * 選択候補の順位を取得する。
	 */
	public static void getSelectedStringOrders(TestSite[] testSites, List<Integer> answerOrders){
		for(int i = 0; i < testSites.length; i++){
			answerOrders.add(testSites[i].getSelectedStringOrder());
		}
	}
	
	/*
	 * 正解が出現したタスクの数をスコアとした
	 * スコアを返す。
	 */
	public static int getScoreOfAnswerAppearancedTaskNumbar(List<Integer> order_list){
		int score = 0;
		for(Integer i: order_list){
			if(i != -1)
				score++;
		}
		return score;
	}
	
	
	/*
	 * 正解が出現した順位の逆数を各タスク合計したものをスコアとした
	 * スコアを返す。
	 */
	public static double getScoreOfAnswerAppearancedOrder(List<Integer> order_list){
		double score = 0.0;
		for(Integer i: order_list){
			if(i != -1)	//出現しなければスコアは0
				score += 1.0 / (i + 1);
		}
		return score;
	}
	
	/*
	 * 
	 * 一度目に行ったKPによって生成された出力候補群を用いて
	 * 評価値の再計算を行う
	 * 
	 * 逆数スコアを出力する。
	 * 
	 * tmp_w 特徴の重み
	 */
	public static double reCalculateScore(TestSite[] testSites, double[] tmp_w, List<Integer> tmp_order_list){
		
		for(int i = 0; i < testSites.length; i++){
			//各Treeに関してtmp_wのときのスコアを計算。
			testSites[i].reCalculateEvaluationValue();
			//ソートする.
			testSites[i].sortFunctionTrees();
			//正解順位を得る。
//			answerOrders.add(testSites[i].getAnswerNumber(200));
			tmp_order_list.add(testSites[i].getAnswerNumber(200));
		}
		
		//逆数スコアを出力する。
		return getScoreOfAnswerAppearancedOrder(tmp_order_list);
	}
	
}
