package keywordProgramming;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * ExplanationVector��\���N���X
 * @author sayuu
 *
 */
public class ExplanationVector{
	
	//�֐���]����������̐�
	public static final int FEATURE_NUM = 4;

	//�����̏d�݃x�N�g��
	private static double[] w_arr = {-0.05, 1, -0.01, 0.001};		//��s���������l�B
	
	//�d�݂̍X�V��
	private static double[] w_step_arr = {0.04, 0.2, 0.02, 0.02};

	//�L�[���[�h�̌�
	public static int keywords_length;
	
	//�L�[���[�h���Ƃ̓�����
	private double[] keyword_p;
	
	//�S�Ă̗v�f(e0��e_arr)�𑫂�������.�召��r�ɂ͂�����g��.	
	private double sum;
	
	//�����x�N�g��
	private double p[] = new double [FEATURE_NUM];	

	public static double INFINITE_VALUE = 9999.9;
	
	/**
	 * �R���X�g���N�^
	 * @param keywords_length �L�[���[�h��
	 */
	public ExplanationVector(int keywords_length){
		ExplanationVector.keywords_length = keywords_length;
		this.keyword_p = new double[keywords_length];
	}

	/**
	 * �R���X�g���N�^
	 * @param keywords_length �L�[���[�h��
	 * @param sum ExplanationVector�̗v�f�̍��v�l
	 */
	public ExplanationVector(int keywords_length, double sum){
		ExplanationVector.keywords_length = keywords_length;
		this.keyword_p = new double[keywords_length];
		this.sum = sum;
	}

	/**
	 * �R���X�g���N�^
	 * @param e �R�s�[������ExplanationVector
	 */
	public ExplanationVector(ExplanationVector e){
		this.keyword_p = new double[keywords_length];
		substitution(e);
	}

	/**
	 * ��r�̂��߂�
	 * ExplanationVector�̊e�v�f�̍��v�l���v�Z����
	 * 
	 */
	public void calcSum(){
		sum = 0.0;
		//�L�[���[�h�A���[����L�[���[�h�̓����ʂ��Z�o�B
		double sum_k = 0.0;
		for(double k: keyword_p)
			sum_k += k;
		p[1] = sum_k;

		//�e�����̍��v�l�B
		for(int i = 0; i < FEATURE_NUM; i++){
			sum += w_arr[i] * p[i];
		}

	}

	/**
	 * ��r�̂��߂�
	 * ExplanationVector�̊e�v�f�̍��v�l��
	 * �v�Z���A�擾����B
	 * 
	 * @return ExplanationVector�̊e�v�f�̍��v�l
	 */
	public double calcAndGetSum(){
		calcSum();
		return sum;
	}
	
	public double getSum(){
		return sum;
	}

	/**
	 * ExplanationVector�Ɏq�m�[�h�t���������ۂ̒l���v�Z����B
	 * 
	 * @param child�@�t����������q�m�[�h
	 */
	public void add(ExplanationVector child){
		//�L�[���[�h�A���[����L�[���[�h�̓����ʂ��Z�o�B
		double sum_k = 0.0;
		for(int i = 0; i < keyword_p.length; i++){
			keyword_p[i] += child.keyword_p[i];
			if(keyword_p[i] > 1)
				keyword_p[i] = 1;
			sum_k += keyword_p[i];
		}

		//�e�����ʂ����Z(�P���Ȑe�q�̍��v)
		for(int i = 0; i < FEATURE_NUM; i++){
			p[i] += child.p[i];
		}
		p[1] = sum_k;


		calcSum();
	}

	/**
	 * �e��ExplanationVector�Ɏq��t���������ۂ̒l���v�Z����
	 * ���̌��ʂ�ێ������V����ExplanationVector��Ԃ��B
	 * 
	 * @param parent�@�e��ExplanationVector
	 * @param child�@�q��ExplanationVector
	 * @return �v�Z���ʂ�ێ������V����ExplanationVector
	 */
	public static ExplanationVector add(ExplanationVector parent,  ExplanationVector child){
		ExplanationVector new_e = new ExplanationVector(parent);
		//�L�[���[�h�A���[����L�[���[�h�̓����ʂ��Z�o�B
		double sum_k = 0.0;
		for(int i = 0; i < new_e.keyword_p.length; i++){
			new_e.keyword_p[i] = parent.keyword_p[i] + child.keyword_p[i];
			if(new_e.keyword_p[i] > 1)
				new_e.keyword_p[i] = 1;
			sum_k += new_e.keyword_p[i];
		}

		//�e�����ʂ����Z(�P���Ȑe�q�̍��v)
		for(int i = 0; i < FEATURE_NUM; i++){
			new_e.p[i] = parent.p[i] + child.p[i];
		}
		new_e.p[1] = sum_k;
		new_e.calcSum();
		return new_e;
	}

	/**
	 * ���̃I�u�W�F�N�g�Ɉ����ŗ^����ꂽExplanationVector��
	 * �e�v�f�̒l��S�ăR�s�[����B
	 * 
	 * @param e ���e���R�s�[������ExplanationVector
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
	 * ���̓L�[���[�h����ExplanationVector���v�Z����B
	 * 
	 * @param input_words ���̓L�[���[�h��List
	 * @param numOfSameWordsIn �X�̃L�[���[�h�����ꂼ�ꉽ���̓N�G���Ɋ܂܂�Ă��邩��\��HashMap
	 * @param ft ExplanationVector���v�Z������FunctionTree
	 * @return �e�v�f�ɒl�����͂���A�e�v�f�̍��v�l�̌v�Z���Ȃ��ꂽExplanationVector
	 */
	public static ExplanationVector calcExplanationVector(List<String> input_words, HashMap<String, Integer> numOfSameWordsIn, FunctionTree ft){
		ExplanationVector e = new ExplanationVector(input_words.size());
		Function f = ft.getRoot().getFunction();
		
		/*
		 * �]���l�̌v�Z�́A
		 * �؂�root�̕]���l�������v�Z���āA
		 * ���łɋ��܂��Ă���q�̒l�ɑ����΂悢�B
		 */

		//������
		for(int i = 0; i < FEATURE_NUM; i++){
			e.p[i] = 0;
		}

		//�ؑS�̂̃m�[�h��
		e.p[0] = 1;	//root1�Ȃ̂ŁA1.

		//�L�[���[�h�Ɉ�v���郉�x���̐�

		//f.label��input�Ɠ���̃L�[���[�h�����鎞�̌v�Z
		//����P��̏o���������ꂼ��x, y�Ƃ����e_i = max(x/y, 1)�Ƃ���.
		for(int i=0; i < input_words.size(); i++){
			for(String flab: f.getLabels()){
				if(input_words.get(i).equals(flab)){
					e.keyword_p[i] = (double)(ft.numOfSameWordsFunc.get(flab)) / numOfSameWordsIn.get(flab);
					if(e.keyword_p[i] > 1)
						e.keyword_p[i] = 1;
				}
			}
		}


		//�L�[���[�h�Ɉ�v���Ȃ����x���̐�
		for(String flab: f.getLabels()){
			if(numOfSameWordsIn.containsKey(flab) == false){
				e.p[2]++;
			}
		}

		//����f�����[�J���ϐ��A�����o�ϐ��i�֐��j�Ȃ��e_0��+0.001����(f�̓R���e�L�X�g�ɋ߂������]�܂�������)
		//�i��ŏ����j
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
	 * �̌`���̕������Ԃ��B
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
	 * �̌`���̕������ݒ肷��B
	 */
	public static void setWeightString(String input){
		String[] s_arr = input.split(",");
		for(int i = 0; i < w_arr.length; i++){
			w_arr[i] = Double.parseDouble(s_arr[i].trim());
		}
	}
	
	/*
	 * 1�̓����̏d�݂̐ݒ�
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
	 * �̌`���̕������Ԃ��B
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
	 * �̌`���̕������ݒ肷��B
	 */
	public static void setStepString(String input){
		String[] s_arr = input.split(",");
		for(int i = 0; i < w_step_arr.length; i++){
			w_step_arr[i] = Double.parseDouble(s_arr[i].trim());
		}
	}
	
	/*
	 * 1�̓����̏d�݂̍X�V��(�X�e�b�v��)�̐ݒ�
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

	//this��sum���傫�����1�A���������-1.
	public int compareTo(ExplanationVector o) {
		if(o.getSum() != -INFINITE_VALUE)
			o.calcSum();
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		if(this.sum > o.getSum())
			return 1;
		else if(this.sum < o.getSum())
			return -1;
		return 0;
	}


	//�e��������(1)�̓������A��(-1)�̓��������ׂ郁�\�b�h
	/*
	 * �e�����́A
	 * 0: �؂��\������m�[�h�̐�					 Negative
	 * 1: �L�[���[�h�Ɉ�v���郉�x���̐��i�������͊֐��̐��j Positive
	 * 2: �L�[���[�h�Ɉ�v���Ȃ����x���̐� 			 Negative
	 * 3: �R���e�L�X�g�ɋ߂��֐��̐� 				 Positive
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
