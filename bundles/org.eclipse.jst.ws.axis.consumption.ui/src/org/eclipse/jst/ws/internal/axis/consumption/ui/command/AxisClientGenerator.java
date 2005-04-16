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
package org.eclipse.jst.ws.internal.axis.consumption.ui.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jst.j2ee.internal.webservices.WebServiceClientGenerator;
import org.eclipse.jst.j2ee.internal.webservices.WebServicesClientDataHelper;
import org.eclipse.jst.ws.internal.axis.consumption.core.command.WSDL2JavaCommand;
import org.eclipse.jst.ws.internal.axis.consumption.ui.env.J2EEEnvironment;
import org.eclipse.jst.ws.internal.axis.consumption.ui.task.AddJarsToProjectBuildPathTask;
import org.eclipse.jst.ws.internal.axis.consumption.ui.task.CopyAxisJarCommand;
import org.eclipse.jst.ws.internal.axis.consumption.ui.task.DefaultsForHTTPBasicAuthCommand;
import org.eclipse.jst.ws.internal.axis.consumption.ui.task.RefreshProjectCommand;
import org.eclipse.jst.ws.internal.axis.consumption.ui.task.Stub2BeanCommand;
import org.eclipse.jst.ws.internal.axis.consumption.ui.task.ValidateWSDLCommand;
import org.eclipse.jst.ws.internal.axis.consumption.ui.util.WSDLUtils;
import org.eclipse.jst.ws.internal.common.EnvironmentUtils;
import org.eclipse.jst.ws.internal.common.ResourceUtils;
import org.eclipse.jst.ws.internal.common.StringToIProjectTransformer;
import org.eclipse.jst.ws.internal.consumption.command.common.BuildProjectCommand;
import org.eclipse.wst.command.env.core.common.SimpleStatus;
import org.eclipse.wst.command.env.core.common.Status;
import org.eclipse.wst.ws.internal.parser.discovery.WebServicesParserExt;
import org.eclipse.wst.ws.internal.parser.wsil.WebServicesParser;

/**
 * 
 */
public class AxisClientGenerator extends WebServiceClientGenerator 
{
  private String pluginId_ = "org.eclipse.jst.ws.axis.consumption.ui";
  
  public IStatus genWebServiceClientArtifacts(WebServicesClientDataHelper dataModel)
  {
  	//get info from the model
  	String wsdlURL = dataModel.getWSDLUrl();
    String project = dataModel.getProjectName();
    String outputWSDLFilePathName = dataModel.getOutputWSDLFileName();
    String serviceQName = dataModel.getServiceQName();
    boolean shouldDeploy = dataModel.shouldDeploy();
    
    
    //Generate the artifacts
    Status status = new SimpleStatus("");
    //AxisClientDefaultingCommand
    AxisClientDefaultingCommand axisClientDefaultingCommand = new AxisClientDefaultingCommand();
                                                                                                axisClientDefaultingCommand.setWsdlURL(wsdlURL);
    axisClientDefaultingCommand.setWebServicesParser(new WebServicesParserExt());
    axisClientDefaultingCommand.setClientProject((IProject)(new StringToIProjectTransformer().transform(project)));
    axisClientDefaultingCommand.setTestProxySelected(false);
    axisClientDefaultingCommand.setIsClientScenario(true);
    axisClientDefaultingCommand.setGenerateProxy(true);
    J2EEEnvironment j2eeEnvironment = new J2EEEnvironment();
    status = axisClientDefaultingCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }
    
//  Figure out if this is a Web, EJB, or AppClient project
    boolean isWebProject = ResourceUtils.isWebProject(axisClientDefaultingCommand.getClientProject());
    
    //DefaultsForHTTPBasicAuthCommand
    DefaultsForHTTPBasicAuthCommand httpCommand = new DefaultsForHTTPBasicAuthCommand();
    httpCommand.setJavaWSDLParam(axisClientDefaultingCommand.getJavaWSDLParam());
    httpCommand.setWsdlServiceURL(axisClientDefaultingCommand.getWsdlURL());
    httpCommand.setWebServicesParser(axisClientDefaultingCommand.getWebServicesParser());
    status = httpCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }
    
    //CopyAxisJarCommand
    //The code generated by the Axis WSDL2Java emitter requires axis.jar in order to compile.
    //In the case of a Web project, we add it to the lib directory.
    //In the case of an EJB or App Client project, we add it as an external JAR. This breaks
    //the team environment scenario. We will have to think of a better way to deal with this 
    //in the future.
    if (isWebProject)
    {
	    CopyAxisJarCommand axjCommand = new CopyAxisJarCommand();
	    axjCommand.setProject(axisClientDefaultingCommand.getClientProject());
	    status = axjCommand.execute(j2eeEnvironment);
	    if (status.getSeverity()!=Status.OK)
	    {
	      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
	    }
    }
    else 
    {
      AddAxisJARToBuildPathCommand addAxisCommand = new AddAxisJARToBuildPathCommand();
      addAxisCommand.setProject(axisClientDefaultingCommand.getClientProject());
      status = addAxisCommand.execute(j2eeEnvironment);
	  if (status.getSeverity() != Status.OK)
      {
        return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
      }      
    }
    
    //AddJarsToProjectBuildPathTask
    AddJarsToProjectBuildPathTask addJarsCommand = new AddJarsToProjectBuildPathTask();
    addJarsCommand.setProject(axisClientDefaultingCommand.getClientProject());
    status = addJarsCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }    
    
    //DefaultsForClientJavaWSDLCommand
    DefaultsForClientJavaWSDLCommand defClientCommand = new DefaultsForClientJavaWSDLCommand();
    defClientCommand.setJavaWSDLParam(axisClientDefaultingCommand.getJavaWSDLParam());
    defClientCommand.setProxyProject(axisClientDefaultingCommand.getClientProject());
    defClientCommand.setWSDLServiceURL(axisClientDefaultingCommand.getWsdlURL());
    status = defClientCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }        
    
    //ValidateWSDLCommand
    ValidateWSDLCommand valWSDLCommand = new ValidateWSDLCommand();
    valWSDLCommand.setWsdlURI(axisClientDefaultingCommand.getWsdlURL());
    valWSDLCommand.setWebServicesParser(axisClientDefaultingCommand.getWebServicesParser());
    status = valWSDLCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }    
    
    //WSDL2JavaCommand
    WSDL2JavaCommand w2jCommand = new WSDL2JavaCommand();
    w2jCommand.setJavaWSDLParam(axisClientDefaultingCommand.getJavaWSDLParam());
    w2jCommand.setWsdlURI(axisClientDefaultingCommand.getWsdlURL());
    status = w2jCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }    

    //RefreshProjectCommand
    RefreshProjectCommand refreshCommand = new RefreshProjectCommand();
    refreshCommand.setProject(axisClientDefaultingCommand.getClientProject());
    status = refreshCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }    
    
    Stub2BeanCommand s2bCommand = new Stub2BeanCommand();
    s2bCommand.setJavaWSDLParam(axisClientDefaultingCommand.getJavaWSDLParam());
    s2bCommand.setWebServicesParser(axisClientDefaultingCommand.getWebServicesParser());
    s2bCommand.setClientProject(axisClientDefaultingCommand.getClientProject());
    status = s2bCommand.execute(j2eeEnvironment); 
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }

    //CopyClientWSDLCommand
    CopyClientWSDLCommand copyCommand = new CopyClientWSDLCommand();
    copyCommand.setWsdlURL(axisClientDefaultingCommand.getWsdlURL());
    copyCommand.setClientWSDLPathName(outputWSDLFilePathName);
    copyCommand.setWsParser(axisClientDefaultingCommand.getWebServicesParser());
    status = copyCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }    
    
    
    //BuildProjectCommand
    BuildProjectCommand buildCommand = new BuildProjectCommand();
    buildCommand.setProject(axisClientDefaultingCommand.getClientProject());
    buildCommand.setForceBuild(true);
    status = buildCommand.execute(j2eeEnvironment);
    if (status.getSeverity()!=Status.OK)
    {
      return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
    }    
        
    //Calculate the service interface and service endpoint interface names
    InterfaceNames interfaceNames = calculateSIandSEIName(axisClientDefaultingCommand.getWsdlURL(), serviceQName, axisClientDefaultingCommand.getWebServicesParser());
    String siName = interfaceNames.getSiName(); 
    String[] seiNames = interfaceNames.getSeiNames();
    if (siName==null || siName.length()==0 || seiNames==null || seiNames.length==0)
    {
      return new org.eclipse.core.runtime.Status(IStatus.ERROR,pluginId_,0,"Problems!!",null);
    }
    
    //Set info on the dataModel
    dataModel.setServiceInterfaceName(siName);
    dataModel.setServiceEndpointInterfaceNames(seiNames);
    dataModel.setDidGenDescriptors(false);
    
    return EnvironmentUtils.convertStatusToIStatus(status, pluginId_);
  }
  
  private InterfaceNames calculateSIandSEIName(String wsdlURL, String serviceQName, WebServicesParser wsParser)
  {
    InterfaceNames iNames = new InterfaceNames();
    String serviceLocalPart = serviceQName.substring(serviceQName.lastIndexOf(':')+1);
    Map pkg2nsMapping = null;
    Definition def = wsParser.getWSDLDefinition(wsdlURL);
 
    Map services = def.getServices();
    Service service = null;
    for (Iterator it = services.values().iterator(); it.hasNext();)
    {
      service = (Service)it.next();
      if (serviceLocalPart.equals(service.getQName().getLocalPart()))
        break;
    }        
      
    if (service!=null)
    {
      String servicePkgName = WSDLUtils.getPackageName(service, pkg2nsMapping);
      String serviceClassName = service.getQName().getLocalPart();
      String jndiName = serviceClassName;
      Map ports = service.getPorts();
      for (Iterator it2 = ports.values().iterator(); it2.hasNext();)
      {
        if (serviceClassName.equals(((Port)it2.next()).getBinding().getPortType().getQName().getLocalPart()))
        {
          serviceClassName = serviceClassName + "_Service";
          break;
        }
      }
      ArrayList seiNames = new ArrayList();
      for (Iterator it2 = ports.values().iterator(); it2.hasNext();)
      {
        Port port = (Port)it2.next();
        SOAPAddress soapAddress = null;
        List extensibilityElements = port.getExtensibilityElements();
        if (extensibilityElements != null)
        {
          for (Iterator it3 = extensibilityElements.iterator(); it3.hasNext();)
          {
            Object object = it3.next();
            if (object instanceof SOAPAddress)
            {
              soapAddress = (SOAPAddress)object;
              break;
            }
          }
        }
        if (soapAddress != null)
        {
          PortType portType = port.getBinding().getPortType();
          QName portTypeQName = portType.getQName();
          String portTypePkgName = WSDLUtils.getPackageName(portType, pkg2nsMapping);
          String portTypeClassName = portTypeQName.getLocalPart();
          if (jndiName.equals(portTypeClassName))
              portTypeClassName = portTypeClassName + "_Port";
          seiNames.add(portTypePkgName+"."+portTypeClassName); 
        }
      }
      iNames.setSiName(servicePkgName+"."+serviceClassName);
      iNames.setSEINames(convertToStringArray(seiNames.toArray()));
    }    
    return iNames;
  }
  
  private String[] convertToStringArray(Object[] a)
  {
  	if (a==null) return new String[0];
  	
  	int length = a.length;
  	String[] sa = new String[length];
  	for (int i=0; i<length; i++)
  	{
  		Object obj = a[i];
  		if (obj instanceof String)
  		{
  			sa[i] = (String)obj;
  		}
  	}
  	return sa;
  }  
  private class InterfaceNames
  {
    private String siName_;
    private String[] seiNames_;
    
    public String getSiName()
    {
      return siName_;
    }

    public String[] getSeiNames()
    {
      return seiNames_;
    }
    
    
    public void setSiName(String siName)
    {
      siName_ = siName;
    }
    
    public void setSEINames(String[] seiNames)
    {
      seiNames_ = seiNames;
    }
    
  }
}
