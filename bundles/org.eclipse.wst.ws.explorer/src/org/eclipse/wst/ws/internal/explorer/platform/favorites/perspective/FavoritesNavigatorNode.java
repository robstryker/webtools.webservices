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

package org.eclipse.wst.ws.internal.explorer.platform.favorites.perspective;

import org.eclipse.wst.ws.internal.explorer.platform.datamodel.TreeElement;
import org.eclipse.wst.ws.internal.explorer.platform.favorites.actions.FavoritesToggleNodeAction;
import org.eclipse.wst.ws.internal.explorer.platform.favorites.actions.SelectFavoritesNodeAction;
import org.eclipse.wst.ws.internal.explorer.platform.perspective.Node;
import org.eclipse.wst.ws.internal.explorer.platform.perspective.NodeManager;

public abstract class FavoritesNavigatorNode extends Node {

    public FavoritesNavigatorNode(TreeElement treeElement, NodeManager nodeManager, int nodeDepth, String imagePath) {
        super(treeElement, nodeManager, nodeDepth, imagePath);
    }

    protected String getToggleNodeActionHref() {
        return FavoritesToggleNodeAction.getActionLink(nodeId_,isOpen_);
    }

    protected String getLinkActionHref() {
        return SelectFavoritesNodeAction.getActionLink(nodeId_, false);
    }

}