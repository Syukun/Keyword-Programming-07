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
 * �L�[���[�h�v���O���~���O�̎�v�ȂR�̃A���S���Y��
 *  ���I�v��@(DynamicProgram)�A
 *  GetBestExplTreeForFunc�A
 *  ExtractTree
 * ���L�q�����N���X�B
 * 
 * @author sayuu
 *
 */
public class KeywordProgramming {

	//���I�v��@�̕\�̍���
	public static int HEIGHT = 3;
	
	//���I�v��@�̕\�̂P�̌�_�ɕێ�����u���v�֐��؂̌�
	public static int BEST_R = 25;

	//�����f�[�^�x�[�X�̃t�@�C�����B�@�^�B
	public static final String SUB_CLASS_FILE_NAME = "sub_class.txt";
	
	//�����f�[�^�x�[�X�̃t�@�C�����B�@�֐��B
	public static final String FUNCTION_FILE_NAME = "function.txt";

	//���I�v��@�ɂ���č쐬�����\��\���I�u�W�F�N�g�AbestRoots
	private static BestRoots bestRoots;
	
	//�A���S���Y���Ɏg�p�����S�Ă̌^��ێ�����TreeMap
	private static TreeMap<String, Type> types = new TreeMap<String, Type>();
	
	//�A���S���Y���Ɏg�p�����S�Ă̊֐���ێ�����ArrayList
	private static ArrayList<Function> functions = new ArrayList<Function>();
	
	//�����f�[�^�x�[�X�ɑ��݂���S�Ă̌^��ێ�����TreeMap
	private static TreeMap<String, Type> original_types = new TreeMap<String, Type>();
	
	//�����f�[�^�x�[�X�ɑ��݂���S�Ă̊֐���ێ�����ArrayList
	private static ArrayList<Function> original_functions = new ArrayList<Function>(1);

	/*
	 * �ŏI�I�ȏo�͊֐��؂̗�B
	 * �L�[�����̊֐��؂̏��ʁB
	 * �l�����̊֐��؁B
	 * �]���l���Ƀ\�[�g����Ă���B
	 */
	private static FunctionTree[] outputFunctionTrees;

	//���̓L�[���[�h��List
	private static List<String> input_keywords;
	
	//input�̒P��̓��A����̒P�ꂻ�ꂼ��̌�
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

	//Classes�t�@�C����ǂݍ���ŁA���X�g�Ɋi�[
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
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}

	}

	//Functions�t�@�C����ǂݍ���ŁA���X�g�Ɋi�[
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
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}

	}
	//�t�@�C���̓ǂݍ���
	public static void loadFiles(BufferedReader c_r, BufferedReader f_r){
		String s;

		//classes�t�@�C���̓ǂݍ���
		try {
			while ((s = c_r.readLine()) != null) {
				Type t = new Type(s);
				types.put(t.getName(), t);
				original_types.put(t.getName(), t);
			}
			c_r.close();
		} catch (IOException e1) {
			// TODO �����������ꂽ catch �u���b�N
			e1.printStackTrace();
		}
		//functions�t�@�C���̓ǂݍ���
		try {
			while ((s = f_r.readLine()) != null) {
				Function f = new Function(s);
				functions.add(f);
				original_functions.add(f);
			}
			f_r.close();
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
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


	//�v���O�C���̒�����L�[���[�h�v���O���~���O�𓮂����B
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
				//�L�[������Ƃ��A
				//subtype��ǉ�����B
				types.get(t.getName()).addSubTypes(t.getSubTypes());
			}
		}

		if(localFunctions != null)
		for(String s: localFunctions){
			Function f = new Function(s);
			//�V�����֐����������ꍇ�̂݁A
			if(functions.add(f) == true){
				new_funcs.add(f);
				//�V�����o�������N���X�̃`�F�b�N

				//�Ԃ�l�̃`�F�b�N
				String ret = f.getReturnType();
				Type t = new Type(ret);
				if(!types.containsKey(t.getName())){
					types.put(t.getName(), t);
					new_types.add(t);
				}else{
					//�L�[������Ƃ��A
					//subtype��ǉ�����B
					types.get(t.getName()).addSubTypes(t.getSubTypes());
				}

				//�p�����[�^�̃`�F�b�N
				if(f.getParameters() != null)
				for(String param: f.getParameters()){//������Βǉ�
					Type tt = new Type(param);
					if(!types.containsKey(tt.getName())){
						types.put(tt.getName(), tt);
						new_types.add(tt);
					}else{
						//�L�[������Ƃ��A
						//subtype��ǉ�����B
						types.get(tt.getName()).addSubTypes(tt.getSubTypes());
					}
				}
			}
		}

		//�L�[���[�h�̏����B
		inputKeywords(keywords);
		
		//bestRoots�𐶐�����B
		bestRoots = new BestRoots(types);

		//DynamicProgram �A���S���Y�������s
		DynamicProgram();
		
		//ExtractTree �A���S���Y�������s
		outputFunctionTrees = ExtractTree(desiredType);

		long stop = System.currentTimeMillis();
		
		//���ʂ��R���\�[���ɕ\������B
		printResult(desiredType, new_types, new_funcs, stop-start, state);
		
		//bestRoots�̒��g��\��
//		printBestRoots();
//		printFunctionsList(functions);
		//���O�o�͗p�t�@�C��
		//createLogFiles(new_types, new_funcs);
	
		//static�ȃt�B�[���h��S�ăN���A����
		clearStaticFields();

		return outputFunctionTrees;
	}

		
	/*
	 * Type��Function�̃��O���o�͂���B
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
	 * �f�o�b�O�p ���ʕ\�����\�b�h
	 */
	
	private static void printResult(String desiredType, ArrayList<Type> new_types, ArrayList<Function> new_funcs, long time, String state) {
		LogControl logControl = new LogControl(LogControl.KP);
		
		//�R�[�h�R���v���[�V�����̂Ƃ��̓��O��Ԃ̕\�����s���B�T�[�`���[�h�̂Ƃ��̓��O��Ԃ̕\���𖈉�s��Ȃ��B�ז��Ȃ̂ŁB
		if(state.equals(KpRunningState.CODE_COMPLETION))
			logControl.printLogState();
		
		//new_types�Ɋ܂܂��󕶎���폜
		new_types.remove(new Type(""));
		
		logControl.println(">>> start keyword programming >>>");
		logControl.println("");

		logControl.println(" >> ��{��� >>", LogControl.KP_BASIC);
		logControl.println("  ���s�ɂ�����������= " + time + " �~���b�BKeywordProgramming.printResult", LogControl.KP_BASIC);
		logControl.println("  BEST_R =" + BEST_R);
		logControl.print("  ���̓L�[���[�h= ", LogControl.KP_BASIC);
		for(String w: input_keywords)
			logControl.print(w + ", ", LogControl.KP_BASIC);
		logControl.println("");
		logControl.println("  �o�͌��̖]�܂����Ԃ�l�̌^= " + desiredType, LogControl.KP_BASIC);
		logControl.println("  ���̓L�[���[�h��=" + input_keywords.size());
		logControl.println("  ���^��= " + types.size(), LogControl.KP_BASIC);
		if(new_types != null)
			logControl.println("  ���[�J���̌^��= " + new_types.size(), LogControl.KP_BASIC);
		logControl.println("  ���֐���= " + functions.size(), LogControl.KP_BASIC);
		if(new_funcs != null)
			logControl.println("  ���[�J���̊֐���= " + new_funcs.size(), LogControl.KP_BASIC);
		if(outputFunctionTrees != null)
			logControl.println("  �������ꂽ�o�͂̑���= " + outputFunctionTrees.length, LogControl.KP_BASIC);
		
		logControl.println("  �����̏d�݂̑g= (" + ExplanationVector.getWeightString() + ")", LogControl.KP_BASIC);
		
		logControl.println(" << ��{��� <<", LogControl.KP_BASIC);
		logControl.println("", LogControl.KP_BASIC);
		
		if(new_types != null)
			logControl.println("  ���[�J���̌^��= " + new_types.size(), LogControl.KP_TYPES);
		logControl.println(" >> ���[�J���̌^�ꗗ >>", LogControl.KP_TYPES);
		if(new_types != null){
			for(Type t: new_types){
				logControl.println("  " + t.toDBString(), LogControl.KP_TYPES);
			}
		}
		logControl.println(" << ���[�J���̌^�ꗗ <<", LogControl.KP_TYPES);
		logControl.println("", LogControl.KP_TYPES);

		
		if(new_funcs != null)
			logControl.println("  ���[�J���̊֐���= " + new_funcs.size(), LogControl.KP_FUNCTIONS);
		logControl.println(" >> ���[�J���̊֐��ꗗ >> �o�͌`���F[�e�N���X��, isStatic, isFinal, type(field or constructor or method or localvariable), �Ԃ�l�̌^, ���O, ���x��(��؂蕶��;), �����̌^(���ł�) ]", LogControl.KP_FUNCTIONS);
		if(new_funcs != null){
			for(Function f: new_funcs){
				logControl.println("  " + f.toDBString(), LogControl.KP_FUNCTIONS);
			}
		}
		logControl.println(" << ���[�J���̊֐��ꗗs << �o�͌`���F[�e�N���X��, isStatic, isFinal, type(field or constructor or method or localvariable), �Ԃ�l�̌^, ���O, ���x��(��؂蕶��;), �����̌^(���ł�) ]", LogControl.KP_FUNCTIONS);
		logControl.println("", LogControl.KP_FUNCTIONS);

		logControl.println(" >> �o�͌��ꗗ >> �o�͌`���F[�]���l, p(4�̓�����), e_i(�����̓L�[���[�h���ɓ�����), �o�͕�����]", LogControl.KP_RESULTS);
		for(FunctionTree t:outputFunctionTrees){
			if(t != null){
				logControl.println("  " + t.toEvalString() + t.toCompleteMethodString(), LogControl.KP_RESULTS);
//					logControl.out(t.toLogDBString());
			}
		}
		logControl.println(" << �o�͌��ꗗ << �o�͌`���F[�]���l, p(4�̓�����), e_i(�����̓L�[���[�h���ɓ�����), �o�͕�����]", LogControl.KP_RESULTS);
		logControl.println("", LogControl.KP_RESULTS);

		logControl.println("<<< end keyword programming <<<");
		logControl.close();
	}

	/**
	 * ���̃T�C�Y���L�����Ă����āA
	 * ���̃T�C�Y�ɖ߂��Ηǂ������ł͂Ȃ��̂��H
	 *
	 * HashSet�ɂ����
	 * �N���A�̕K�v���Ȃ��Ȃ�I
	 *
	 * static field �̃N���A
	 *
	 * 1:types���X�g��functions���X�g�̃N���A
	 *   �V���ɒǉ��������[�J�����̍폜
	 *
	 * 2:numOfSameWords�̃N���A
	 *
	 * @param localTypeSize
	 * @param localFunctionSize
	 */

	/*
	 * retainAll: �d�Ȃ��Ă�����e�̂ݕێ����A�d�Ȃ�Ȃ����e�͍폜����B
	 */
	public static void clearStaticFields(){
		types.clear();
		types.putAll(original_types);
        //functions.retainAll(original_functions);
		functions.clear();
		functions.addAll(original_functions);
		numOfSameWords.clear();
		bestRoots.clearTable();//�������Ă����Ȃ��Ă��ς��Ȃ��悤���B
//		System.gc();
		//used memory�̑���(�P�ʁFKByte)
//		Runtime rt = Runtime.getRuntime();
//		long usedMemory = rt.totalMemory()/1024 - rt.freeMemory()/1024;
//		System.out.println("�g�p��������= " + usedMemory + "KByte");
	}

	//Classes���X�g�̕\��
	public static void printClassList(ArrayList<String> list){
		for(String s: list){
			System.out.println(s);
		}
	}
	//Functions���X�g�̕\��
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
					if(r != null){//����null�`�F�b�N�͕K�v
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
	 * �L�[���[�h�̂��Ă̏����B
	 */
	public static void inputKeywords(String s){
		
		if(s.equals("")){
			input_keywords = new ArrayList<String>();
			return;
		}
			
		//�������������������
		String s_lowerCase = s.toLowerCase();
		//keyword�ɕ���
		input_keywords = Arrays.asList(s_lowerCase.split("[ �@\t]"));

		//keyword�𓯈�P�ꂲ�Ƃɂ��̌����J�E���g����
		for(int i= 0; i < input_keywords.size(); i++){
			String word = input_keywords.get(i);
			int count = 1;
			if(numOfSameWords.containsKey(word)){
				//�L�[�����łɂ���Ƃ��A�ȑO�܂ł̐��ƂP�𑫂�
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
		 * "java.lang.Object"��subType���쐬�B
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
					
							// if e > -�� then bestRoots(t, i) = bestRoots(t, i)U(e, f)
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
	 *	�Œ�ł�Root�݂̂�Tree�͎擾�ł���
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
		 * �e�N���X��"this"���A��static�̏ꍇ�́A���V�[�o�����ł��N���ł���悤�ɂ���B
		 */
		boolean this_flg = false;
		if(f.getParentClass().equals("this") && f.isStatic() == false){
			this_flg = true;
		}
			
		FunctionTree best_tree = new FunctionTree(f, input_keywords, numOfSameWords);//Root�݂̂�Tree
		ExplanationVector e_cumulative = new ExplanationVector(best_tree.e_vec);//best_tree��e_vec���R�s�[����new
		
		//for each p in params(f)
		
		if(f.getParameters() == null)
			return best_tree;	//�֐��Ɉ������Ȃ��Ƃ��Aroot�݂̂�Ԃ�
		else if(h_max == 0)
			return null;	//�֐��Ɉ����͂��邪�A������0�̂Ƃ��Anull��Ԃ�
		
		/*
		 * this_flg=true�̂Ƃ��́A����0�A�������V�[�o�݂̂̂Ƃ�������B
		 */
		if(this_flg == true && f.getParameters().length == 1)
			return best_tree;
		
		for(String param: f.getParameters()){//�����̏��Ԓʂ�Ƀ��[�v����
			//e_best = (-��, 0, 0, 0, ...) 
			ExplanationVector e_best = new ExplanationVector(input_keywords.size(), -ExplanationVector.INFINITE_VALUE);//�����	
			FunctionTree param_tree = null;
			//for each 1 <= i <= h
			for(int i = 0; i < h_max; i++){//h_max=0�̏ꍇ�͂��̃��[�v�͎��s����Ȃ�
				//for each (e', f') in bestRoots(p, i)
				FunctionTree[] trees = bestRoots.getRoots(param, i);
				if(trees != null){
					for(FunctionTree t: trees){
						if(t != null){
							//if e_cumulative + e' > e_best
							//    e_best = e_cumulative + e'
							
							ExplanationVector tmp = ExplanationVector.add(e_cumulative, t.e_vec);
							if(tmp.compareTo(e_best) == 1){
								e_best.substitution(tmp);//���
								param_tree = t;
							}

						}
					}
				}
			}
			//e_cumulative = e_best
			e_cumulative.substitution(e_best);
			
			if(param_tree == null)
				return null;	//�������P�ł����܂�Ȃ����,����root�֐��͑I�����Ȃ��Bnull��Ԃ��B
			else
				best_tree.addChild(param_tree);
		}

		return best_tree;
	}

	/**
	 *
	 * ���I�v��@�̕\��
	 * �]�܂����Ԃ�l�̌^��Ԃ��֐��؂�
	 * ����1����HEIGHT�܂őS�Ď擾���A
	 * �]���l�Ń\�[�g���ĕԂ��֐�
	 *
	 * @param desiredType �]�܂����Ԃ�l�̌^
	 * @return outputTrees �]���l�Ń\�[�g���ꂽ�֐��؂̃��X�g
	 */
	public static FunctionTree[] ExtractTree(String desiredType){

		Set<FunctionTree> outputTrees = new HashSet<FunctionTree>(BEST_R * HEIGHT * 4 / 3 + 1);

		for(int i=0; i< HEIGHT; i++){
			FunctionTree[] roots = bestRoots.getRoots(desiredType, i);
			if(roots != null)
				for(FunctionTree t: roots){
					//null�v�f�͍폜
					//�����񂪏d�����Ȃ��悤�ɂ���B
					if(t != null){
						outputTrees.add(t);
					}
				}
		}
		outputTrees = new TreeSet<FunctionTree>(outputTrees);
		return outputTrees.toArray(new FunctionTree[0]);
	}

}
