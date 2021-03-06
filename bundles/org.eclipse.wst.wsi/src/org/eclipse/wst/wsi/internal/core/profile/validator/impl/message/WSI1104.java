/*******************************************************************************
 * Copyright (c) 2002-2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.wsi.internal.core.profile.validator.impl.message;

import org.eclipse.wst.wsi.internal.core.profile.validator.impl.BaseMessageValidator;

/**
 * WSI1104.
 * The request message should not contain a content-type of "text/xml".
 */
public class WSI1104 extends BP1104
{

  /**
   * @param BaseMessageValidator
   */
  public WSI1104(BaseMessageValidator impl)
  {
    super(impl);
  }
}
