/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.ws.internal.consumption.ui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jem.java.JavaClass;
import org.eclipse.jst.j2ee.applicationclient.internal.creation.ApplicationClientNatureRuntime;
import org.eclipse.jst.j2ee.internal.J2EEEditModel;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.internal.ejb.project.EJBNatureRuntime;
import org.eclipse.jst.j2ee.internal.web.operations.J2EEWebNatureRuntime;
import org.eclipse.jst.j2ee.webservice.internal.wsclient.impl.Webservice_clientFactoryImpl;
import org.eclipse.jst.j2ee.webservice.wsclient.Handler;
import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
import org.eclipse.jst.j2ee.webservice.wsclient.WebServicesResource;
import org.eclipse.jst.j2ee.webservice.wsclient.Webservice_clientFactory;
import org.eclipse.jst.ws.internal.common.J2EEUtils;
import org.eclipse.jst.ws.internal.common.JavaMOFUtils;
import org.eclipse.jst.ws.internal.common.ResourceUtils;
import org.eclipse.jst.ws.internal.consumption.ui.widgets.object.HandlerTableItem;
import org.eclipse.wst.command.internal.provisional.env.core.SimpleCommand;
import org.eclipse.wst.command.internal.provisional.env.core.common.Environment;
import org.eclipse.wst.command.internal.provisional.env.core.common.MessageUtils;
import org.eclipse.wst.command.internal.provisional.env.core.common.SimpleStatus;
import org.eclipse.wst.command.internal.provisional.env.core.common.Status;
import org.eclipse.wst.common.internal.emf.utilities.EtoolsCopyUtility;

/*
 * Provide a way to externalize the edited fields and create new handlers
 *  
 */
public class ClientHandlersWidgetOutputCommand extends SimpleCommand {

  //private List handlerTableItems_;
  private Hashtable oldWSServiceRefsToHandlersTable_;

  private Hashtable newWSServiceRefsToHandlersTable_;

//  private WebServiceEditModel wsEditModel_;

  private Hashtable handlersTable_;

  private IProject project_;

  private WebServicesResource wsClientRes_;

  private Collection wsServiceRefs_;

  private J2EEEditModel editModel_;

  private Object accessorKey_;

  public Status execute(Environment env) {
    String pluginId = "org.eclipse.jst.ws.consumption.ui";
    MessageUtils msgUtils_ = new MessageUtils(pluginId + ".plugin", this);
    Status status = new SimpleStatus("");

    try {

      oldWSServiceRefsToHandlersTable_ = new Hashtable();
      newWSServiceRefsToHandlersTable_ = new Hashtable();

      if (wsServiceRefs_ != null) {
        Iterator wsRefsIter = wsServiceRefs_.iterator();
        for (int i = 0; i < wsServiceRefs_.size(); i++) {

          ServiceRef wsServiceRef = (ServiceRef) wsRefsIter.next();

          List wsHandlers = wsServiceRef.getHandlers();
          oldWSServiceRefsToHandlersTable_.put(wsServiceRef, wsHandlers);
          newWSServiceRefsToHandlersTable_.put(wsServiceRef, new ArrayList());
        }

        Enumeration e = handlersTable_.keys();
        while (e.hasMoreElements()) {

          String serviceRefName = (String) e.nextElement();
          List handlerTableItems_ = (List) handlersTable_.get(serviceRefName);
          if (handlerTableItems_ != null) {
            // form Handler tables
            for (int i = 0; i < handlerTableItems_.size(); i++) {

              HandlerTableItem hti = (HandlerTableItem) handlerTableItems_.get(i);
              Object wsModelRef = hti.getWsDescRef();
              if (wsModelRef != null && wsModelRef instanceof ServiceRef) {
                ServiceRef wsRef = (ServiceRef) wsModelRef;

                Object handler = hti.getHandler();
                if (handler != null && handler instanceof Handler) {
                  // clone it
                  Handler clonedHandler = (Handler) EtoolsCopyUtility.createCopy((Handler) handler);
                  ((List) newWSServiceRefsToHandlersTable_.get(wsRef)).add(clonedHandler);
                }
                else {
                  // create it
                  Webservice_clientFactory wsClientFactory = new Webservice_clientFactoryImpl();
                  Handler newHandler = wsClientFactory.createHandler();
                  newHandler.setHandlerName(hti.getHandlerName());

                  JavaClass javaClass = JavaMOFUtils.getJavaClass(hti.getHandlerClassName(), project_);
                  if (javaClass != null) {
                    newHandler.setHandlerClass(javaClass);
                  }

                  ((List) newWSServiceRefsToHandlersTable_.get(wsRef)).add(newHandler);
                }

              }

            }
          }
        }

        // add handlers to ports
        addHandlersToServiceRefs();
        getJ2EEEditModel();
        
        // save the resource
        if (J2EEUtils.getJ2EEVersion(project_) == J2EEVersionConstants.J2EE_1_4_ID) {
          editModel_.save(accessorKey_);
        }
        else {
          if (wsClientRes_!=null)
            wsClientRes_.save(new HashMap());
        }
      }
    }
    catch (Exception e) {
      return new SimpleStatus("", msgUtils_.getMessage("MSG_ERROR_TASK_EXCEPTED"), Status.ERROR, e);
    }
    finally {
      if (editModel_ != null) 
        editModel_.releaseAccess(accessorKey_);
    }
    return status;
  }

  private void addHandlersToServiceRefs() {
    try {
      int key = 0;
      Enumeration refsToHandlers = newWSServiceRefsToHandlersTable_.keys();
      while (refsToHandlers.hasMoreElements()) {
        ServiceRef serviceRef = (ServiceRef) refsToHandlers.nextElement();
        if (serviceRef != null) {

          List handlers = (List) newWSServiceRefsToHandlersTable_.get(serviceRef);
          List modelHandlers = (List) oldWSServiceRefsToHandlersTable_.get(serviceRef);
          modelHandlers.clear();
          modelHandlers.addAll(handlers);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @param handlerTableItems
   *          The handlerTableItems to set.
   */
  //  public void setAllHandlersList(List handlerTableItems) {
  //    this.handlerTableItems_ = handlerTableItems;
  //  }
  public void setHandlersTable(Hashtable handlersTable) {
    this.handlersTable_ = handlersTable;
  }

  /**
   * @param wsEditModel
   *          The wsEditModel to set.
   */
//  public void setWsEditModel(WebServiceEditModel wsEditModel) {
//    this.wsEditModel_ = wsEditModel;
//  }

  public void setClientProject(IProject project) {
    this.project_ = project;
  }

  public void setWsClientResource(WebServicesResource wsRes) {
    this.wsClientRes_ = wsRes;
  }

  public void setWsServiceRefs(Collection wsRefs) {
    this.wsServiceRefs_ = wsRefs;
  }

  public void getJ2EEEditModel() {
  	accessorKey_ = new Object();
    if (ResourceUtils.isWebProject(project_)) {
      J2EEWebNatureRuntime rt = J2EEWebNatureRuntime.getRuntime(project_);
      if (rt != null) {
        editModel_ = rt.getWebAppEditModelForWrite(accessorKey_);
      }
    }
    else if (ResourceUtils.isAppClientProject(project_)){
      ApplicationClientNatureRuntime rt = ApplicationClientNatureRuntime.getRuntime(project_);
      if (rt!=null) {
        editModel_ = rt.getAppClientEditModelForWrite(accessorKey_);
      }
    }
    else if (ResourceUtils.isEJBProject(project_)){
      EJBNatureRuntime rt = EJBNatureRuntime.getRuntime(project_);
      if(rt!=null){
        editModel_ = rt.getEJBEditModelForWrite(accessorKey_);
      }
    }
  }

}