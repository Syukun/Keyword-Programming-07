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

		addField(new IntegerFieldEditor(PreferenceInitializer.BEST_R, "BEST_R�̒l�i���I�v��@�̕\�̊e��_�ɕێ�����؂̌��̍ő�l�j", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceInitializer.HEIGHT, "HEIGHT�̒l�i���I�v��@�̕\�̍����̍ő�l�j", getFieldEditorParent()));
		
		
		
		RadioGroupFieldEditor editor = new RadioGroupFieldEditor(PreferenceInitializer.IMPORT_TYPES_FROM,
				"�����f�[�^�x�[�X(java.lang�p�b�P�[�W)�ȊO�̌^�Ɗ֐������C���|�[�g����ꏊ���w��.",3,
                new String[][] {
                { "���ݕҏW���̃N���X�̂�", PreferenceInitializer.IMPORT_TYPES_FROM_MY_CLASS},
                { "���ݕҏW���̃N���X����������p�b�P�[�W����", PreferenceInitializer.IMPORT_TYPES_FROM_MY_PACKAGE},
                { "���ݕҏW���̃N���X����������v���W�F�N�g�S�̂���",  PreferenceInitializer.IMPORT_TYPES_FROM_MY_PROJECT}
                },getFieldEditorParent());      
		addField(editor);
		
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	
}
