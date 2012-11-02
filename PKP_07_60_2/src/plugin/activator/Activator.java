package plugin.activator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import keywordProgramming.ExplanationVector;
import keywordProgramming.KeywordProgramming;
import logging.LogControl;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import plugin.preference.PreferenceInitializer;
import plugin.testSite.TestSite;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup{

	// The plug-in ID
	public static final String PLUGIN_ID = "jp.ac.hokudai.eng.complex.kussharo.sayuu.kp";

	// The shared instance
	private static Activator plugin;

	private static Bundle bundle;

	private static ImageRegistry registry;
	private static ImageRegistry myRegistry;	//view�쐬�̂Ƃ���initializeImageRegistry���܂��Ă΂�Ă��Ȃ��Ƃ��p�B
	
	// ���̓t�@�C����
	static final String classesFileName = "sub_class.txt";
	static final String functionsFileName = "function.txt";

	//�C���[�W��
	public static final String IMG_REFRESH = "refresh";
	public static final String IMG_SAVE_EDIT = "save_edit";
	public static final String IMG_UPDATE = "update";
	public static final String IMG_RUN = "run";
	public static final String IMG_SETTINGS = "settings";
	public static final String IMG_PROPERTIES = "properties";
	public static final String IMG_DELETE = "delete";
	public static final String IMG_EXPORT = "export";

	//�R���\�[��
	private static MessageConsole console;
	//�R���\�[���}�l�[�W���[
	private static IConsoleManager consoleManager;
	//�R���\�[���X�g���[��
	public static MessageConsoleStream consoleStream;
	
	/*
	 * �C���|�[�g���̂݁A
	 * �ʂɏ������悤�Ƃ�����
	 * ����earlyStartup�i�j�Ń��X�i��o�^���悤�Ƃ������A
	 * 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(listener);
	 * �����ŁA�܂�WorkbenchWindow��Active�ɂȂ��Ă��Ȃ��̂ŁA
	 * ���X�i��o�^�ł��Ȃ��I
	 * 
	 */
    
	/**
	 * The constructor
	 */
	public Activator() {
		bundle = Platform.getBundle(Activator.PLUGIN_ID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		//earlyStartup();
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	protected void initializeImageRegistry(ImageRegistry registry) {
		super.initializeImageRegistry(registry);
		this.registry = registry;
		registerImages();
		// ..���ɂ��o�^
	}
	
	public static void registerImages() {
		registerImage(registry, IMG_REFRESH, "refresh.gif");
		registerImage(registry, IMG_SAVE_EDIT, "save_edit.gif");
		registerImage(registry, IMG_UPDATE, "update.gif");
		registerImage(registry, IMG_RUN, "run.gif");
		registerImage(registry, IMG_SETTINGS, "settings.gif");
		registerImage(registry, IMG_PROPERTIES, "properties.gif");
		registerImage(registry, IMG_DELETE, "delete.gif");
		registerImage(registry, IMG_EXPORT, "export.gif");
		// ..���ɂ��o�^
	}
	
	private static void registerImage(ImageRegistry registry, String key,String fileName){
		try {
			IPath path = new Path("icons/" + fileName);
			URL url = FileLocator.find(bundle, path, null);
			if (url != null) {
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				if(registry != null)
					registry.put(key, desc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//�r���[����Ăяo��
	public static ImageDescriptor getImageDescriptor(String key,String fileName){
		try {
			IPath path = new Path("icons/" + fileName);
			URL url = FileLocator.find(bundle, path, null);
			LogControl log = new LogControl("");
			log.println("url:" + url.toString());
			if (url != null) {
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				log.println("desc:" + desc.toString());
				return desc;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void earlyStartup() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		
		//�v���t�@�����X�Œ�߂��l�̃Z�b�g
		IPreferenceStore store = getDefault().getPreferenceStore();
		KeywordProgramming.BEST_R = store.getInt(PreferenceInitializer.BEST_R);
		KeywordProgramming.HEIGHT = store.getInt(PreferenceInitializer.HEIGHT);
		
		//���������ӂ���B
		ExplanationVector.setWeight(-store.getDouble(PreferenceInitializer.INITIAL_WEIGHT_0), 0);
		ExplanationVector.setWeight(store.getDouble(PreferenceInitializer.INITIAL_WEIGHT_1), 1);
		ExplanationVector.setWeight(-store.getDouble(PreferenceInitializer.INITIAL_WEIGHT_2), 2);
		ExplanationVector.setWeight(store.getDouble(PreferenceInitializer.INITIAL_WEIGHT_3), 3);
	
		//�����v��Ȃ��B
		ExplanationVector.setStep(store.getDouble(PreferenceInitializer.LOCAL_STEP_WIDTH_0), 0);
		ExplanationVector.setStep(store.getDouble(PreferenceInitializer.LOCAL_STEP_WIDTH_1), 1);
		ExplanationVector.setStep(store.getDouble(PreferenceInitializer.LOCAL_STEP_WIDTH_2), 2);
		ExplanationVector.setStep(store.getDouble(PreferenceInitializer.LOCAL_STEP_WIDTH_3), 3);
		
		//���X�i�̐ݒ�B
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				// TODO Auto-generated method stub
				String propertyName = event.getProperty();
				Object newValue = event.getNewValue();
				if(propertyName.equals(PreferenceInitializer.HEIGHT)){
					KeywordProgramming.HEIGHT = (Integer) event.getNewValue();
				}else if(propertyName.equals(PreferenceInitializer.BEST_R)){
					KeywordProgramming.BEST_R = (Integer) event.getNewValue();
				}else if(propertyName.equals(PreferenceInitializer.INITIAL_WEIGHT_0)){
					ExplanationVector.setWeight(-(Double)newValue, 0);
				}else if(propertyName.equals(PreferenceInitializer.INITIAL_WEIGHT_1)){
					ExplanationVector.setWeight((Double)newValue, 1);
				}else if(propertyName.equals(PreferenceInitializer.INITIAL_WEIGHT_2)){
					ExplanationVector.setWeight(-(Double)newValue, 2);
				}else if(propertyName.equals(PreferenceInitializer.INITIAL_WEIGHT_3)){
					ExplanationVector.setWeight((Double)newValue, 3);
				}else if(propertyName.equals(PreferenceInitializer.LOCAL_STEP_WIDTH_0)){
					ExplanationVector.setStep((Double)newValue, 0);
				}else if(propertyName.equals(PreferenceInitializer.LOCAL_STEP_WIDTH_1)){
					ExplanationVector.setStep((Double)newValue, 1);
				}else if(propertyName.equals(PreferenceInitializer.LOCAL_STEP_WIDTH_2)){
					ExplanationVector.setStep((Double)newValue, 2);
				}else if(propertyName.equals(PreferenceInitializer.LOCAL_STEP_WIDTH_3)){
					ExplanationVector.setStep((Double)newValue, 3);
				}
			}
		});
		
		//�v���O�C���̒��ɂ���t�@�C���̓ǂݍ���
		
		//classes�t�@�C��
		Path c_path = new Path(classesFileName);
		URL c_fileURL = FileLocator.find(bundle, c_path, null);
		InputStream c_in;
		//functions�t�@�C��
		Path f_path = new Path(functionsFileName);
		URL f_fileURL = FileLocator.find(bundle, f_path, null);
		InputStream f_in;
		
		try {
			c_in = c_fileURL.openStream();
			BufferedReader c_reader = new BufferedReader(new InputStreamReader(c_in));
			f_in = f_fileURL.openStream();
			BufferedReader f_reader = new BufferedReader(new InputStreamReader(f_in));
			KeywordProgramming.loadFiles(c_reader, f_reader);
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
		}
	
		//���O�t�@�C���̓ǂݍ���
//		int numOfFiles = store.getInt(PreferenceInitializer.NUMBER_OF_PAST_LOG_FOR_ONLINE);
		int numOfFiles = 50;
		TestSite.loadLogFiles(numOfFiles);
		
		//�o�̓R���\�[���̗p��
		createConsoleStream();
	}
	
	public void createConsoleStream(){
		//�o�̓R���\�[���̗p��
		console = new MessageConsole("�L�[���[�h�v���O���~���O", null);
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.addConsoles(new IConsole[]{console});
		consoleStream = console.newMessageStream();
	}
	
	public static void showConsoleView(){
		consoleManager.showConsoleView(console);
	}
}
