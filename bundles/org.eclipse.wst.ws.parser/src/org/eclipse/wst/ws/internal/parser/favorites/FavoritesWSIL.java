/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.ws.internal.parser.favorites;

public class FavoritesWSIL extends FavoritesLink implements IFavoritesWSIL
{
  public FavoritesWSIL()
  {
    super();
  }

  public String getName()
  {
    return getWsilUrl();
  }


  public String getWsilUrl()
  {
    return link_.getLocation();
  }

  public void setName(String name)
  {
  }

  public void setWsilUrl(String wsilURL)
  {
  }
}
