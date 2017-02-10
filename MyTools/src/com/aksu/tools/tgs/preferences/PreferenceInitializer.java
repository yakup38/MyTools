package com.aksu.tools.tgs.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aksu.tools.tgs.TGSPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = TGSPlugin.getDefault().getPreferenceStore();
		// store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		// store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.USERNAME_STRING, "<Please set username>");
		store.setDefault(PreferenceConstants.PASSWORD_STRING, "<Please set password>");
	}

}
