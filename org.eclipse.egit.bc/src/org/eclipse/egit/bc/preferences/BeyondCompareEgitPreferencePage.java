package org.eclipse.egit.bc.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.egit.bc.Activator;
import org.eclipse.egit.bc.UIText;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/** Preferences for our window cache. */
public class BeyondCompareEgitPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	private static final String SETTING_BEYONDCOMPARE_EXECUTABLE_PATH = "egit.bc.BeyondCompareExecutableLocation";
	private static final String DEFAULT_BEYONDCOMPARE_EXECUTABLE_PATH = "C:/Program Files (x86)/Beyond Compare 3/BComp.exe";
	
	
	public BeyondCompareEgitPreferencePage() {
		super(GRID);
		setTitle(UIText.BeyondCompareEgit_PreferencePageTitle);
		ScopedPreferenceStore store = new ScopedPreferenceStore(
				InstanceScope.INSTANCE, Activator.getPluginId());
		setPreferenceStore(store);
	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(
				SETTING_BEYONDCOMPARE_EXECUTABLE_PATH,
				UIText.BeyondCompareEgit_ExecutablePath,
				getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		// Nothing to do
	}
	
	public static void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(SETTING_BEYONDCOMPARE_EXECUTABLE_PATH, DEFAULT_BEYONDCOMPARE_EXECUTABLE_PATH);
	}
	
	public static String getBeyondCompareExecutablePath() {
		IEclipsePreferences d = DefaultScope.INSTANCE.getNode(Activator.getPluginId());
		IEclipsePreferences p = InstanceScope.INSTANCE.getNode(Activator.getPluginId());
		String beyondCompareExecutablePath = p.get(SETTING_BEYONDCOMPARE_EXECUTABLE_PATH, d.get(SETTING_BEYONDCOMPARE_EXECUTABLE_PATH, null));
		return beyondCompareExecutablePath;
	}
	
}
