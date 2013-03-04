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
	
	private static TestSite tsLog;//記録用ソースコード状況

    
	//履歴を表すクラス（キーワード、文字列、回数）
	private class UsedString{
		private String keywords;
		private String desiredReturnType;
		private String replacementString;
		private int count;

		//履歴(UsedString.txt)の1行
		public UsedString(String used){
			String s[] = used.split("\t");
			this.keywords = s[0];
			this.desiredReturnType = s[1];
			this.replacementString = s[2];
			this.count = Integer.parseInt(s[3]);//intに変換
		}

	}

	public JavaCompletionProposalComputer() {
		// TODO 自動生成されたコンストラクター・スタブ
		//インポート文読み込み。リスナの登録
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(Import.listener);
		//インポート文読み込み。あらかじめエディタ上で開いてあるファイルに対応する
		Import.clearImportDeclaration();
		Import.getImportDeclaration(null);
	}

	@Override
	public List computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		// TODO 自動生成されたメソッド・スタブ		
		long start = System.currentTimeMillis();

		//プロポーザルリストの作成
		List<MyCompletionProposal> proposalList = new ArrayList<MyCompletionProposal>();

		String source = context.getDocument().get();//編集中のソース
		int offset = context.getInvocationOffset();//キャレット位置
		int lineNumber = 0;//行番号
		try {
			lineNumber = context.getDocument().getLineOfOffset(offset);
		} catch (BadLocationException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		/*
		 * '.'では起動しない。
		 * WindowsではcharAt(offset)に'\r', charAt(offset-1)に'.'となっている。
		 */
		if(source.charAt(offset) == '.' || source.charAt(offset-1) == '.'){
			return proposalList;
		}

		try{
			/*
			 * offset位置から、入力キーワードと返り値の型と、周辺の型(Type)と、関数(Function)を取得するメソッド 
			 */
			AstLocalCode.getLocalInfomation(source, offset, 0, false, null, null, null);
			
			String keywords = AstLocalCode.getKeywords();
			String className = AstLocalCode.getClassName();
			String desiredReturnType = AstLocalCode.getDesiredReturnType();
	    	String location = AstLocalCode.getLocation();
			int keyword_head_offset = AstLocalCode.getKeywordHeadOffset();
			int replacement_length = AstLocalCode.getReplacementLength();
			//現在エディタ内に存在する有効なTypeを入れるリスト
	    	List<String> classesInActiveEditor = new ArrayList<String>(AstLocalCode.getClasses());
	    	//現在エディタ内に存在する有効なFunctionを入れるリスト
	    	List<String> functionsInActiveEditor = new ArrayList<String>(AstLocalCode.getFunctions());
	    	
			//出力の計算
			FunctionTree[] outputs = KeywordProgramming.execute(keywords, desiredReturnType, classesInActiveEditor, functionsInActiveEditor, KpRunningState.CODE_COMPLETION);
			
			AstLocalCode.clear();//使った後はクリアする。
			
			//保存する履歴のための出力候補群のリスト
			List<OutputCandidateLog> outputLogList = new ArrayList<OutputCandidateLog>();
			
			/*
			 * 出力候補群の生成。
			 * 
			 * １つ１つの候補は
			 * MyCompletionProposal
			 * である。
			 */
			if(outputs != null)
				for(int i = 0; i < outputs.length ; i++){
					if(outputs[i] != null){
						String out = outputs[i].toCompleteMethodString();
						MyCompletionProposal mcp = new MyCompletionProposal(out, keyword_head_offset, replacement_length, out.length(), keywords, desiredReturnType, i, outputs[i]);
						//リストに追加
						proposalList.add(mcp);
						outputLogList.add(new OutputCandidateLog(outputs[i]));
					}
				}
			
			/*
			 * あとで選択履歴を保存するために、
			 * ソースコード状況を保持しておく。 
			 * 
			 * 選択テキストとテキスト長は、
			 * 候補が選択されてから入力する。
			 */
			TestSite site = new TestSite(className, offset, keyword_head_offset, lineNumber, lineNumber, 0, null, desiredReturnType, location, classesInActiveEditor, functionsInActiveEditor, true);
			setLogSite(site, outputLogList);
	
			
			//履歴使わない。
				//履歴の読み込み
				//List<UsedString> usedList = new ArrayList<UsedString>();
				//readUsedStringList(usedList);
			
				//Iterator<String> it = outputs.keySet().iterator();
			/*
	        while(it.hasNext()){
	        	String out = it.next();
				MyCompletionProposal mcp = new MyCompletionProposal(out, offset, 0, out.length(), keywords, desiredReturnType, outputs.get(out));
				//『キーワードと返り値の型と文字列の組』が履歴に含まれるかを確認
	
				int usedCount = 0;//履歴に存在しないとき、回数0
				for(UsedString used: usedList){
					if(used.keywords.equals(keywords) && used.desiredReturnType.equals(desiredReturnType) && used.replacementString.equals(out)){
						usedCount = used.count;
						break;
					}
				}
				mcp.plusEVec(usedCount);//最終的な木の評価値を上げている
				mcp.setCount(usedCount);//履歴に保存されている出現回数
	
				//リストに追加
				proposalList.add(mcp);
			}
			*/
			
		}catch(Exception e){
			//変な場所で起動したりしたときなどの例外をキャッチする。
			e.printStackTrace();
		}
		 long stop = System.currentTimeMillis();
		 System.out.println("実行にかかった時間は " + (stop - start) + " ミリ秒です。JavaCompletionProposalComputer.computeCompletionProposals");
		return proposalList;
	}

	@Override
	public List computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void sessionEnded() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void sessionStarted() {
		// TODO 自動生成されたメソッド・スタブ

	}

	public static TestSite getLogSite() {
		return tsLog;
	}

	/*
	 * 履歴の保存
	 * 出力候補群 outputLogList も必要。
	 */
	public static void setLogSite(TestSite ts, List<OutputCandidateLog> outputLogList) {
		ts.setOutputLogList(outputLogList);
		JavaCompletionProposalComputer.tsLog = ts;
	}


}


