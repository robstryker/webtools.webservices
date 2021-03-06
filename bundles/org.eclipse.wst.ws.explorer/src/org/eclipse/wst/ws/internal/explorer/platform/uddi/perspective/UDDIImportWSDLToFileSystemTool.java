/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.ws.internal.explorer.platform.uddi.perspective;

import org.eclipse.wst.ws.internal.explorer.platform.perspective.ImportToFileSystemTool;
import org.eclipse.wst.ws.internal.explorer.platform.perspective.Node;
import org.eclipse.wst.ws.internal.explorer.platform.perspective.ToolManager;
import org.eclipse.wst.ws.internal.explorer.platform.uddi.actions.SelectPropertiesToolAction;
import org.eclipse.wst.ws.internal.explorer.platform.uddi.actions.UDDIImportWSDLToFileSystemAction;

public class UDDIImportWSDLToFileSystemTool extends ImportToFileSystemTool
{
  public UDDIImportWSDLToFileSystemTool(ToolManager toolManager,String alt)
  {
    super(toolManager,alt);
  }

  public final String getSelectToolActionHref(boolean forHistory)
  {
    Node node = toolManager_.getNode();
    return SelectPropertiesToolAction.getActionLink(node.getNodeId(),toolId_,node.getViewId(),node.getViewToolId(),forHistory);
  }

  public final String getActionLink()
  {
    Node node = toolManager_.getNode();
    return UDDIImportWSDLToFileSystemAction.getActionLink(node.getNodeId(),toolId_,node.getViewId(),node.getViewToolId());
  }
}
