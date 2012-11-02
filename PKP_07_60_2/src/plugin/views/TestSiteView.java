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
	
  	private IAction refreshAction;//��ʂ����t���b�V������A�N�V����
  	private IAction deleteAction;//���ڂ��폜����A�N�V����
  	private IAction localSearchAction;//���[�J���T�[�`������A�N�V����
  	private IAction gridSearchAction;//�O���b�h�T�[�`������A�N�V����
  	
  	private IAction settingAction;//�����̏d�݂̏����l�̐ݒ�A�N�V����
  	
  	private IAction selectAllAction;//�S�Ẵ^�X�N��I������A�N�V����
  	private IAction unSelectAllAction;//�S�Ẵ^�X�N��I����������A�N�V����
  	
  	private IAction countTaskAction;//�^�X�N�����J�E���g����
  	
  	private IAction kpAction;//�L�[���[�h�v���O���~���O�̎��s�A�N�V����

  	private IAction arrangeTaskAction;//�^�X�N�𐮗�����A�N�V����
  	private IAction experimentAction;//�����̂��߂̃A�N�V����

	@Override
	public void createPartControl(Composite parent) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		//�C���|�[�g���ǂݍ��݁B���X�i�̓o�^
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		window.getSelectionService().addSelectionListener(Import.listener);
		
		//�C���|�[�g���ǂݍ��݁B���炩���߃G�f�B�^��ŊJ���Ă���t�@�C���ɑΉ�����
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
//		tableColumn_check.setText("�I��");
//		tableColumn_check.setWidth(100); // �������� pack()
//		viewerColumn_check.setEditingSupport(new CheckboxEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_id = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_id.setLabelProvider(new IdLabelProvider());
		TableColumn tableColumn_id = viewerColumn_id.getColumn();
		tableColumn_id.setText("ID");
		tableColumn_id.setWidth(150); // �������� pack()
		
		TableViewerColumn viewerColumn_className = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_className.setLabelProvider(new ClassNameLabelProvider());
		TableColumn tableColumn_className = viewerColumn_className.getColumn();
		tableColumn_className.setText("�����p�b�P�[�W�ƃN���X");
		tableColumn_className.setWidth(220); // �������� pack()
//		tableColumn_className.pack();
		
		/*
		TableViewerColumn viewerColumn_offset = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_offset.setLabelProvider(new OffsetLabelProvider());
		TableColumn tableColumn_offset = viewerColumn_offset.getColumn();
		tableColumn_offset.setText("�I�t�Z�b�g");
//		tableColumn_offset.setWidth(50); // �������� pack()
		tableColumn_offset.pack();
//		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		*/
		
		TableViewerColumn viewerColumn_startLine = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_startLine.setLabelProvider(new StartLineLabelProvider());
		TableColumn tableColumn_startLine = viewerColumn_startLine.getColumn();
		tableColumn_startLine.setText("�J�n�s");
//		tableColumn_startLine.setWidth(50); // �������� pack()
		tableColumn_startLine.pack();
//		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_location = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_location.setLabelProvider(new LocationLabelProvider());
		TableColumn tableColumn_location = viewerColumn_location.getColumn();
		tableColumn_location.setText("���P�[�V����");
		tableColumn_location.setWidth(150); // �������� pack()
//		tableColumn_text.pack();
		
		TableViewerColumn viewerColumn_retType = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_retType.setLabelProvider(new ReturnTypeLabelProvider());
		TableColumn tableColumn_retType = viewerColumn_retType.getColumn();
		tableColumn_retType.setText("�]�܂����Ԃ�l");
		tableColumn_retType.setWidth(320); // �������� pack()
//		tableColumn_retType.pack();
//		viewerColumn.setEditingSupport(new MyEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_text = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_text.setLabelProvider(new TextLabelProvider());
		TableColumn tableColumn_text = viewerColumn_text.getColumn();
		tableColumn_text.setText("�����������o��(�I�������e�L�X�g)");
		tableColumn_text.setWidth(320); // �������� pack()
//		tableColumn_text.pack();
		viewerColumn_text.setEditingSupport(new TextCellEditingSupport(viewer));
		
		TableViewerColumn viewerColumn_keyword = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_keyword.setLabelProvider(new KeywordLabelProvider());
		TableColumn tableColumn_keyword = viewerColumn_keyword.getColumn();
		tableColumn_keyword.setText("���̓L�[���[�h");
		tableColumn_keyword.setWidth(320); // �������� pack()
//		tableColumn_text.pack();
		//�Z�����G�f�B�b�g�\�ɂ���B
		viewerColumn_keyword.setEditingSupport(new KeywordCellEditingSupport(viewer));

		TableViewerColumn viewerColumn_saveTime = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn_saveTime.setLabelProvider(new SaveTimeLabelProvider());
		TableColumn tableColumn_saveTime = viewerColumn_saveTime.getColumn();
		tableColumn_saveTime.setText("�ۑ�����");
		tableColumn_saveTime.setWidth(190); // �������� pack()
		
		viewer.setInput(getItems());
//		viewer.setAllChecked(true);
		
		//���t���b�V���E�A�N�V�����ǉ��B
		registerAction();
	}

	@Override
	public void setFocus() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
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
	    
	  //���Ԃ̍~���Ƀ\�[�g����B
	    TestSiteComparator comp = new TestSiteComparator();
	    Collections.sort(list, comp);
	    
		return list;
	}
	
	private void registerAction(){
		//���t���b�V���A�N�V�������쐬
		refreshAction = new Action(){
			public void run(){
				viewer.setInput(getItems());
			}
		};
		refreshAction.setText("���t���b�V��");
//		refreshAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_REFRESH));
//		refreshAction.setImageDescriptor(Activator.getImageDescriptor(Activator.IMG_REFRESH, "refresh.gif"));
		refreshAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.browser", "$nl$/icons/elcl16/nav_refresh.gif"));

		//�S�I���A�N�V�������쐬
		selectAllAction = new Action(){
			public void run(){
				viewer.setAllChecked(true);
			}
		};
		selectAllAction.setText("�S�đI��");
		selectAllAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui", "$nl$/icons/full/elcl16/step_done.gif"));

		//�S�I�������A�N�V�������쐬
		unSelectAllAction = new Action(){
			public void run(){
				viewer.setAllChecked(false);
			}
		};
		unSelectAllAction.setText("�S�đI������");
		unSelectAllAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.debug.ui", "$nl$/icons/full/dlcl16/disabled_co.gif"));

					
		//�폜�A�N�V�������쐬
		deleteAction = new Action(){
			public void run(){
				//�I�����ꂽ�A�C�e�����폜����B
				TestSite[] list = getCheckedItems();
				
				//�^�X�N���I������Ă��Ȃ��B
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "�^�X�N�̍폜", "�폜����^�X�N���`�F�b�N�{�b�N�X�őI�����Ă��������B");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"�^�X�N�̍폜",
						"�I�����ꂽ "+ list.length + " �̊w�K�^�X�N���폜���܂��B");
			
				//"No"�Ȃ牽�����Ȃ��B
				if(yn == false){
					return;
				}
				//�I�����ꂽ�t�@�C����S�č폜����B
				for(TestSite site:list){
					site.deleteFile();
				}
				//���t���b�V��
				viewer.setInput(getItems());
			}
		};
		deleteAction.setText("�^�X�N�폜");
//		deleteAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_DELETE));
		deleteAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui", "$nl$/icons/full/obj16/delete_obj.gif"));
		

		//�L�[���[�h�v���O���~���O�̃A�N�V�������쐬
		kpAction = new Action(){
			public void run(){
				TestSite[] list = getCheckedItems();
				

				//�^�X�N���I������Ă��Ȃ��B
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "�L�[���[�h�v���O���~���O", "�^�X�N���`�F�b�N�{�b�N�X�őI�����Ă��������B");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"�L�[���[�h�v���O���~���O",
						"�I�����ꂽ "+ list.length + " �̊w�K�^�X�N�ɑ΂��ăL�[���[�h�v���O���~���O���s���܂��B");
			
				//"No"�Ȃ牽�����Ȃ��B
				if(yn == false){
					return;
				}
					
				//�R���\�[����ʕ\��
				Activator.showConsoleView();
				
				//���������Ȃ̂ŁA�W���u���g��
				KeywordProgrammingJob job = new KeywordProgrammingJob("�L�[���[�h�v���O���~���O�̎��s", list);
				job.setUser(true);//���[�U�[�Ƀ|�b�v�A�b�v��\������B
				job.schedule();//�W���u��Eclipse�ɓo�^���Ă����܂��B���Ƃ�Eclipse���K�؂�run�����s���A�������Ă����
			}
		};
		kpAction.setText("�L�[���[�h�v���O���~���O���s");
//		localSearchAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_RUN));
		kpAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.cheatsheets", "$nl$/icons/elcl16/skip_task.gif"));

		
		//���[�J���T�[�`�A�N�V�������쐬
		localSearchAction = new Action(){
			public void run(){
				//�e�t�@�C���ɂ��āA
				//�L�[���[�h�v���O���~���O�����Ă��炻�̌��ʂ�p���ă��[�J���T�[�`���s���B
				TestSite[] list = getCheckedItems();
				

				//�^�X�N���I������Ă��Ȃ��B
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "���[�J���T�[�`", "�^�X�N���`�F�b�N�{�b�N�X�őI�����Ă��������B");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"���[�J���T�[�`",
						"�I�����ꂽ "+ list.length + " �̊w�K�^�X�N�ɑ΂��ă��[�J���T�[�`���s���܂��B");
			
				//"No"�Ȃ牽�����Ȃ��B
				if(yn == false){
					return;
				}
				
				LocalSearch ls = new LocalSearch(list, false);
				
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				
				//�X�e�b�v�����w��
				int numOfSteps = store.getInt(PreferenceInitializer.LOCAL_BATCH_NUMBER_OF_STEPS);
				//�����܂ōs�����ۂ��B
				boolean isConvergence = store.getBoolean(PreferenceInitializer.LOCAL_BATCH_FLAG_CONVERGENCE);
				if(isConvergence == true)
					numOfSteps = -1;
				
				//�R���\�[����ʕ\��
				Activator.showConsoleView();
				
				//���������Ȃ̂ŁA�W���u���g��
				LocalSearchJob job = new LocalSearchJob("���[�J���T�[�`�̎��s", ls, numOfSteps);
				job.setUser(true);//���[�U�[�Ƀ|�b�v�A�b�v��\������B
				job.schedule();//�W���u��Eclipse�ɓo�^���Ă����܂��B���Ƃ�Eclipse���K�؂�run�����s���A�������Ă����
			}
		};
		localSearchAction.setText("���[�J���T�[�`���s");
//		localSearchAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_RUN));
		localSearchAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.cheatsheets", "$nl$/icons/elcl16/start_task.gif"));

		
		//�O���b�h�T�[�`�A�N�V�������쐬
		gridSearchAction = new Action(){
			public void run(){
				//�e�t�@�C���ɂ��āA
				//�L�[���[�h�v���O���~���O�����Ă��炻�̌��ʂ�p���ăO���b�h�T�[�`���s���B
				TestSite[] list = getCheckedItems();
				

				//�^�X�N���I������Ă��Ȃ��B
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "�O���b�h�T�[�`", "�^�X�N���`�F�b�N�{�b�N�X�őI�����Ă��������B");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"�O���b�h�T�[�`",
						"�I�����ꂽ "+ list.length + " �̊w�K�^�X�N�ɑ΂��ăO���b�h�T�[�`���s���܂��B");
			
				//"No"�Ȃ牽�����Ȃ��B
				if(yn == false){
					return;
				}
				
				//�R���\�[����ʕ\��
				Activator.showConsoleView();
				
				//���������Ȃ̂ŁA�W���u���g��
				GridSearchJob job = new GridSearchJob("�O���b�h�T�[�`�̎��s", list);
				job.setUser(true);//���[�U�[�Ƀ|�b�v�A�b�v��\������B
				job.schedule();//�W���u��Eclipse�ɓo�^���Ă����܂��B���Ƃ�Eclipse���K�؂�run�����s���A�������Ă����
			}
		};
		gridSearchAction.setText("�O���b�h�T�[�`���s");
//		gridSearchAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_RUN));
		gridSearchAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.debug.ui", "$nl$/icons/full/obj16/lrun_obj.gif"));

		
		//�ݒ�A�N�V�������쐬
		settingAction = new Action(){
			public void run(){
				//�v���t�@�����X���擾���āA�_�C�A���O�ɕ\������B
				PreferenceManager pm = PlatformUI.getWorkbench( ).getPreferenceManager();
		        IPreferenceNode kp_node = pm.find("jp.ac.hokudai.eng.complex.kussharo.sayuu.kp.preference.page.kp");
		        PreferenceManager pm2 = new PreferenceManager();
		        pm2.addToRoot(kp_node);
		        PreferenceDialog dialog1 = new PreferenceDialog(fParent.getShell(), pm2);
		        dialog1.open();
			}
		};
		
		settingAction.setText("�ݒ�");
//		settingAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_SETTINGS));
		settingAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.debug.ui", "$nl$/icons/full/elcl16/prop_ps.gif"));

		//�v���p�e�B��\������B
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
					
					MessageDialog.openInformation(fParent.getShell(), "�r���[�̃v���p�e�B", "�r���[�ɕ\������Ă���\n�S�^�X�N��= " + sum
							+ "\n�S�p�b�P�[�W��= " + package_sum
							+ "\n�S�N���X��= " + class_sum);
//							+ "\n" + s);
				}
				

			}
		};
		
		countTaskAction.setText("�v���p�e�B");
//				countTaskAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.IMG_SETTINGS));
		countTaskAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui", "$nl$/icons/full/obj16/generic_elements.gif"));

		
		
		//�^�X�N�����A�N�V����
		arrangeTaskAction = new Action(){
			public void run(){
				Object o = viewer.getInput();
				if(o instanceof ArrayList){
					ArrayList<TestSite> ts_arr = (ArrayList<TestSite>)o;
					List<Result> result_list = new ArrayList<Result>(); 
					
					for(TestSite ts: ts_arr){
						String str = ts.getSelectedString();
						int odr = -1;//���ʂ͕s���B
						int numKey = ts.getNumOfKeywords();
						int numLT = ts.getNumOfLocalTypes();
						int numLF = ts.getNumOfLocalFunctions();
//						//generics���폜
//			        	if(!str.contains("<"))
			        		result_list.add(new Result(ts.getId(), str, odr, numKey, numLT, numLF));
					}
					System.out.println("==============�d���������generics���폜����=================================");
					
					//�\�[�g���ďd����������폜����B
//					TreeSet<Result> result_set = new TreeSet<Result>(result_list);
//					Iterator<Result> it = result_set.iterator();
//			        while (it.hasNext()) {
//			        	Result result = it.next();
//			        	//generics���폜
//			        	if(!result.fSelectedString.contains("<"))
//			        		System.out.println(result.fTestSiteId + "\t" + result.fSelectedString);
//			        }
			        
					TreeSet<Result> result_set = new TreeSet<Result>();
					List<Result> deleted_list = new ArrayList<Result>(); 
					
					for(Result r : result_list){
						//�d���ƁAgenerics���폜
						if(!result_set.contains(r) && !r.fSelectedString.contains("<")){
							result_set.add(r);
							System.out.println(r.fTestSiteId + "\t" + r.fSelectedString);
						}else{
							deleted_list.add(r);
						}
					}
					
			        System.out.println("=================�폜���ꂽ���̈ꗗ==============================");
					
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
		arrangeTaskAction.setText("�^�X�N����1");

		
		//����1
		experimentAction = new Action(){
			public void run(){
				//�e�t�@�C���ɂ��āA
				//�L�[���[�h�v���O���~���O�����Ă��炻�̌��ʂ�p���ă��[�J���T�[�`���s���B
				TestSite[] list = getCheckedItems();
				

				//�^�X�N���I������Ă��Ȃ��B
				if(list.length == 0){
					MessageDialog.openWarning(fParent.getShell(), "����1", "�^�X�N���`�F�b�N�{�b�N�X�őI�����Ă��������B");
					return;
				}
				
				boolean yn = MessageDialog.openQuestion(fParent.getShell(),
						"����1",
						"�I�����ꂽ "+ list.length + " �̊w�K�^�X�N�ɑ΂��Ď���1���s���܂��B");
			
				//"No"�Ȃ牽�����Ȃ��B
				if(yn == false){
					return;
				}
				
				LocalSearch2 ls = new LocalSearch2(list, false);
				
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				
				//�X�e�b�v�����w��
				int numOfSteps = store.getInt(PreferenceInitializer.LOCAL_BATCH_NUMBER_OF_STEPS);
				//�����܂ōs�����ۂ��B
				boolean isConvergence = store.getBoolean(PreferenceInitializer.LOCAL_BATCH_FLAG_CONVERGENCE);
				if(isConvergence == true)
					numOfSteps = -1;
				
				//�R���\�[����ʕ\��
				Activator.showConsoleView();
				
				//���������Ȃ̂ŁA�W���u���g��
				LocalSearch2Job job = new LocalSearch2Job("����1�̎��s", ls, numOfSteps);
				job.setUser(true);//���[�U�[�Ƀ|�b�v�A�b�v��\������B
				job.schedule();//�W���u��Eclipse�ɓo�^���Ă����܂��B���Ƃ�Eclipse���K�؂�run�����s���A�������Ă����
			}
		};
		experimentAction.setText("�����P");
		
		//�A�N�V�������c�[���o�[�ƃv���_�E�����j���[�ɑg�ݍ��ށB
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
		
		//�A�N�V�������c�[���o�[�ƃ|�b�v�A�b�v���j���[�ɑg�ݍ��ށB
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
	 * ��ʂ����t���b�V�����A�V���ɓo�^���ꂽ���̂�\������B
	 */
	public void refresh(){
		refreshAction.run();
	}
	
	/*
	 * �`�F�b�N�������̂̂ݎ擾����B
	 * 
	 * 
	 */
	public TestSite[] getCheckedItems(){
		/*
		Object[]����TestSite[]�̃L���X�g�͂ł��Ȃ��B
		TestSite[] site = (TestSite[])viewer.getCheckedElements();
		
		�Q�l�Fhttp://d.hatena.ne.jp/fumokmm/20080902/1220372739
		*/
		Object[] o = viewer.getCheckedElements();
		List<Object> site = Arrays.asList(o);
		return site.toArray(new TestSite[site.size()]);
	}
}
