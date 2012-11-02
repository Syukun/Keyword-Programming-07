package plugin.views;

import experiment.LocalSearch2;
import experiment.LocalSearch2Job;
import experiment.Result;
import gridSearch.GridSearchJob;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import keywordProgramming.KeywordProgrammingJob;

import localSearch.LocalSearch;
import localSearch.LocalSearchJob;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ast.AstLocalCode;
import ast.Import;

import plugin.activator.Activator;
import plugin.preference.PreferenceInitializer;
import plugin.testSite.TestSite;
import plugin.testSite.TestSiteComparator;

public class TestSiteView extends org.eclipse.ui.part.ViewPart{

	private CheckboxTableViewer viewer; 
	private Composite fParent;
	
  	private IAction refreshAction;//画面をリフレッシュするアクション
  	private IAction deleteAction;//項目を削除するアクション
  	private IAction localSearchAction;//ローカルサーチをするアクション
  	private IAction gridSearchAction;//グリッドサーチをするアクション
  	
  	private IAction settingAction;//特徴の重みの初期値の設定アクション
  	
  	private IAction selectAllAction;//全てのタスクを選択するアクション
  	private IAction unSelectAllAction;//全てのタスクを選択解除するアクション
  	
  	private IAction countTaskAction;//タスク数をカウントする
  	
  	private IAction kpAction;//キーワードプログラミングの実行アクション

  	private IAction arrangeTaskAction;//タスクを整理するアクション
  	private IAction experimentAction;//実験のためのアクション

	@Override
	public void createPartControl(Composite parent) {
		// TODO 自動生成されたメソッド・スタブ
		//インポート文読み込み。リスナの登録
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		window.getSelectionService().addSelectionListener(Import.listener);
		
		//インポート文読み込み。あらかじめエディタ上で開いてあるファイルに対応する
//		AstLocalCode.clearImportDeclaration();
//		AstLocalCode.getImportDeclaration();
		
		fParent = parent;
		
//		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
//		Table table = viewer.getTable();
		
		Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		
		viewer = new CheckboxTableViewer(table);
		
		viewer.setContentProvider(new ArrayContentProvider());
		
		viewer.addDoubleClickListener(new ViewerDoubleClickListener(viewer));
			
//		TableViewerColumn viewerColumn_check = new TableViewerColumn(viewer, SWT.LEFT);
//		//viewerColumn_check.setLabelProvider(new ClassNameLabelProvider());
//		TableColumn tableColumn_check = viewerColumn_check.getColumn();
//		tableColumn_check.setText("選択");
//		tableColumn_check.setWidth(100); // もしくは pack()
//		viewerColumn_check.setEditingSupport(new CheckboxEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_id = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_id.setLabelProvider(new IdLabelProvider());
		TableColumn tableColumn_id = viewerColumn_id.getColumn();
		tableColumn_id.setText("ID");
		tableColumn_id.setWidth(150); // もしくは pack()
		
		TableViewerColumn viewerColumn_className = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_className.setLabelProvider(new ClassNameLabelProvider());
		TableColumn tableColumn_className = viewerColumn_className.getColumn();
		tableColumn_className.setText("所属パッケージとクラス");
		tableColumn_className.setWidth(220); // もしくは pack()
//		tableColumn_className.pack();
		
		/*
		TableViewerColumn viewerColumn_offset = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_offset.setLabelProvider(new OffsetLabelProvider());
		TableColumn tableColumn_offset = viewerColumn_offset.getColumn();
		tableColumn_offset.setText("オフセット");
//		tableColumn_offset.setWidth(50); // もしくは pack()
		tableColumn_offset.pack();
//		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		*/
		
		TableViewerColumn viewerColumn_startLine = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_startLine.setLabelProvider(new StartLineLabelProvider());
		TableColumn tableColumn_startLine = viewerColumn_startLine.getColumn();
		tableColumn_startLine.setText("開始行");
//		tableColumn_startLine.setWidth(50); // もしくは pack()
		tableColumn_startLine.pack();
//		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_location = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_location.setLabelProvider(new LocationLabelProvider());
		TableColumn tableColumn_location = viewerColumn_location.getColumn();
		tableColumn_location.setText("ロケーション");
		tableColumn_location.setWidth(150); // もしくは pack()
//		tableColumn_text.pack();
		
		TableViewerColumn viewerColumn_retType = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_retType.setLabelProvider(new ReturnTypeLabelProvider());
		TableColumn tableColumn_retType = viewerColumn_retType.getColumn();
		tableColumn_retType.setText("望ましい返り値");
		tableColumn_retType.setWidth(320); // もしくは pack()
//		tableColumn_retType.pack();
//		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_text = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_text.setLabelProvider(new TextLabelProvider());
		TableColumn tableColumn_text = viewerColumn_text.getColumn();
		tableColumn_text.setText("生成したい出力(選択したテキスト)");
		tableColumn_text.setWidth(320); // もしくは pack()
//		tableColumn_text.pack();
		viewerColumn_text.setEditingSupport(new TextCellEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_keyword = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_keyword.setLabelProvider(new KeywordLabelProvider());
		TableColumn tableColumn_keyword = viewerColumn_keyword.getColumn();
		tableColumn_keyword.setText("入力キーワード");
		tableColumn_keyword.setWidth(320); // もしくは pack()
//		tableColumn_text.pack();
		//セルをエディット可能にする。
		viewerColumn_keyword.setEditingSupport(new KeywordCellEditingSupport(viewer));

		TableViewerColumn viewerColumn_saveTime = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_saveTime.setLabelProvider(new SaveTimeLabelProvider());
		TableColumn tableColumn_saveTime = viewerColumn_saveTime.getColumn();
		tableColumn_saveTime.setText("保存日時");
		tableColumn_saveTime.setWidth(190); // もしくは pack()
		
		viewer.setInput(getItems());
//		viewer.setAllChecked(true);
		
		//リフレッシュ・アクション追加。
		registerAction();
	}

	@Override
	public void setFocus() {
		// TODO 自動生成されたメソッド・スタブ
		viewer.getControl().setFocus();
	}

	private List<TestSite> getItems(){
		List<TestSite> list = new ArrayList<TestSite>();
		
		String path = TestSite.TestSiteFolder;
		
	    File dir = new File(path);
	    if (!dir.exists()) {  
		    return null;
		}

	    File[] classFolders = dir.listFiles();
	    for (File folder: classFolders) {
	        File[] files = folder.listFiles();
	        for(File file: files){
	        	TestSite t = new TestSite(file, false);
	        	list.add(t);
	        }
	    }
	    
	  //時間の降順にソートする。
	    TestSiteComparator comp = new TestSiteComparator();
	    Collections.sort(list, comp);
	    
		return list;
	}
	
	private void registerAction(){
		//リフレッシュアクションを作成
		refreshAction = new Action(){
			public void run(){
				viewer.setInput(getItems());
			}
		};
		refreshAction.setText("リフレッシュ");
//		refreshAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_REFRESH));
//		refreshAction.setImageDescriptor(Activator.getImageDescriptor(Activator.IMG_REFRESH, "refresh.gif"));
		refreshAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.browser", "$nl$/icons/elcl16/nav_refresh.gif"));

		//全選択アクションを作成
		selectAllAction = new Action(){
			public void run(){
				viewer.setAllChecked(true);
			}
		};
		selectAllAction.setText("全て選択");
		selectAllAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui", "$nl$/icons/full/elcl16/step_done.gif"));

		//全選択解除アクションを作成
		unSelectAllAction = new Action(){
			public void run(){
				viewer.setAllChecked(false);
			}
		};
		unSelectAllAction.setText("全て選択解除");
		unSelectAllAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.debug.ui", "$nl$/icons/full/dlcl16/disabled_co.gif"));

					
		//削除アクションを作成
		deleteAction = new Action(){
			public void run(){
				//選択されたアイテムを削除する。
				TestSite[] list = getCheckedItems();
				
				//タスクが選択されていない。
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "タスクの削除", "削除するタスクをチェックボックスで選択してください。");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"タスクの削除",
						"選択された "+ list.length + " 個の学習タスクを削除します。");
			
				//"No"なら何もしない。
				if(yn == false){
					return;
				}
				//選択されたファイルを全て削除する。
				for(TestSite site:list){
					site.deleteFile();
				}
				//リフレッシュ
				viewer.setInput(getItems());
			}
		};
		deleteAction.setText("タスク削除");
//		deleteAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_DELETE));
		deleteAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui", "$nl$/icons/full/obj16/delete_obj.gif"));
		

		//キーワードプログラミングのアクションを作成
		kpAction = new Action(){
			public void run(){
				TestSite[] list = getCheckedItems();
				

				//タスクが選択されていない。
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "キーワードプログラミング", "タスクをチェックボックスで選択してください。");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"キーワードプログラミング",
						"選択された "+ list.length + " 個の学習タスクに対してキーワードプログラミングを行います。");
			
				//"No"なら何もしない。
				if(yn == false){
					return;
				}
					
				//コンソール画面表示
				Activator.showConsoleView();
				
				//長い処理なので、ジョブを使う
				KeywordProgrammingJob job = new KeywordProgrammingJob("キーワードプログラミングの実行", list);
				job.setUser(true);//ユーザーにポップアップを表示する。
				job.schedule();//ジョブをEclipseに登録しておきます。あとはEclipseが適切にrunを実行し、処理してくれる
			}
		};
		kpAction.setText("キーワードプログラミング実行");
//		localSearchAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_RUN));
		kpAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.cheatsheets", "$nl$/icons/elcl16/skip_task.gif"));

		
		//ローカルサーチアクションを作成
		localSearchAction = new Action(){
			public void run(){
				//各ファイルについて、
				//キーワードプログラミングをしてからその結果を用いてローカルサーチを行う。
				TestSite[] list = getCheckedItems();
				

				//タスクが選択されていない。
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "ローカルサーチ", "タスクをチェックボックスで選択してください。");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"ローカルサーチ",
						"選択された "+ list.length + " 個の学習タスクに対してローカルサーチを行います。");
			
				//"No"なら何もしない。
				if(yn == false){
					return;
				}
				
				LocalSearch ls = new LocalSearch(list, false);
				
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				
				//ステップ数を指定
				int numOfSteps = store.getInt(PreferenceInitializer.LOCAL_BATCH_NUMBER_OF_STEPS);
				//収束まで行うか否か。
				boolean isConvergence = store.getBoolean(PreferenceInitializer.LOCAL_BATCH_FLAG_CONVERGENCE);
				if(isConvergence == true)
					numOfSteps = -1;
				
				//コンソール画面表示
				Activator.showConsoleView();
				
				//長い処理なので、ジョブを使う
				LocalSearchJob job = new LocalSearchJob("ローカルサーチの実行", ls, numOfSteps);
				job.setUser(true);//ユーザーにポップアップを表示する。
				job.schedule();//ジョブをEclipseに登録しておきます。あとはEclipseが適切にrunを実行し、処理してくれる
			}
		};
		localSearchAction.setText("ローカルサーチ実行");
//		localSearchAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_RUN));
		localSearchAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.cheatsheets", "$nl$/icons/elcl16/start_task.gif"));

		
		//グリッドサーチアクションを作成
		gridSearchAction = new Action(){
			public void run(){
				//各ファイルについて、
				//キーワードプログラミングをしてからその結果を用いてグリッドサーチを行う。
				TestSite[] list = getCheckedItems();
				

				//タスクが選択されていない。
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "グリッドサーチ", "タスクをチェックボックスで選択してください。");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"グリッドサーチ",
						"選択された "+ list.length + " 個の学習タスクに対してグリッドサーチを行います。");
			
				//"No"なら何もしない。
				if(yn == false){
					return;
				}
				
				//コンソール画面表示
				Activator.showConsoleView();
				
				//長い処理なので、ジョブを使う
				GridSearchJob job = new GridSearchJob("グリッドサーチの実行", list);
				job.setUser(true);//ユーザーにポップアップを表示する。
				job.schedule();//ジョブをEclipseに登録しておきます。あとはEclipseが適切にrunを実行し、処理してくれる
			}
		};
		gridSearchAction.setText("グリッドサーチ実行");
//		gridSearchAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_RUN));
		gridSearchAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.debug.ui", "$nl$/icons/full/obj16/lrun_obj.gif"));

		
		//設定アクションを作成
		settingAction = new Action(){
			public void run(){
				//プリファレンスを取得して、ダイアログに表示する。
				PreferenceManager pm = PlatformUI.getWorkbench( ).getPreferenceManager();
		        IPreferenceNode kp_node = pm.find("jp.ac.hokudai.eng.complex.kussharo.sayuu.kp.preference.page.kp");
		        PreferenceManager pm2 = new PreferenceManager();
		        pm2.addToRoot(kp_node);
		        PreferenceDialog dialog1 = new PreferenceDialog(fParent.getShell(), pm2);
		        dialog1.open();
			}
		};
		
		settingAction.setText("設定");
//		settingAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_SETTINGS));
		settingAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.debug.ui", "$nl$/icons/full/elcl16/prop_ps.gif"));

		//プロパティを表示する。
		countTaskAction = new Action(){
			public void run(){
				Object o = viewer.getInput();
				if(o instanceof ArrayList){
					ArrayList<TestSite> ts_arr = (ArrayList<TestSite>)o;
					int sum = ts_arr.size();
//					HashMap<String, Integer> map = new HashMap<String, Integer>();
//					for(TestSite ts: ts_arr){
//						String name = ts.getPackageName();
//						if(map.containsKey(name)){
//							int count = map.get(name);
//							map.put(name, count+1);
//						}else{
//							map.put(name, 1);
//						}
//					}
//					int package_sum = map.size();
					
					HashSet<String> set = new HashSet<String>();
					for(TestSite ts: ts_arr){
						String name = ts.getPackageName();
						set.add(name);
					}
					int package_sum = set.size();
					set.clear();
					for(TestSite ts: ts_arr){
						String name = ts.getClassName();
						set.add(name);
					}
					int class_sum = set.size();
					
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					for(TestSite ts: ts_arr){
						String name = ts.getLocation();
						if(map.containsKey(name)){
							int count = map.get(name);
							map.put(name, count+1);
						}else{
							map.put(name, 1);
						}
					}
					Iterator<String> it = map.keySet().iterator();
					String s = "";
			        while (it.hasNext()) {
			            String name = it.next();
			            s += name + " = " + map.get(name) + "\n";
			        }
					System.out.println(map.toString());
					
					MessageDialog.openInformation(fParent.getShell(), "ビューのプロパティ", "ビューに表示されている\n全タスク数= " + sum
							+ "\n全パッケージ数= " + package_sum
							+ "\n全クラス数= " + class_sum);
//							+ "\n" + s);
				}
				

			}
		};
		
		countTaskAction.setText("プロパティ");
//				countTaskAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_SETTINGS));
		countTaskAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui", "$nl$/icons/full/obj16/generic_elements.gif"));

		
		
		//タスク整理アクション
		arrangeTaskAction = new Action(){
			public void run(){
				Object o = viewer.getInput();
				if(o instanceof ArrayList){
					ArrayList<TestSite> ts_arr = (ArrayList<TestSite>)o;
					List<Result> result_list = new ArrayList<Result>(); 
					
					for(TestSite ts: ts_arr){
						String str = ts.getSelectedString();
						int odr = -1;//順位は不明。
						int numKey = ts.getNumOfKeywords();
						int numLT = ts.getNumOfLocalTypes();
						int numLF = ts.getNumOfLocalFunctions();
//						//genericsを削除
//			        	if(!str.contains("<"))
			        		result_list.add(new Result(ts.getId(), str, odr, numKey, numLT, numLF));
					}
					System.out.println("==============重複文字列とgenericsを削除した=================================");
					
					//ソートして重複文字列を削除する。
//					TreeSet<Result> result_set = new TreeSet<Result>(result_list);
//					Iterator<Result> it = result_set.iterator();
//			        while (it.hasNext()) {
//			        	Result result = it.next();
//			        	//genericsを削除
//			        	if(!result.fSelectedString.contains("<"))
//			        		System.out.println(result.fTestSiteId + "\t" + result.fSelectedString);
//			        }
			        
					TreeSet<Result> result_set = new TreeSet<Result>();
					List<Result> deleted_list = new ArrayList<Result>(); 
					
					for(Result r : result_list){
						//重複と、genericsを削除
						if(!result_set.contains(r) && !r.fSelectedString.contains("<")){
							result_set.add(r);
							System.out.println(r.fTestSiteId + "\t" + r.fSelectedString);
						}else{
							deleted_list.add(r);
						}
					}
					
			        System.out.println("=================削除されたもの一覧==============================");
					
			        for(Result r: deleted_list){
				        System.out.println(r.fTestSiteId + "\t" + r.fSelectedString);
				        for(TestSite ts: ts_arr){
				        	if(r.fTestSiteId.equals(ts.getId())){
				        		ts.deleteFile();
				        	}
				        }
			        }
			        refresh();
				}
			}
		};
		arrangeTaskAction.setText("タスク整理1");

		
		//実験1
		experimentAction = new Action(){
			public void run(){
				//各ファイルについて、
				//キーワードプログラミングをしてからその結果を用いてローカルサーチを行う。
				TestSite[] list = getCheckedItems();
				

				//タスクが選択されていない。
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "実験1", "タスクをチェックボックスで選択してください。");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"実験1",
						"選択された "+ list.length + " 個の学習タスクに対して実験1を行います。");
			
				//"No"なら何もしない。
				if(yn == false){
					return;
				}
				
				LocalSearch2 ls = new LocalSearch2(list, false);
				
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				
				//ステップ数を指定
				int numOfSteps = store.getInt(PreferenceInitializer.LOCAL_BATCH_NUMBER_OF_STEPS);
				//収束まで行うか否か。
				boolean isConvergence = store.getBoolean(PreferenceInitializer.LOCAL_BATCH_FLAG_CONVERGENCE);
				if(isConvergence == true)
					numOfSteps = -1;
				
				//コンソール画面表示
				Activator.showConsoleView();
				
				//長い処理なので、ジョブを使う
				LocalSearch2Job job = new LocalSearch2Job("実験1の実行", ls, numOfSteps);
				job.setUser(true);//ユーザーにポップアップを表示する。
				job.schedule();//ジョブをEclipseに登録しておきます。あとはEclipseが適切にrunを実行し、処理してくれる
			}
		};
		experimentAction.setText("実験１");
		
		//アクションをツールバーとプルダウンメニューに組み込む。
		IActionBars bars = getViewSite().getActionBars();
		
		bars.getToolBarManager().add(refreshAction);
		bars.getToolBarManager().add(deleteAction);
		bars.getToolBarManager().add(localSearchAction);
		bars.getToolBarManager().add(gridSearchAction);
		bars.getToolBarManager().add(kpAction);
		bars.getToolBarManager().add(settingAction);
		bars.getToolBarManager().add(selectAllAction);
		bars.getToolBarManager().add(unSelectAllAction);
		bars.getToolBarManager().add(countTaskAction);
		
		bars.getMenuManager().add(refreshAction);
		bars.getMenuManager().add(deleteAction);
		bars.getMenuManager().add(localSearchAction);
		bars.getMenuManager().add(gridSearchAction);
		bars.getMenuManager().add(kpAction);
		bars.getMenuManager().add(settingAction);
		bars.getMenuManager().add(selectAllAction);
		bars.getMenuManager().add(unSelectAllAction);
		bars.getMenuManager().add(countTaskAction);
		
		//アクションをツールバーとポップアップメニューに組み込む。
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				// TODO Auto-generated method stub
				manager.add(refreshAction);
				manager.add(deleteAction);
				manager.add(localSearchAction);
				manager.add(gridSearchAction);
				manager.add(kpAction);
				manager.add(settingAction);
				manager.add(selectAllAction);
				manager.add(unSelectAllAction);
				manager.add(countTaskAction);
				manager.add(arrangeTaskAction);
				manager.add(experimentAction);
				
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	/*
	 * 画面をリフレッシュし、新たに登録されたものを表示する。
	 */
	public void refresh(){
		refreshAction.run();
	}
	
	/*
	 * チェックしたもののみ取得する。
	 * 
	 * 
	 */
	public TestSite[] getCheckedItems(){
		/*
		Object[]からTestSite[]のキャストはできない。
		TestSite[] site = (TestSite[])viewer.getCheckedElements();
		
		参考：http://d.hatena.ne.jp/fumokmm/20080902/1220372739
		*/
		Object[] o = viewer.getCheckedElements();
		List<Object> site = Arrays.asList(o);
		return site.toArray(new TestSite[site.size()]);
	}
}
