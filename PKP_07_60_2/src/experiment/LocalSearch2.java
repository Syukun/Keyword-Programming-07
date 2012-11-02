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

	//�]���l�̏��ɕ��ׂ��֐��؂̏���
	private int selected_tree_num;

	//�d�݂̍X�V��
	private double [] step_w;

	//�I��������₪�ŏ�ʂɗ���d�݂̑g�ݍ��킹
	private double [] best_w;

	//�v�Z�������ʏo�����̍ŏ�����
	private int min_order;

	private TestSite[] testSites;
	
	//�������o�������^�X�N���̍ő升�v�l
	//private int max_sum_ans_tasks;
	
	//�X�R�A�̍ő�l
	private double max_score;
	
	//�e�^�X�N�̏o�͌��Q��List
	//private List<FunctionTree[]> functionTreeLists = new ArrayList<FunctionTree[]>();
	
	private boolean isOnline;	//�I�����C���w�K���A�ۂ�
	
	private boolean flg_log_step_by_step;	//1�X�e�b�v���ƂɃ��O���o�����ۂ��̃t���O
	private boolean flg_log_neighbours;		//1�X�e�b�v���ƂɊe�ߖT�̃��O���o�����ۂ��̃t���O
		
	private int times_of_steps;	//�w��X�e�b�v��
	
	private int counter_step;	//�X�e�b�v���J�E���^
	private int counter_neighbour;	//�ߖT�J�E���^
	
	LogControl logControl;	//���O�o�͊Ǘ��I�u�W�F�N�g
	
	/*
	 * �R���X�g���N�^
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

		//�J�n���̓����̏d�݃x�N�g��
		double []start_w = Arrays.copyOf(ExplanationVector.getWeights(), ExplanationVector.getWeights().length);
		this.step_w = ExplanationVector.getSteps();		//�����̏d�݂̍X�V��
		this.best_w = Arrays.copyOf(start_w, start_w.length);	//�I���������ɍŏ��̏��ʂ�^����d�݂̑g

		logControl = new LogControl(LogControl.LOCAL_SEARCH);
		
		//�N���X�����Ƃɕ�����B
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
			//�Ō��
			if(site.getId().equals(testSites[testSites.length-1].getId())){
				list_groupby_class.add(new ArrayList<TestSite>(list));
			}
		}
		
		for(List<TestSite> site_list: list_groupby_class){
			//�e�^�X�N�̏o�͌��Q�̒��ŁA�������o����������
			List<Integer> answerOrders = new ArrayList<Integer>();
			runKeywordProgrammingForAllTasks(monitor, site_list, answerOrders, state);
			
			if(monitor != null)
				monitor.worked(4);
			
			//�X�R�A�̌v�Z
			max_score = getScoreOfAnswerAppearancedOrder(answerOrders);
		
			List<Result> result_list = new ArrayList<Result>(); 
			for(int i = 0; i < site_list.size(); i++){
				String str = site_list.get(i).getSelectedString();
				int odr = answerOrders.get(i);
				int numKey = site_list.get(i).getNumOfKeywords();
				int numLT = site_list.get(i).getNumOfLocalTypes();
				int numLF = site_list.get(i).getNumOfLocalFunctions();
				//generics ��r��
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
		logControl.print("���݂̓����̏d�� = ");		
		for(int i = 0; i < 4; i++){
			logControl.print(String.valueOf(best_w[i]) + ", ");
		}
		logControl.println("");
		logControl.println("");
		
		logControl.println(site_list.get(0).getPackageName());
		logControl.println(site_list.get(0).getClassName());
		
		logControl.println("����\t" + result_list.size());
		logControl.println("�����o����\t\t" + (result_list.size() - sum_m_one));
		logControl.println("�o�����Ȃ�������\t\t\t" + sum_m_one);
		logControl.println("1�Ԗڂɏo����\t\t\t\t" + sum_zero);
		logControl.println("2�Ԗڈȍ~�ɏo����\t\t\t\t\t" + sum_others);
		logControl.println("  > 2�Ԗڈȍ~�ɏo������");
		for(Result result: result_list_others){
			logControl.println(result.fSelectedString + "\t" + result.fAnswerOrder);
		}
		logControl.println("  > 2�Ԗڈȍ~�ɏo������");

		logControl.println("���σL�[���[�h��\t" + ((double)sumKey/result_list.size()));
		logControl.println("���σ��[�J���^��\t" + ((double)sumLT/result_list.size()));
		logControl.println("���σ��[�J���֐���\t" + ((double)sumLF/result_list.size()));
		logControl.println("���L�[���[�h��\t" + (sumKey));
		logControl.println("�����[�J���^��\t" + (sumLT));
		logControl.println("�����[�J���֐���\t" + (sumLF));
		
		logControl.println("");
		logControl.println("");
	}
	
	/*
	 * ���ׂĂ�TestSite�ɑ΂���KP���s���A
	 * �o�͌���List���擾����B
	 * 
	 * ������₪�o���������ʂ��ۑ�����B
	 * 
	 */
	public static void runKeywordProgrammingForAllTasks(IProgressMonitor monitor, List<TestSite> testSites, List<Integer> answerOrders, String state){
		for(int i = 0; i < testSites.size(); i++){
			//���[�U�[���������L�����Z������
			if(monitor != null && monitor.isCanceled()) {
			    return;
			}
			if(monitor != null)
				monitor.setTaskName("�^�X�NId= "+ testSites.get(i).getId() +"(" +(i+1)+ "/" + testSites.size() + ") �ɂ��āA�L�[���[�h�v���O���~���O�̃A���S���Y���ɂ��o�͌��Q�𐶐���");
			testSites.get(i).initKeywordProgramming();
			testSites.get(i).runKeywordProgramming(state);
			answerOrders.add(testSites.get(i).getAnswerNumber(9999));
		}
	}
	
	/*
	 * �I�����̏��ʂ��擾����B
	 */
	public static void getSelectedStringOrders(TestSite[] testSites, List<Integer> answerOrders){
		for(int i = 0; i < testSites.length; i++){
			answerOrders.add(testSites[i].getSelectedStringOrder());
		}
	}
	
	/*
	 * �������o�������^�X�N�̐����X�R�A�Ƃ���
	 * �X�R�A��Ԃ��B
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
	 * �������o���������ʂ̋t�����e�^�X�N���v�������̂��X�R�A�Ƃ���
	 * �X�R�A��Ԃ��B
	 */
	public static double getScoreOfAnswerAppearancedOrder(List<Integer> order_list){
		double score = 0.0;
		for(Integer i: order_list){
			if(i != -1)	//�o�����Ȃ���΃X�R�A��0
				score += 1.0 / (i + 1);
		}
		return score;
	}
	
	/*
	 * 
	 * ��x�ڂɍs����KP�ɂ���Đ������ꂽ�o�͌��Q��p����
	 * �]���l�̍Čv�Z���s��
	 * 
	 * �t���X�R�A���o�͂���B
	 * 
	 * tmp_w �����̏d��
	 */
	public static double reCalculateScore(TestSite[] testSites, double[] tmp_w, List<Integer> tmp_order_list){
		
		for(int i = 0; i < testSites.length; i++){
			//�eTree�Ɋւ���tmp_w�̂Ƃ��̃X�R�A���v�Z�B
			testSites[i].reCalculateEvaluationValue();
			//�\�[�g����.
			testSites[i].sortFunctionTrees();
			//�������ʂ𓾂�B
//			answerOrders.add(testSites[i].getAnswerNumber(200));
			tmp_order_list.add(testSites[i].getAnswerNumber(200));
		}
		
		//�t���X�R�A���o�͂���B
		return getScoreOfAnswerAppearancedOrder(tmp_order_list);
	}
	
}
