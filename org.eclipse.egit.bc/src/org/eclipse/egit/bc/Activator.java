package org.eclipse.egit.bc;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.bc.preferences.BeyondCompareEgitPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

public class Activator extends AbstractUIPlugin {
	private static Activator plugin;

	public Activator() {
		Activator.setActivator(this);
	}

	private static void setActivator(Activator a) {
		plugin = a;
	}
	
	public static Activator getDefault() {
		return plugin;
	}
	
	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		BeyondCompareEgitPreferencePage.initializeDefaultPreferences(store);
	}
	
	public static void handleError(String message, Throwable throwable, boolean show) {
		IStatus status = new Status(IStatus.ERROR, getPluginId(), message, throwable);
		int style = StatusManager.LOG;
		if (show)
			style |= StatusManager.SHOW;
		StatusManager.getManager().handle(status, style);
	}
	
	public static void logError(String message, Throwable e) {
		handleError(message, e, false);
	}

}
