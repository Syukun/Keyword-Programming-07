package plugin.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import plugin.activator.Activator;

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage  {

	public GeneralPreferencePage(){
		super(GRID);
	    setPreferenceStore(Activator.getDefault().getPreferenceStore());
	    
	}
	
	@Override
	protected void createFieldEditors() {
		// TODO Auto-generated method stub

		addField(new IntegerFieldEditor(PreferenceInitializer.BEST_R, "BEST_Rの値（動的計画法の表の各交点に保持する木の個数の最大値）", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceInitializer.HEIGHT, "HEIGHTの値（動的計画法の表の高さの最大値）", getFieldEditorParent()));
		
		
		
		RadioGroupFieldEditor editor = new RadioGroupFieldEditor(PreferenceInitializer.IMPORT_TYPES_FROM,
				"内部データベース(java.langパッケージ)以外の型と関数情報をインポートする場所を指定.",3,
                new String[][] {
                { "現在編集中のクラスのみ", PreferenceInitializer.IMPORT_TYPES_FROM_MY_CLASS},
                { "現在編集中のクラスが所属するパッケージから", PreferenceInitializer.IMPORT_TYPES_FROM_MY_PACKAGE},
                { "現在編集中のクラスが所属するプロジェクト全体から",  PreferenceInitializer.IMPORT_TYPES_FROM_MY_PROJECT}
                },getFieldEditorParent());      
		addField(editor);
		
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	
}
