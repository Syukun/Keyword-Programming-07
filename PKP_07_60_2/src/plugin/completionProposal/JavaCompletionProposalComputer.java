package plugin.completionProposal;

import java.util.ArrayList;
import java.util.List;

import keywordProgramming.FunctionTree;
import keywordProgramming.KeywordProgramming;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import plugin.testSite.OutputCandidateLog;
import plugin.testSite.TestSite;
import state.KpRunningState;

import ast.AstLocalCode;
import ast.Import;


public class JavaCompletionProposalComputer implements
		IJavaCompletionProposalComputer {

//	static final String USED_STRING_FILE_NAME = "C:\\usedString.txt";
//	static final String TMP_USED_STRING_FILE_NAME = "C:\\tmpUsedString.txt";

	static final String USED_STRING_FILE_NAME = "./usedString.txt";
	static final String TMP_USED_STRING_FILE_NAME = "tmpUsedString.txt";
	
	private static TestSite tsLog;//�L�^�p�\�[�X�R�[�h��

    
	//������\���N���X�i�L�[���[�h�A������A�񐔁j
	private class UsedString{
		private String keywords;
		private String desiredReturnType;
		private String replacementString;
		private int count;

		//����(UsedString.txt)��1�s
		public UsedString(String used){
			String s[] = used.split("\t");
			this.keywords = s[0];
			this.desiredReturnType = s[1];
			this.replacementString = s[2];
			this.count = Integer.parseInt(s[3]);//int�ɕϊ�
		}

	}

	public JavaCompletionProposalComputer() {
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
		//�C���|�[�g���ǂݍ��݁B���X�i�̓o�^
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(Import.listener);
		//�C���|�[�g���ǂݍ��݁B���炩���߃G�f�B�^��ŊJ���Ă���t�@�C���ɑΉ�����
		Import.clearImportDeclaration();
		Import.getImportDeclaration(null);
	}

	@Override
	public List computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u		
		long start = System.currentTimeMillis();

		//�v���|�[�U�����X�g�̍쐬
		List<MyCompletionProposal> proposalList = new ArrayList<MyCompletionProposal>();

		String source = context.getDocument().get();//�ҏW���̃\�[�X
		int offset = context.getInvocationOffset();//�L�����b�g�ʒu
		int lineNumber = 0;//�s�ԍ�
		try {
			lineNumber = context.getDocument().getLineOfOffset(offset);
		} catch (BadLocationException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}
		/*
		 * '.'�ł͋N�����Ȃ��B
		 * Windows�ł�charAt(offset)��'\r', charAt(offset-1)��'.'�ƂȂ��Ă���B
		 */
		if(source.charAt(offset) == '.' || source.charAt(offset-1) == '.'){
			return proposalList;
		}

		try{
			/*
			 * offset�ʒu����A���̓L�[���[�h�ƕԂ�l�̌^�ƁA���ӂ̌^(Type)�ƁA�֐�(Function)���擾���郁�\�b�h 
			 */
			AstLocalCode.getLocalInfomation(source, offset, 0, false, null, null, null);
			
			String keywords = AstLocalCode.getKeywords();
			String className = AstLocalCode.getClassName();
			String desiredReturnType = AstLocalCode.getDesiredReturnType();
	    	String location = AstLocalCode.getLocation();
			int keyword_head_offset = AstLocalCode.getKeywordHeadOffset();
			int replacement_length = AstLocalCode.getReplacementLength();
			//���݃G�f�B�^���ɑ��݂���L����Type�����郊�X�g
	    	List<String> classesInActiveEditor = new ArrayList<String>(AstLocalCode.getClasses());
	    	//���݃G�f�B�^���ɑ��݂���L����Function�����郊�X�g
	    	List<String> functionsInActiveEditor = new ArrayList<String>(AstLocalCode.getFunctions());
	    	
			//�o�͂̌v�Z
			FunctionTree[] outputs = KeywordProgramming.execute(keywords, desiredReturnType, classesInActiveEditor, functionsInActiveEditor, KpRunningState.CODE_COMPLETION);
			
			AstLocalCode.clear();//�g������̓N���A����B
			
			//�ۑ����闚���̂��߂̏o�͌��Q�̃��X�g
			List<OutputCandidateLog> outputLogList = new ArrayList<OutputCandidateLog>();
			
			/*
			 * �o�͌��Q�̐����B
			 * 
			 * �P�P�̌���
			 * MyCompletionProposal
			 * �ł���B
			 */
			if(outputs != null)
				for(int i = 0; i < outputs.length ; i++){
					if(outputs[i] != null){
						String out = outputs[i].toCompleteMethodString();
						MyCompletionProposal mcp = new MyCompletionProposal(out, keyword_head_offset, replacement_length, out.length(), keywords, desiredReturnType, i, outputs[i]);
						//���X�g�ɒǉ�
						proposalList.add(mcp);
						outputLogList.add(new OutputCandidateLog(outputs[i]));
					}
				}
			
			/*
			 * ���ƂőI�𗚗���ۑ����邽�߂ɁA
			 * �\�[�X�R�[�h�󋵂�ێ����Ă����B 
			 * 
			 * �I���e�L�X�g�ƃe�L�X�g���́A
			 * ��₪�I������Ă�����͂���B
			 */
			TestSite site = new TestSite(className, offset, keyword_head_offset, lineNumber, lineNumber, 0, null, desiredReturnType, location, classesInActiveEditor, functionsInActiveEditor, true);
			setLogSite(site, outputLogList);
	
			
			//�����g��Ȃ��B
				//�����̓ǂݍ���
				//List<UsedString> usedList = new ArrayList<UsedString>();
				//readUsedStringList(usedList);
			
				//Iterator<String> it = outputs.keySet().iterator();
			/*
	        while(it.hasNext()){
	        	String out = it.next();
				MyCompletionProposal mcp = new MyCompletionProposal(out, offset, 0, out.length(), keywords, desiredReturnType, outputs.get(out));
				//�w�L�[���[�h�ƕԂ�l�̌^�ƕ�����̑g�x�������Ɋ܂܂�邩���m�F
	
				int usedCount = 0;//�����ɑ��݂��Ȃ��Ƃ��A��0
				for(UsedString used: usedList){
					if(used.keywords.equals(keywords) && used.desiredReturnType.equals(desiredReturnType) && used.replacementString.equals(out)){
						usedCount = used.count;
						break;
					}
				}
				mcp.plusEVec(usedCount);//�ŏI�I�Ȗ؂̕]���l���グ�Ă���
				mcp.setCount(usedCount);//�����ɕۑ�����Ă���o����
	
				//���X�g�ɒǉ�
				proposalList.add(mcp);
			}
			*/
			
		}catch(Exception e){
			//�ςȏꏊ�ŋN�������肵���Ƃ��Ȃǂ̗�O���L���b�`����B
			e.printStackTrace();
		}
		 long stop = System.currentTimeMillis();
		 System.out.println("���s�ɂ����������Ԃ� " + (stop - start) + " �~���b�ł��BJavaCompletionProposalComputer.computeCompletionProposals");
		return proposalList;
	}

	@Override
	public List computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}

	@Override
	public void sessionEnded() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void sessionStarted() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	public static TestSite getLogSite() {
		return tsLog;
	}

	/*
	 * �����̕ۑ�
	 * �o�͌��Q outputLogList ���K�v�B
	 */
	public static void setLogSite(TestSite ts, List<OutputCandidateLog> outputLogList) {
		ts.setOutputLogList(outputLogList);
		JavaCompletionProposalComputer.tsLog = ts;
	}


}


