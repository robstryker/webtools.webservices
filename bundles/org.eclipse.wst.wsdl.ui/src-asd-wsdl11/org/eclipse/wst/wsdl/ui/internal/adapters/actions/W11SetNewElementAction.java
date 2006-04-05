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
package org.eclipse.wst.wsdl.ui.internal.adapters.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.common.ui.internal.search.dialogs.ComponentSpecification;
import org.eclipse.wst.wsdl.asd.editor.ASDEditorPlugin;
import org.eclipse.wst.wsdl.asd.editor.actions.BaseSelectionAction;
import org.eclipse.wst.wsdl.asd.facade.IParameter;
import org.eclipse.wst.wsdl.ui.internal.adapters.WSDLBaseAdapter;
import org.eclipse.wst.xsd.adt.edit.ComponentReferenceEditManager;
import org.eclipse.wst.xsd.adt.edit.IComponentDialog;
import org.eclipse.wst.xsd.editor.XSDElementReferenceEditManager;

public class W11SetNewElementAction extends BaseSelectionAction {
	public static String ID = "ASDSetNewElementAction";
	protected WSDLBaseAdapter wsdlBaseAdapter;
	
	public W11SetNewElementAction(IWorkbenchPart part)	{
		super(part);
		setId(ID);
		setText("Set New Element...");
//		setImageDescriptor(WSDLEditorPlugin.getImageDescriptor("icons/service_obj.gif"));
	}
	
	public void run() {		
		if (wsdlBaseAdapter == null) {
			if (getSelectedObjects().size() > 0) {
				Object o = getSelectedObjects().get(0);
				if (o instanceof IParameter && o instanceof WSDLBaseAdapter) {
					wsdlBaseAdapter = (WSDLBaseAdapter) o;
				}
			}
		}
		
		if (wsdlBaseAdapter != null) {
			IEditorPart editor = ASDEditorPlugin.getActiveEditor();
			ComponentReferenceEditManager refManager = (ComponentReferenceEditManager) editor.getAdapter(XSDElementReferenceEditManager.class);
			IComponentDialog dialog = refManager.getNewDialog();
			if (dialog.createAndOpen() == Window.OK) {
				ComponentSpecification spec = dialog.getSelectedComponent();
				refManager.modifyComponentReference(wsdlBaseAdapter, spec);
			}
		}
		
		wsdlBaseAdapter = null;
	}
	
	protected boolean calculateEnabled() {
		return true;
	}
}