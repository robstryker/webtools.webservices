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
package org.eclipse.wst.wsdl.ui.internal.adapters.basic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.wsdl.Binding;
import org.eclipse.wst.wsdl.Port;
import org.eclipse.wst.wsdl.asd.editor.actions.ASDAddEndPointAction;
import org.eclipse.wst.wsdl.asd.editor.actions.ASDDeleteAction;
import org.eclipse.wst.wsdl.asd.editor.actions.ASDSetExistingBindingAction;
import org.eclipse.wst.wsdl.asd.editor.actions.ASDSetNewBindingAction;
import org.eclipse.wst.wsdl.asd.editor.actions.BaseSelectionAction;
import org.eclipse.wst.wsdl.asd.editor.outline.ITreeElement;
import org.eclipse.wst.wsdl.asd.facade.IASDObject;
import org.eclipse.wst.wsdl.asd.facade.IASDObjectListener;
import org.eclipse.wst.wsdl.asd.facade.IBinding;
import org.eclipse.wst.wsdl.asd.facade.IEndPoint;
import org.eclipse.wst.wsdl.asd.facade.IService;
import org.eclipse.wst.wsdl.binding.http.HTTPAddress;
import org.eclipse.wst.wsdl.binding.soap.SOAPAddress;
import org.eclipse.wst.wsdl.ui.internal.WSDLEditorPlugin;
import org.eclipse.wst.wsdl.ui.internal.adapters.WSDLBaseAdapter;
import org.eclipse.wst.wsdl.ui.internal.adapters.commands.W11DeleteCommand;
import org.eclipse.wst.wsdl.ui.internal.adapters.commands.W11SetAddressCommand;
import org.eclipse.wst.wsdl.ui.internal.adapters.commands.W11SetBindingCommand;
import org.eclipse.wst.wsdl.ui.internal.adapters.specialized.W11AddressExtensibilityElementAdapter;

public class W11EndPoint extends WSDLBaseAdapter implements IEndPoint, IASDObjectListener {
 
    protected List addressExtensiblityElements = null;
    protected List thingsToListenTo = null;
    
	public String getAddress() {
        List list = getAddressExtensiblityElements();
        if (list.size() > 0)
        {
          W11AddressExtensibilityElementAdapter addressEE = (W11AddressExtensibilityElementAdapter)list.get(0);
          return addressEE.getLocationURI();
        }	
		return "";
	}
    
    protected List getAddressExtensiblityElements()
    {    
      if (addressExtensiblityElements == null || addressExtensiblityElements.size() == 0)
      {
        addressExtensiblityElements = new ArrayList();
        thingsToListenTo = new ArrayList();
        Port port = (Port) getTarget();          
        for (Iterator it = port.getEExtensibilityElements().iterator(); it.hasNext(); )
        {   
          Notifier item = (Notifier)it.next();
          Adapter adapter = createAdapter(item);
          if (adapter instanceof W11AddressExtensibilityElementAdapter)
          {  
            addressExtensiblityElements.add(adapter);
          }             
          if (adapter instanceof IASDObject)
          {  
            thingsToListenTo.add(adapter);
          }  
        }
        for (Iterator i = thingsToListenTo.iterator(); i.hasNext(); )
        {
          IASDObject object = (IASDObject)i.next();
          object.registerListener(this);
        }  
      }  
      return addressExtensiblityElements;
    }
    
    protected void clearAddressExtensiblityElements()
    {    
      if (thingsToListenTo != null)
      {  
        for (Iterator i = thingsToListenTo.iterator(); i.hasNext(); )
        {
          IASDObject object = (IASDObject)i.next();
          object.unregisterListener(this);
        }
      }
      thingsToListenTo = null;
      addressExtensiblityElements = null;
    }

	public IBinding getBinding() {
		if (getPort().getEBinding() != null) {
			return (IBinding) createAdapter(getPort().getEBinding());
		}
		
		return null;
	}

	public String getName() {
		return getPort().getName();
	}

	public String getTypeName() {
		String value = "";
		List eeElements = getPort().getEExtensibilityElements();
		if (eeElements.size() > 0) {
			Object object = eeElements.get(0);
			if (object instanceof SOAPAddress) {
				value = ((SOAPAddress) object).getLocationURI();
			}
			else if (object instanceof HTTPAddress) {
				value = ((HTTPAddress) object).getLocationURI();
			}
		}
		
		if (value == null || value.equals("")) {
			value = "No Address";
		}
		
		return value;
	}
	
	public Object getType() {
		return getBinding();
	}

	private Port getPort() {
		return (Port) target;
	}
	
	public IService getOwnerService() {
		return (IService) owner;
	}
	
	public String[] getActions(Object object) {
		String[] actionIDs = new String[6];
		actionIDs[0] = ASDAddEndPointAction.ID;
		actionIDs[1] = BaseSelectionAction.SUBMENU_START_ID + "Set Binding";
		actionIDs[2] = ASDSetNewBindingAction.ID;
		actionIDs[3] = ASDSetExistingBindingAction.ID;
		actionIDs[4] = BaseSelectionAction.SUBMENU_END_ID;
		actionIDs[5] = ASDDeleteAction.ID;
		
		return actionIDs;
	}
	
	public Command getSetBindingCommand(IBinding binding) {
		W11Binding w11Binding = (W11Binding) binding;
		return new W11SetBindingCommand((Port) target, (Binding) w11Binding.getTarget());
	}
	
	public Command getSetAddressCommand(String newAddress) {
		return new W11SetAddressCommand((Port) this.getTarget(), newAddress);
	}
	public Command getDeleteCommand() {
		return new W11DeleteCommand(this);
	}
    
    public void propertyChanged(Object object, String property)
    {
      // this is called when one of the 'address' extensibility element adapters we're listening to changes
      //
      clearAddressExtensiblityElements();      
      notifyListeners(this, null);
    }
	
	public Image getImage() {
		return WSDLEditorPlugin.getInstance().getImage("icons/port_obj.gif");
	}
	
	public String getText() {
		return "port";
	}
	
	public ITreeElement[] getChildren() {
		return ITreeElement.EMPTY_LIST;
	}
	
	public boolean hasChildren() {
		return false;
	}
	
	public ITreeElement getParent() {
		return null;
	}
}