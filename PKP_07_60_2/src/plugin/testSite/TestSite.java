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
 * 1�̃^�X�N��\���N���X�B
 */
public class TestSite {

	public static final String TestSiteFolder = "./KeywordProgramming/testSite/";
	public static final String LogSiteFolder = "./KeywordProgramming/logSite/";

	private static List<TestSite> logFiles = new LinkedList<TestSite>();	//�I�����C���w�K�p�̃��O�t�@�C��
	
	private String className;//�N���X���i�p�b�P�[�W�����j
	private int offset;
	private int original_offset;	//�ύX�O�B
    private int line_number_start;
    private int line_number_end;
    private int selected_length;
    //�I�𕶎���
    private String selected_string;
    //�I�𕶎���̏���
    private int selected_string_order;
    //�]�܂����Ԃ�l�̌^
  	private String desiredReturnType;
  	//���̓L�[���[�h
  	private String keywords;
  	//���P�[�V����
  	/*
  	 * Return�� �̒�
	 * If���@�̒�
	 * �錾��
	 * �����
	 * �n�̕�
	 * ���\�b�h�̈����̒�
	 * �ȂǁB
  	 */
  	private String location;
  	
  	//�d����h�����߂�HashSet�ɂ����B
  	//���݃G�f�B�^���ɑ��݂���L���Ȋ֐������郊�X�g
  	private HashSet<String> classesInActiveEditor = new HashSet<String>();
  	//���݃G�f�B�^���ɑ��݂���L���Ȋ֐������郊�X�g
  	private HashSet<String> functionsInActiveEditor = new HashSet<String>();
  	
  	//1��TextSite�ɑΉ�����1�̃e�L�X�g�t�@�C��
  	private File txtFile;
  	
  	//�I�����ꂽ
  	private boolean isSelectedTask = false;
  	
  	//���O�t�@�C�����ۂ�
  	private boolean isLogFile;
  	
  	//�L�[���[�h�v���O���~���O�̏o��(�o�b�`�w�K�̎��Ɏg�p�B)
  	public List<FunctionTree> outputfunctionTrees = null;
  	
  	//�L�[���[�h�v���O���~���O�̏o��(�I�����C���w�K�̎��Ɏg�p�B)
  	private List<OutputCandidateLog> outputLogList = new ArrayList<OutputCandidateLog>();
  	
  	//1�̃t�@�C��file�͂P��TestSite�ɑΉ��B
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
					String s1[] = db_str.split(";");   // ";"����؂�B
					for(String ss1: s1){
						classesInActiveEditor.add(ss1);
					}
		  		}else{
					//function 1�s���ƁB
			        functionsInActiveEditor.add(db_str);
		  		}
		  		line_number++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}
		
		/*
		 * Log�t�@�C���̏ꍇ�A�o�͌��Q��ۑ������t�@�C�� ".*out.txt" ���ǂݍ���
		 */
		if(isLogFile){
			String name = txtFile.getName().replace(".txt", "out.txt");
			String path = txtFile.getParent() + "/" +name;
			File logFile = new File(path);
			if(logFile.exists()){
				try {
					fr = new FileReader(logFile);
					BufferedReader br = new BufferedReader(fr);

					selected_string_order = Integer.parseInt(br.readLine());//�擪�s�͑I�����̏��ʁB
					
					String log_str;
					while ((log_str = br.readLine()) != null) {
						OutputCandidateLog log = new OutputCandidateLog(log_str, keywords.split("[ �@\t]").length);
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
  		String path = TestSite.LogSiteFolder;//���O��ǂݎ��
  		
	    File dir = new File(path);
	    if (!dir.exists()) {  
		    return;
		}

	    File[] classFolders = dir.listFiles();
	    for (File folder: classFolders) {
	        File[] files = folder.listFiles();
	        
	        for(File file: files){
	        	//�o�͕����񃍃O�t�@�C���͏����B
	        	if(file.getName().matches(".*out\\.txt$") == false){
		        	logFiles.add(new TestSite(file, true));
	        	}
	        }
	    }
  		
	    //���Ԃ̍~���Ƀ\�[�g����B
	    Collections.sort(logFiles, new TestSiteComparator());
	    
//	    for(TestSite site: logFiles){
//	    	System.out.println(site.getSaveTime());
//	    }
	   
  	}
  	
  	/*
  	 * ���O�t�@�C���̒ǉ��B
  	 */
  	public static void addLogFile(TestSite site){
  		//�擪�ɒǉ�����B
  		logFiles.add(0, site);
  	}

  	/*
  	 * ���O�t�@�C���̎擾
  	 * �w�肵�����̂݁A�ŐV�̐擪����B
  	 */
  	public static TestSite[] getLogFiles(int numOfFiles){
  		ArrayList<TestSite> list = new ArrayList<TestSite>();
  		for(int i = 0; i < numOfFiles; i++){
  			list.add(logFiles.get(i));
  		}
  		return (TestSite[]) list.toArray(new TestSite[list.size()]);
  	}
  	
    /**
     * �I���e�L�X�g����L�[���[�h��������������̂ŁA
     * �L�[���[�h�������ɕK�v�Ƃ��Ȃ��B
     * 
     * @param cn	�N���X��
     * @param o		�I�t�Z�b�g
     * @param o_ori	�ύX�O�̃I�t�Z�b�g
     * @param lis	�I���J�n�s
     * @param lie	�I���I���s
     * @param len	�I���e�L�X�g��
     * @param sct	�I���e�L�X�g
     * @param drt	�]�܂����Ԃ�l
     * @param lo	���P�[�V����
     * @param cls	Type�̃��X�g
     * @param fus	Function�̃��X�g
     * @param is_log_file ���O�t�@�C�����H
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
        //�t�@�C�����́u�p�b�P�[�W���{�N���X��/currentTimeMillis�v
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
    	//11�s��types
        Iterator<String> it_c = classesInActiveEditor.iterator();
        while(it_c.hasNext()){
        	String ss = it_c.next();
        	s += ss + ";";
        }
        s += "\n";
        //12�s�ȍ~ functions
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
	 * �o�͕��������̓L�[���[�h�ɕϊ�����
	 * 
	 * ����out��
	 *  message.replaceAll(space, comma);
	 *  �Ȃ�΁A
	 *  �Ԃ�l��
	 *   message replace all space comma
	 *  �ƂȂ�B
	 *  
	 *  �s���I�h�A�J���}�A�Z�~�R�����A���ʂ͎��A�󔒕����ɕϊ�����B
	 *  message�@replaceAll�@space�@ comma�@�@
	 *  
	 *  replaceAll�̕����́A�啶���̎�O�ɋ󔒕�����}�����Ă���A
	 *  �啶�����������ɕϊ�����B��AstUtil.splitName���g���B
	 *  
	 */
	public String output2InputKeyword(String out){
		if(out == null)
			return null;
		//�s���I�h�A�J���}�A�Z�~�R�����A���ʂ͎��A�󔒕����ɕϊ�����B
		//�_�u���N�I�[�e�[�V������<>�����B
		String rep1 = out.replaceAll("[\\.\\,\\;\\(\\)\\<\\>\"]", " ");
		//�啶���̎�O�ɋ󔒕�����}�����Ă���A�啶�����������ɕϊ�����B
		String rep2 = ast.AstUtil.splitName(rep1, " ");
		//�����̋󔒕������A�����Ă����ꍇ�P�ɂ���B
		String rep3 = rep2.replaceAll(" +", " ");
		return rep3.trim();
	}
	
	//keyword�̐����J�E���g����B
	private int countKeywords(){
		
		if(keywords == null)
			return -1;
		else{
			//�X��keyword�ɕ���
			String []input_keywords = keywords.split("[ �@\t]");
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
	 * �L�[���[�h�v���O���~���O�̃A���S���Y�������s����B
	 * �����āA
	 * ���selected_string�ɕҏW�������߂��o�͕�����̏��ʂ��o�͂���B
	 * 
	 */
	public int runKeywordProgrammingAndGetNearestOutputNumber(int output_size, String state){
		//�L�[���[�h�v���O���~���O�̎��s
		FunctionTree[] ft = KeywordProgramming.execute(keywords, desiredReturnType,  new ArrayList<String>(classesInActiveEditor), new ArrayList<String>(functionsInActiveEditor), state);
		outputfunctionTrees = (ArrayList<FunctionTree>) Arrays.asList(ft);
		//���selected_string�ɕҏW�������߂��o�͕�����̏��ʂ𓾂�B
		return getNearestDistanceOutputNumber(output_size);
	}

	/*
	 * ���selected_string�ɕҏW�������߂��o�͕�����̏��ʂ𓾂�B
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
			s += min_d + ", " + ", �񓚂Ȃ� ts == null";
//			System.out.println(i + ", �񓚂Ȃ� ts == null, "+ desiredReturnType);
		}else{

			FunctionTree best_tree = null;

			//pop-up�ɕ\�����ꂽ���̒��̂����A
			//output_size �ȓ��̒��ŁA
			//��ԓ����ɋ߂������̌���T��
			if(output_size > outputfunctionTrees.size())
				output_size = outputfunctionTrees.size();//������over���Ă���A�z��T�C�Y�ɂ���B
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
				s += min_d + ", " + best_tree_order + ", " + ", �񓚂Ȃ� best_tree == null";
//				System.out.println(i + ", �񓚂Ȃ� best_tree == null, "+ desiredReturnType);
			}else{
				//System.out.println(i + ", " + min_d + ", " + best_tree.toCompleteMethodString());
				s += min_d + ", " + best_tree_order + ", " + best_tree.toCompleteMethodString() + ", " + best_tree.getEvaluationValue();
//				System.out.println(s);
				best_tree.setSelectedFlg();	//�I�񂾃t���O�B
			}

		}
		System.out.println("=== " + selected_string + " �Ɉ�ԕҏW�������߂������� ===");
		System.out.println(s);
		System.out.println("=== " + selected_string + " �Ɉ�ԕҏW�������߂������� ===");
		
		return best_tree_order;
	}


	/*
	 * �L�[���[�h�v���O���~���O�̃A���S���Y�������s����B
	 * �����āA�����̏��ʂ��o�͂���B
	 * �������o�Ȃ���΁A
	 * -1��Ԃ��B
	 */
	public int runKeywordProgrammingAndOutputNumber(int output_size, String state){
		FunctionTree[] ft = KeywordProgramming.execute(keywords, desiredReturnType,  new ArrayList<String>(classesInActiveEditor), new ArrayList<String>(functionsInActiveEditor), state);
		outputfunctionTrees = (ArrayList<FunctionTree>) Arrays.asList(ft);
		//�����̏��ʂ��o�͂���B
		return getAnswerNumber(output_size);
	}

	//�L�[���[�h�v���O���~���O�̏�����
	public void initKeywordProgramming(){
		outputfunctionTrees = null;
	}
	
	//�L�[���[�h�v���O���~���O�̏I��
	public void closeKeywordProgramming(){
		outputfunctionTrees = null;
	}
	
	/*
	 * �L�[���[�h�v���O���~���O�̃A���S���Y�������s���A
	 * �o�͌��Q��Ԃ�
	 */
	public List<FunctionTree> runKeywordProgramming(String state){
		//�L�[���[�h�v���O���~���O�̎��s
		FunctionTree[] ft =  KeywordProgramming.execute(keywords, desiredReturnType,  new ArrayList<String>(classesInActiveEditor), new ArrayList<String>(functionsInActiveEditor), state);
		outputfunctionTrees = Arrays.asList(ft);
		return outputfunctionTrees;
	}

	/*
	 * �]���l�̍Čv�Z
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
	 * �o�͌��̃\�[�g
	 */
	public void sortFunctionTrees(){
		if(isLogFile == false){
			Collections.sort(outputfunctionTrees);
		}else{
			Collections.sort(outputLogList, new OutputCanditateLogComparator());
		}
	}
	
	/*
	 * �����̏��ʂ����߂�B
	 * ���ʂ�0�Ԃ���B
	 * 
	 * �������o�Ȃ���΁A-1��Ԃ��B
	 */
	public int getAnswerNumber(int output_size) {
		
		int best_tree_order = -1;

//		String s = i + ", ";
		String s = "";
		for(double d : ExplanationVector.getWeights()){
			s += d + ", ";
		}
		
		if(isLogFile == false){
			//�o�b�`�����̂Ƃ��B
			if(outputfunctionTrees == null){
				s += "�񓚂Ȃ� ts == null";
			}else{
	
				FunctionTree best_tree = null;
	
				//pop-up�ɕ\�����ꂽ���̒��ɐ��������邩���ׂ�B
				if(output_size > outputfunctionTrees.size())
					output_size = outputfunctionTrees.size();//������over���Ă���A�z��T�C�Y�ɂ���B
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
					s += best_tree_order + ", �񓚂Ȃ� best_tree == null";
				}else{
					s += best_tree_order + ", " + best_tree.toCompleteMethodString() + ", " + best_tree.getEvaluationValue();
					best_tree.setSelectedFlg();	//�I�񂾃t���O�B
				}
	
			}
	//		System.out.println("=== " + selected_string + " ���o���������� ===");
	//		System.out.println(s);
	//		System.out.println("=== " + selected_string + " ���o���������� ===");
		}else{
			//�I�����C�������̂Ƃ��B
			if(outputLogList == null){
				s += "�񓚂Ȃ� ts == null";
			}else{
	
				OutputCandidateLog best_log = null;
	
				//pop-up�ɕ\�����ꂽ���̒��ɐ��������邩���ׂ�B
				if(output_size > outputLogList.size())
					output_size = outputLogList.size();//������over���Ă���A�z��T�C�Y�ɂ���B
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
					s += best_tree_order + ", �񓚂Ȃ� best_tree == null";
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
	 * �e�X�g�T�C�g�̃t�@�C����V�K�쐬
	 */
	public void createNewFile() {
		//�t�H���_���Ȃ���΍쐬����B
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
	 * �e�X�g�T�C�g�̃t�@�C����V�K�쐬
	 */
	public void copyAndMoveFileToTestSiteFolder() {
		//�t�@�C�����Ȃ����
		if (!txtFile.exists()) {  
		    return;
		}
		String savefilename = TestSiteFolder + className + "/" + txtFile.getName();
      	File newFile = new File(savefilename);
      	
		//�t�H���_���Ȃ���΍쐬����B
		File dir = newFile.getParentFile();
		if (!dir.exists()) {  
		    dir.mkdirs();
		}
		
		//�t�@�C�����R�s�[����B
		try{
			copyFile(txtFile, newFile);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * �e�X�g�T�C�g���폜����
	 */
	public void deleteFile() {
		//�t�@�C�����Ȃ����
		if (!txtFile.exists()) {  
		    return;
		}
		txtFile.delete();
	}
	
	/*
	 * �����Ƃ��ăe�X�g�T�C�g�̃t�@�C����V�K�쐬
	 * 
	 * �ۑ�����ꏊ���Ⴄ�����B
	 */
	public void createNewFileAsLog(String selected_string, int selected_length, int selected_string_order, String keywords) {
		this.selected_string = selected_string;
		this.selected_length = selected_length;
		this.selected_string_order = selected_string_order;
		this.keywords = keywords;
		
		//�t�@�C�����́u�p�b�P�[�W���{�N���X��/currentTimeMillis�v
		long millis = System.currentTimeMillis();
      	String savefilename = LogSiteFolder + className + "/" + millis + ".txt";
      	//�o�͕�������ۑ��B
      	String savefilename_o = LogSiteFolder + className + "/" + millis + "out.txt";
      	File  file = new File(savefilename);
      	File  file_o = new File(savefilename_o);
		//�t�H���_���Ȃ���΍쐬����B
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
			pw.write(selected_string_order + "\n");	//�擪�s�͑I���������̏��ʁB
			for(FunctionTree ft : KeywordProgramming.getOutputFunctionTrees()){
				pw.write(ft.toLogDBString());
			} 
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * �t�@�C���ۑ��������擾
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
	
	//�t�@�C�����R�s�[����B
	 private void copyFile(File in, File out) throws IOException {
	    FileChannel sourceChannel = new FileInputStream(in).getChannel();
	    FileChannel destinationChannel = new FileOutputStream(out).getChannel();
	    sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
	    sourceChannel.close();
	    destinationChannel.close();
	 }
	 
	 /*
	  * ����TestSite��ID��Ԃ��B
	  */
	 public String getId(){
		 return String.valueOf(getSaveTime());
	 }
}
