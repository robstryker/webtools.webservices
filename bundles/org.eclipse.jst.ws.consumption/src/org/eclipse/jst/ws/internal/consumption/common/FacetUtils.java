/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jst.ws.internal.consumption.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectTemplate;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action;
import org.eclipse.wst.common.project.facet.core.IFacetedProject.Action.Type;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;

public class FacetUtils
{

  /**
   * Returns a list of valid projects. Valid projects include projects with the facets nature or
   * projects with the Java nature.
   * @return IProject[] an array of valid projects
   */
  public static IProject[] getAllProjects()
  {
    //Return all projects in the workspace that have the project facet nature or that do not have the project
    //facet nature but have the Java nature.
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    ArrayList validProjects = new ArrayList();
    for (int i = 0; i < projects.length; i++)
    {
      try
      {
        IFacetedProject facProject = ProjectFacetsManager.create(projects[i]);
        if (facProject != null)
        {
          //Add it to the list
          validProjects.add(projects[i]);
        }
        else
        {
          //Check if it's a Java project        
          IJavaProject javaProject = null;    
          javaProject = JavaCore.create(projects[i]);    
          if (javaProject != null)
          {
            //Add it to the list
            validProjects.add(projects[i]);
          }
        }
      }
      catch (CoreException ce)
      {        
      }      
    }
    return (IProject[])validProjects.toArray(new IProject[]{});
  }
  
  public static Set getTemplates(RequiredFacetVersion[] requiredFacetVersions)
  {
    
    //Get the templates that support the actions
    Set templates = new HashSet();
    
    for( Iterator itr = ProjectFacetsManager.getTemplates().iterator(); itr.hasNext(); )
    {
        final IFacetedProjectTemplate template = (IFacetedProjectTemplate) itr.next();
        String templateId = template.getId();
        if (templateId.indexOf("ear") == -1 && templateId.indexOf("wst.web") == -1) //Don't include the EARs!!
        {
          //TODO final Set initial = template.getInitialProjectFacets(); 
          Set initial = getInitialFacetVersionsFromTemplate(templateId);         
          FacetMatcher fm = match(requiredFacetVersions, initial);
          if (fm.isMatch())
          {
            templates.add(template);
          }
        }
    }    
    
    return templates;
    
  }
  
  public static Set getInitialFacetVersionsFromTemplate(String templateId)
  {
    IFacetedProjectTemplate template = ProjectFacetsManager.getTemplate(templateId);
    Set fixedFacets = template.getFixedProjectFacets(); 
    HashSet initial = new HashSet(); 
    for (Iterator itr2 = fixedFacets.iterator(); itr2.hasNext(); ) 
    { 
      IProjectFacet facet = (IProjectFacet) itr2.next(); 
      IProjectFacetVersion highestFacetVersion = null;
      if (isJavaFacet(facet)) //special case the java facet because 1.4 is a better default than 5.0 for now.
      {
        highestFacetVersion = facet.getVersion("1.4");
      }
      highestFacetVersion = facet.getLatestVersion(); 
      initial.add(highestFacetVersion); 
    }             
    
    return initial;
  }
  
  public static String[] getTemplateLabels(String[] templateIds)
  {
    String[] labels = new String[templateIds.length];
    for (int i=0; i<templateIds.length; i++)
    {
      IFacetedProjectTemplate template = ProjectFacetsManager.getTemplate(templateIds[i]);      
      labels[i] = template.getLabel();
    }
    return labels;
    
  }
  
  public static String getTemplateIdByLabel(String templateLabel)
  {
    for( Iterator itr = ProjectFacetsManager.getTemplates().iterator(); itr.hasNext(); )
    {
        final IFacetedProjectTemplate template = (IFacetedProjectTemplate) itr.next();
        if (template.getLabel().equals(templateLabel))
        {
          return template.getId();

        }
    }
    
    return "";
  }
  
  public static Set getInstallActions(Set projectFacetVersions)
  {
    HashSet actions = new HashSet();
    
    Iterator facets = projectFacetVersions.iterator();
    
    while(facets.hasNext())
    {
      IProjectFacetVersion fv = (IProjectFacetVersion)facets.next();
      Action action = new Action(Type.INSTALL, fv, null);
      actions.add(action);
    }
    
    return actions;
  }
  
  public static FacetMatcher match(RequiredFacetVersion[] requiredFacetVersions, Set projectFacetVersions)
  {
    FacetMatcher fm = new FacetMatcher();
    HashSet facetsToAdd = new HashSet();
    HashSet facetsThatMatched = new HashSet();
    for (int i=0; i<requiredFacetVersions.length; i++)
    {
      RequiredFacetVersion rfv = requiredFacetVersions[i];
      IProjectFacetVersion rpfv = rfv.getProjectFacetVersion();
      String rid = rpfv.getProjectFacet().getId();
      String rv = rpfv.getVersionString();
      boolean facetPresent = false;

      //Is the project facet present? or a later version of applicable.
      Iterator itr = projectFacetVersions.iterator();
      while(itr.hasNext())
      {
        IProjectFacetVersion pfv = (IProjectFacetVersion)itr.next();
        String id = pfv.getProjectFacet().getId();
        String version = pfv.getVersionString();
        if (rid.equals(id))
        {
          if (rv.equals(version))
          {
            //found an exact match
            facetPresent = true;
            facetsThatMatched.add(pfv);
          }
          else
          {
            if (rfv.getAllowNewer())
            {
              if (greaterThan(version, rv))
              {
                //found a match
                facetPresent = true;
                facetsThatMatched.add(pfv);
              }
            }
          }
          //No need to keep iterating since we hit a facet with the same id;
          break;
        }
      }
      
      //if not present, put it in the list to check if it can be added.
      if (!facetPresent)
      {
        facetsToAdd.add(rpfv);
      }
      
    }
    
    //Check if the facetsToAdd can be added
    if (facetsToAdd.size() > 0)
    {
      Set actions = getInstallActions(facetsToAdd);
      if( ProjectFacetsManager.check( projectFacetVersions, actions ).getSeverity() == IStatus.OK )
      {
        //Facets can be added so there is a match
        fm.setMatch(true);
        fm.setFacetsThatMatched(facetsThatMatched);
        fm.setFacetsToAdd(facetsToAdd);        
      }
      else
      {
        fm.setMatch(false);
      }      
    }
    else
    {
      //Facets can be added so there is a match
      fm.setMatch(true);
      fm.setFacetsThatMatched(facetsThatMatched);
      fm.setFacetsToAdd(facetsToAdd);
    }
    
    return fm;
  }
  
  //Methods related to facet runtimes.
  
  public static Set getRuntimes(RequiredFacetVersion[] requiredFacetVersions)
  {
    //Form the sets of IProjectFacetVersions these RequiredFacetVersions represent.
    ArrayList listOfFacetSets = new ArrayList();
    
    HashSet facets = new HashSet();
    int javaFacetIndex = -1;
    for (int i=0; i<requiredFacetVersions.length; i++)
    {
      IProjectFacetVersion pfv = requiredFacetVersions[i].getProjectFacetVersion();
      if (FacetUtils.isJavaFacet(pfv.getProjectFacet()))
      {
        //Remember the index
        javaFacetIndex = i;
      }
      facets.add(requiredFacetVersions[i].getProjectFacetVersion());
    }
    
    listOfFacetSets.add(facets);
    
    //If the java facet was one of the facets in the set, and new versions of java are allowed,
    //create sets that contain the newer permitted versions of the java facets.
    if (javaFacetIndex > -1)
    {
      ArrayList permittedJavaVersions = new ArrayList();
      RequiredFacetVersion rfv = requiredFacetVersions[javaFacetIndex];
      if (rfv.getAllowNewer())
      {
        String version = rfv.getProjectFacetVersion().getVersionString();      
        Set allVersions = rfv.getProjectFacetVersion().getProjectFacet().getVersions();
        Iterator itr = allVersions.iterator();
        while (itr.hasNext())
        {
          IProjectFacetVersion thisPfv = (IProjectFacetVersion)itr.next();
          String thisVersion = thisPfv.getVersionString();
          if (greaterThan(thisVersion, version))
          {
            permittedJavaVersions.add(thisVersion);
          }          
        }
        
        String[] javaVersions = (String[])permittedJavaVersions.toArray(new String[0]);
        
        for (int j=0; j<javaVersions.length; j++)
        {
          HashSet thisFacetSet = new HashSet();
          
          for (int k=0; k<requiredFacetVersions.length; k++)
          {
             if (k==javaFacetIndex)
             {
               IProjectFacetVersion pfv = requiredFacetVersions[k].getProjectFacetVersion().getProjectFacet().getVersion(javaVersions[j]);
               thisFacetSet.add(pfv);
             }
             else
             {
               IProjectFacetVersion pfv = requiredFacetVersions[k].getProjectFacetVersion();
               thisFacetSet.add(pfv);
             }
          }
          
          listOfFacetSets.add(thisFacetSet);          
        }
      }
    }
    
    //Return the union of runtimes for all the facetSets.
    return getRuntimes((Set[])listOfFacetSets.toArray(new Set[0]));
    
  }  
    
  public static Set getRuntimes(Set[] facetSets)  
  {
    HashSet unionSet = new HashSet();
    for (int i=0; i<facetSets.length; i++)
    {
      Set facets = facetSets[i];
      Set runtimes = RuntimeManager.getRuntimes(facets);
      Iterator itr = runtimes.iterator();
      while (itr.hasNext())
      {
        IRuntime runtime = (IRuntime)itr.next();
        if (!unionSet.contains(runtime))
        {
          unionSet.add(runtime);
        }
      }
    }
    return unionSet;
  }
  
  public static boolean doesRuntimeSupportFacets(IRuntime facetRuntime, Set projectFacetVersions)
  {
    Set runtimes = RuntimeManager.getRuntimes(projectFacetVersions);
    Iterator itr = runtimes.iterator();
    while (itr.hasNext())
    {
      IRuntime runtime = (IRuntime)itr.next();
      if (runtime.getName().equals(facetRuntime.getName()))
      {
        return true;
      }
    }
    
    return false;
  }
  
  /*
   * @param versionA version number of the form 1.2.3
   * @param versionA version number of the form 1.2.3
   * @return boolean returns whether versionA is greater than versionB
   */
  private static boolean greaterThan(String versionA, String versionB)
  {
    StringTokenizer stA = new StringTokenizer(versionA, ".");
    StringTokenizer stB = new StringTokenizer(versionB, ".");
    
    int sizeA = stA.countTokens();
    int sizeB = stB.countTokens();
    
    int size;
    if (sizeA < sizeB)
    {
      size = sizeA;
    }
    else
      size = sizeB;
    
    for (int i=0; i<size; i++)
    {
      int a = Integer.parseInt(stA.nextToken());
      int b = Integer.parseInt(stB.nextToken());
      if (a!=b)
      {
        return a > b;
      }      
    }
    
    return sizeA > sizeB;
  }
  
  public static boolean isJavaFacet(IProjectFacet pf)
  {
    if (pf.getId().equals("jst.java"))
      return true;
    else
      return false;
  }
  
}