/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.aksu.tools.tgs.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aksu.tools.tgs.TGSPlugin;

public final class TextifyHandler extends AbstractHandler {

	private TGSPlugin plugin;

	/**
	 * The constructor.
	 */
	public TextifyHandler() {
		plugin = TGSPlugin.getDefault();

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			
			ITextEditor editor = null;

			if (part instanceof ITextEditor) {
				editor = (ITextEditor) part;
			} else if (part instanceof MultiPageEditorPart) {
				Object page = ((MultiPageEditorPart) part).getSelectedPage();
				if (page instanceof ITextEditor)
					editor = (ITextEditor) page;
			}

			if (editor != null) {
				IDocumentProvider provider = editor.getDocumentProvider();
				IDocument document = provider.getDocument(editor.getEditorInput());
				
				
				
				ISelection sel = editor.getSelectionProvider().getSelection();
				if (sel instanceof TextSelection) {
					final TextSelection textSel = (TextSelection) sel;

//					 int lineStart = document.getLineInformationOfOffset(command.offset).getOffset();
//					    String lineContents = document.get(lineStart, command.offset - lineStart);					
					
					int offsetStart = textSel.getOffset();
					int lineStart = document.getLineOfOffset(offsetStart);
					int lineLength = document.getLineLength(lineStart);
					
				    String lineContents = document.get(offsetStart, lineLength);
//				    lineContents = lineContents.replaceAll("(\\r|\\n)", "");
				    String newText = "<xsl:text>" + lineContents ;
				    newText = newText.replaceFirst("\r\n", "</xsl:text>\r\n");
					document.replace(offsetStart, lineLength, newText);
				}
			}
		} catch (Exception ex) {
			plugin.log("An exception occured while textifying raw text.", Status.ERROR, ex);
		}

		return null;

	}

}