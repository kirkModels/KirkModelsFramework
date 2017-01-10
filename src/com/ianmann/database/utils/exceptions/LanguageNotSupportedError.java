package com.ianmann.database.utils.exceptions;

import com.ianmann.database.config.Settings;

public class LanguageNotSupportedError extends Error {

	public LanguageNotSupportedError(String _language) {
		super(_language + " is not a supported database language.");
	}
	
	public LanguageNotSupportedError() {
		super(Settings.database.language + " is not a supported database language.");
	}
}
