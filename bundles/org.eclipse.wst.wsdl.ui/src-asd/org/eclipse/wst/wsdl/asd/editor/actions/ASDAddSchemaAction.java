/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.wsdl.asd.editor.actions;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.wsdl.asd.editor.ASDEditorPlugin;
import org.eclipse.wst.wsdl.asd.editor.outline.ICategoryAdapter;
import org.eclipse.wst.wsdl.asd.facade.IDescription;

public class ASDAddSchemaAction extends BaseSelectionAction {
	public static String ID = "ASDAddSchemaAction"; 
	
	public ASDAddSchemaAction(IWorkbenchPart part)	{
		super(part);
		setId(ID);
		setText("Add Schema");
		setImageDescriptor(ASDEditorPlugin.getImageDescriptor("icons/xsd_obj.gif"));
	}
	
	public void run() {
		if (getSelectedObjects().size() > 0) {
			Object o = getSelectedObjects().get(0);
			
			if (o instanceof ICategoryAdapter) {
				o = ((ICategoryAdapter) o).getOwnerDescription();
			}
			
			if (o instanceof IDescription) {
				Command command = ((IDescription) o).getAddSchemaCommand();
			    CommandStack stack = (CommandStack) ASDEditorPlugin.getActiveEditor().getAdapter(CommandStack.class);
			    stack.execute(command);
			}
		}  
	}
	
	protected boolean calculateEnabled() {
		return true;
	}
}