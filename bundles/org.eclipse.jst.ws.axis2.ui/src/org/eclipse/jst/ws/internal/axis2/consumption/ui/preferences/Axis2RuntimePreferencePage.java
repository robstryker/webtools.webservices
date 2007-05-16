/*******************************************************************************
 * Copyright (c) 2007 WSO2 Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * WSO2 Inc. - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20070130   168762 sandakith@wso2.com - Lahiru Sandakith, Initial code to introduse the Axis2 
 * 										  runtime to the framework for 168762
 * 20070425   183046 sandakith@wso2.com - Lahiru Sandakith
 * 20070501   180284 sandakith@wso2.com - Lahiru Sandakith
 * 20070511   186440 sandakith@wso2.com - Lahiru Sandakith fix 186440
 * 20070513   186430 sandakith@wso2.com - Lahiru Sandakith, fix for 186430
 *										  Text not accessible on AXIS2 wizard pages.
 *******************************************************************************/
package org.eclipse.jst.ws.internal.axis2.consumption.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jst.ws.axis2.core.plugin.data.ServerModel;
import org.eclipse.jst.ws.axis2.core.plugin.messages.Axis2CoreUIMessages;
import org.eclipse.jst.ws.axis2.core.utils.Axis2CoreUtils;
import org.eclipse.jst.ws.axis2.core.utils.RuntimePropertyUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Axis2RuntimePreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button browseButton; 
	private Text axis2Path; 
	private Text statusLabel;
	@SuppressWarnings("unused")
	private IStatus status = Status.OK_STATUS;
	private boolean webappExist = false;
	private boolean isWar = false;
	private String statusBanner = null;
	

	  
	protected Control createContents(Composite superparent) {
		status = Status.OK_STATUS;
		
		final Composite  mainComp = new Composite( superparent, SWT.NONE );
		
		TabFolder axis2PreferenceTab = new TabFolder(mainComp, SWT.NONE);
		TabItem runtimeInstalLocationItem = new TabItem(axis2PreferenceTab, SWT.NONE);
		runtimeInstalLocationItem.setText(Axis2CoreUIMessages.AXIS2_RUNTIME);
		runtimeInstalLocationItem.setToolTipText(Axis2CoreUIMessages.AXIS2_RUNTIME_TOOLTIP);
		
		//-----------------------------Axis2 Rintume Location Group------------------------------//
		Group runtimeGroup = new Group(axis2PreferenceTab, SWT.NONE);
		runtimeGroup.setText(Axis2CoreUIMessages.AXIS2_RUNTIME_LOCATION);
		runtimeInstalLocationItem.setControl(runtimeGroup);
		runtimeGroup.setToolTipText(Axis2CoreUIMessages.AXIS2_RUNTIME_TOOLTIP);
		
		Label label = new Label( runtimeGroup, SWT.NONE );
		label.setText( Axis2CoreUIMessages.AXIS2_LOCATION );
		label.setLocation(10,30);
		label.setSize(100,20);
		
		axis2Path = new Text( runtimeGroup, SWT.BORDER );
		String serverPath = null;
		if (ServerModel.getAxis2ServerPath()==null||ServerModel.getAxis2ServerPath().equals("")){
			serverPath = (RuntimePropertyUtils.getServerPathFromPropertiesFile() == null) ? "" 
					: RuntimePropertyUtils.getServerPathFromPropertiesFile();
			axis2Path.setText(serverPath);
			ServerModel.setAxis2ServerPath( serverPath );
		}else{
			axis2Path.setText(ServerModel.getAxis2ServerPath());
			serverPath = ServerModel.getAxis2ServerPath();
		}

		webappExist =runtimeExist(serverPath);
		if(isWar){
			updateWarStatus(true);
		}else{
			updateWarStatus(false);
		}
		axis2Path.setLocation(110,30);
		axis2Path.setSize(400, 20);
		axis2Path.addModifyListener( new ModifyListener(){
			public void modifyText(ModifyEvent e){
				ServerModel.setAxis2ServerPath( axis2Path.getText() );
				webappExist =runtimeExist(axis2Path.getText());
				status = RuntimePropertyUtils.writeServerPathToPropertiesFile(
						axis2Path.getText());
				if (webappExist) {
					status = Status.OK_STATUS;
					statusUpdate(true);
				}else{
					status = Status.CANCEL_STATUS;
					statusUpdate(false);
				}
			}
		});
		browseButton = new Button( runtimeGroup, SWT.NONE );
		browseButton.setText(Axis2CoreUIMessages.LABEL_BROUSE);
		browseButton.setLocation(520,30);
		browseButton.setSize(70, 20);
		browseButton.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				handleBrowse(mainComp.getShell());
			}     
		}); 
	
		if(axis2Path.getText().equals(Axis2CoreUIMessages.NULL)){
			status = new Status( IStatus.ERROR, 
					"id", 
					0, 
					Axis2CoreUIMessages.ERROR_INVALID_AXIS2_SERVER_LOCATION, 
					null ); 
		}
		
		
		Text statusLabel = new Text(runtimeGroup,SWT.BACKGROUND | SWT.READ_ONLY | SWT.CENTER | SWT.WRAP);
		statusLabel.setLocation(20,100);
		statusLabel.setSize(560,40);
		
		if (axis2Path.getText().equals("")) {
			statusBanner = Axis2CoreUIMessages.LABEL_AXIS2_RUNTIME_NOT_EXIT;
		} else if ( !axis2Path.getText().equals("") && (!webappExist)) {
			statusBanner = Axis2CoreUIMessages.LABEL_AXIS2_RUNTIME_LOAD_ERROR;
		}else{
			statusBanner = Axis2CoreUIMessages.LABEL_AXIS2_RUNTIME_LOAD;
		}
		statusLabel.setText(statusBanner);
		
		webappExist =runtimeExist(axis2Path.getText());
		
		TabItem codegenPreferencesItem = new TabItem(axis2PreferenceTab, SWT.NONE);
		codegenPreferencesItem.setText(Axis2CoreUIMessages.AXIS2_PREFERENCES);
		codegenPreferencesItem.setToolTipText(Axis2CoreUIMessages.AXIS2_PREFERENCES_TOOLTIP);
		
		//--------------------------------Axis2 Runtime Preferences------------------------------//
		
		Group codegenGroup = new Group(axis2PreferenceTab, SWT.NONE);
		codegenGroup.setText(Axis2CoreUIMessages.AXIS2_RUNTIME_PREFERENCES);
		codegenPreferencesItem.setControl(codegenGroup);
		codegenGroup.setToolTipText(Axis2CoreUIMessages.AXIS2_PREFERENCES_TOOLTIP);
		
		//Service Codegen Options
		Text serviceCodegenLabel = new Text(codegenGroup,SWT.BACKGROUND | SWT.READ_ONLY);
		serviceCodegenLabel.setText( Axis2CoreUIMessages.LABEL_WEB_SERVICE_CODEGEN);
		serviceCodegenLabel.setLocation(10,30);
		serviceCodegenLabel.setSize(220,20);
		
		//Data binding
		Label databindingLabel = new Label( codegenGroup, SWT.NONE );
		databindingLabel.setText( Axis2CoreUIMessages.LABEL_DATABINDING);
		databindingLabel.setLocation(10,60);
		databindingLabel.setSize(200,20);
		
		final Text databindingText = new Text( codegenGroup,SWT.BORDER );
		databindingText.setText(ServerModel.getServiceDatabinding());
		databindingText.addModifyListener( new ModifyListener() {
			public void modifyText(ModifyEvent e){
				ServerModel.setServiceDatabinding( databindingText.getText() );
			}
		});
		databindingText.setLocation(220,60);
		databindingText.setSize(100,20);
		
		// generate test case option
		final Button testCaseCheckBoxButton = new Button(codegenGroup, SWT.CHECK);
		testCaseCheckBoxButton.setText(Axis2CoreUIMessages.LABEL_GENERATE_TESTCASE_CAPTION);
		testCaseCheckBoxButton.setSelection(ServerModel.isServiceTestcase());
		testCaseCheckBoxButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setServiceTestcase(testCaseCheckBoxButton.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		testCaseCheckBoxButton.setLocation(10, 90);
		testCaseCheckBoxButton.setSize(300, 15);

		//model.setServerXMLCheck(true);

		//the server side interface option
		final Button generateServerSideInterfaceCheckBoxButton = 
							new Button(codegenGroup, SWT.CHECK);
		generateServerSideInterfaceCheckBoxButton.setText(
				Axis2CoreUIMessages.LABEL_GENERATE_SERVERSIDE_INTERFACE);
		generateServerSideInterfaceCheckBoxButton.setSelection(
									ServerModel.isServiceInterfaceSkeleton());
		generateServerSideInterfaceCheckBoxButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setServiceInterfaceSkeleton(
						generateServerSideInterfaceCheckBoxButton.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		generateServerSideInterfaceCheckBoxButton.setLocation(10, 120);
		generateServerSideInterfaceCheckBoxButton.setSize(300, 15);

		// generate all
		final Button generateAllCheckBoxButton = new Button(codegenGroup, SWT.CHECK);
		generateAllCheckBoxButton.setSelection(ServerModel.isServiceGenerateAll());
		generateAllCheckBoxButton.setText(Axis2CoreUIMessages.LABEL_GENERATE_ALL);
		generateAllCheckBoxButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setServiceGenerateAll(generateAllCheckBoxButton.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		generateAllCheckBoxButton.setLocation(10, 150);
		generateAllCheckBoxButton.setSize(350, 15);
		
		///////////////////////////////////////////////////////////////////////////////////////////
		
		//seperator
		Label seperatorLabel0 = new Label( codegenGroup, SWT.SEPARATOR|SWT.BORDER);
		seperatorLabel0.setLocation(10,185);
		seperatorLabel0.setSize(570,1);
		
		///Client Codegen Options
		Text clientCodegenLabel = new Text(codegenGroup,SWT.BACKGROUND | SWT.READ_ONLY);
		clientCodegenLabel.setText( Axis2CoreUIMessages.LABEL_WEB_SERVICE_CLIENT_CODEGEN);
		clientCodegenLabel.setLocation(10,200);
		clientCodegenLabel.setSize(220,20);
		
		//Client type label 
		Label clientLabel = new Label(codegenGroup, SWT.HORIZONTAL | SWT.NULL);
		clientLabel.setText(Axis2CoreUIMessages.LABEL_CLIENT_SIDE);
		clientLabel.setLocation(10,240);
		clientLabel.setSize(70,20); 
		
		//client side buttons
		final Button syncAndAsyncRadioButton = new Button(codegenGroup, SWT.RADIO);
		syncAndAsyncRadioButton.setText(Axis2CoreUIMessages.LABEL_SYNC_AND_ASYNC);
		syncAndAsyncRadioButton.setVisible(true);
		syncAndAsyncRadioButton.setSelection(
				((ServerModel.isAsync() || ServerModel.isSync())==false)
				?true
				:(ServerModel.isAsync() && ServerModel.isSync()));
		syncAndAsyncRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setSync(syncAndAsyncRadioButton.getSelection());
				ServerModel.setAsync(syncAndAsyncRadioButton.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		syncAndAsyncRadioButton.setLocation(80,240);
		syncAndAsyncRadioButton.setSize(190,20); 
		
		final Button syncOnlyRadioButton = new Button(codegenGroup, SWT.RADIO);
		syncOnlyRadioButton.setText(Axis2CoreUIMessages.LABEL_SYNC);
		syncOnlyRadioButton.setSelection(ServerModel.isSync() && !ServerModel.isAsync() );
		syncOnlyRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setSync(syncOnlyRadioButton.getSelection());
				ServerModel.setAsync(!syncOnlyRadioButton.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		syncOnlyRadioButton.setLocation(280,240);
		syncOnlyRadioButton.setSize(170,20); 

		final Button asyncOnlyRadioButton = new Button(codegenGroup, SWT.RADIO);
		asyncOnlyRadioButton.setText(Axis2CoreUIMessages.LABEL_ASYNC);
		asyncOnlyRadioButton.setSelection(ServerModel.isAsync() && !ServerModel.isSync());
		asyncOnlyRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setAsync(asyncOnlyRadioButton.getSelection());
				ServerModel.setSync(!asyncOnlyRadioButton.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		asyncOnlyRadioButton.setLocation(460,240);
		asyncOnlyRadioButton.setSize(170,20);
		
		//Data binding
		Label clientDatabindingLabel = new Label( codegenGroup, SWT.NONE );
		clientDatabindingLabel.setText( Axis2CoreUIMessages.LABEL_DATABINDING);
		clientDatabindingLabel.setLocation(10,270);
		clientDatabindingLabel.setSize(200,20);
		
		final Text databindingText1 = new Text( codegenGroup, SWT.BORDER );
		databindingText1.setText(ServerModel.getCleintDatabinding());
		databindingText1.addModifyListener( new ModifyListener() {
			public void modifyText(ModifyEvent e){
				ServerModel.setAxis2ServerPath( databindingText1.getText() );
			}
		});
		databindingText1.setLocation(220,270);
		databindingText1.setSize(100,20);
		
		
		// generate test case option
		final Button clientTestCaseCheckBoxButton = new Button(codegenGroup, SWT.CHECK);
		clientTestCaseCheckBoxButton.setText(Axis2CoreUIMessages.LABEL_GENERATE_TESTCASE_CAPTION);
		clientTestCaseCheckBoxButton.setSelection(ServerModel.isClientTestcase());
		clientTestCaseCheckBoxButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setClientTestcase(clientTestCaseCheckBoxButton.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		clientTestCaseCheckBoxButton.setLocation(10,300);
		clientTestCaseCheckBoxButton.setSize(300, 15);


		// generate all
		final Button clientGenerateAllCheckBoxButton = new Button(codegenGroup, SWT.CHECK);
		clientGenerateAllCheckBoxButton.setSelection(ServerModel.isClientGenerateAll());
		clientGenerateAllCheckBoxButton.setText(Axis2CoreUIMessages.LABEL_GENERATE_ALL);
		clientGenerateAllCheckBoxButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerModel.setClientGenerateAll(clientGenerateAllCheckBoxButton.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		clientGenerateAllCheckBoxButton.setLocation(10, 330);
		clientGenerateAllCheckBoxButton.setSize(400, 15);
		
		///////////////////////////////////////////////////////////////////////////////////////////

		//seperator
		Label seperatorLabel1 = new Label( codegenGroup, SWT.SEPARATOR|SWT.BORDER);
		seperatorLabel1.setLocation(10,365);
		seperatorLabel1.setSize(570,1);
		
		///AAR Options
		Text aarLabel = new Text(codegenGroup,SWT.BACKGROUND | SWT.READ_ONLY);
		aarLabel.setText( Axis2CoreUIMessages.LABEL_WEB_SERVICE_AAR);
		aarLabel.setLocation(10,380);
		aarLabel.setSize(220,20);
		
		//aar extention 
		Label aarExtentionLabel = new Label( codegenGroup, SWT.NONE );
		aarExtentionLabel.setText( Axis2CoreUIMessages.LABEL_AAR_EXTENTION);
		aarExtentionLabel.setLocation(10,420);
		aarExtentionLabel.setSize(200,20);
		
		final Text aarExtentionText = new Text( codegenGroup, SWT.BORDER);
		aarExtentionText.setText(ServerModel.getAarExtention());
		aarExtentionText.addModifyListener( new ModifyListener() {
			public void modifyText(ModifyEvent e){
				ServerModel.setAarExtention( aarExtentionText.getText() );
			}
		});
		aarExtentionText.setLocation(220,420);
		aarExtentionText.setSize(100,20);
		

		axis2PreferenceTab.setSize(640, 500);
		
	    return mainComp;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
	
	  
	/**
	 * Pops up the file browse dialog box
	 */
	private void handleBrowse(Shell parent) {
		DirectoryDialog fileDialog = new DirectoryDialog(parent);
		String fileName = fileDialog.open();
		if (fileName != null) {
			axis2Path.setText(fileName);
			ServerModel.setAxis2ServerPath( axis2Path.getText() );
			if(isWar){
				updateWarStatus(true);
			}else{
				updateWarStatus(false);
			}
		}
	}
	
	private void statusUpdate(boolean status){
		if(statusLabel != null){
			if(!axis2Path.getText().equals("")){
		if (status) {
			statusLabel.setText(Axis2CoreUIMessages.LABEL_AXIS2_RUNTIME_LOAD);
			this.setErrorMessage(null);
		} else {
			statusLabel.setText(Axis2CoreUIMessages.LABEL_AXIS2_RUNTIME_LOAD_ERROR);
					//this.setErrorMessage(Axis2CoreUIMessages.LABEL_AXIS2_RUNTIME_LOAD_ERROR);
		}
			}else{
				statusLabel.setText(Axis2CoreUIMessages.LABEL_AXIS2_RUNTIME_NOT_EXIT);
			}
		}
	}
	
	private boolean runtimeExist(String path){
		isWar=false;
		File axis2HomeDir = new File(path);
		if (axis2HomeDir.isDirectory()) {
			String axis2LibPath = Axis2CoreUtils.addAnotherNodeToPath(
													axis2HomeDir.getAbsolutePath(),
													"lib");
			String axis2WebappPath = Axis2CoreUtils.addAnotherNodeToPath(
					axis2HomeDir.getAbsolutePath(),
					"webapp");
			if (new File(axis2LibPath).isDirectory() && new File(axis2WebappPath).isDirectory()) {
				statusUpdate(true);
				return true;
			} else {
				String axis2WarPath = Axis2CoreUtils.addAnotherNodeToPath(
						path,
						"axis2.war");
				if (new File(axis2WarPath).isFile()) {
					isWar = true;
					statusUpdate(true);
					return true;
				} else {
					statusUpdate(false);
					return false;
				}
			}
		}else{
			statusUpdate(false);
			return false;
		}
	}
	
	private void updateWarStatus(boolean status){
		ServerModel.setAxis2ServerPathRepresentsWar(status);
		RuntimePropertyUtils.writeWarStausToPropertiesFile(status);
	}
	
}
