/*
 * MIT License
 * Copyright (c) 2015-2019 Tabocom
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.contrastsecurity.csvdltool.preference;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.OrganizationApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.model.Organization;

public class BasePreferencePage extends PreferencePage {

    private Text contrastUrlTxt;
    private Text apiKeyTxt;
    private Text serviceKeyTxt;
    private Text userNameTxt;
    private Text orgNameTxt;
    private Text orgIdTxt;

    Logger logger = Logger.getLogger("csvdltool");

    public BasePreferencePage() {
        super("基本設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        IPreferenceStore preferenceStore = getPreferenceStore();

        new Label(composite, SWT.LEFT).setText("Contrast URL：");
        contrastUrlTxt = new Text(composite, SWT.BORDER);
        contrastUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contrastUrlTxt.setText(preferenceStore.getString(PreferenceConstants.CONTRAST_URL));
        contrastUrlTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                contrastUrlTxt.selectAll();
            }
        });

        new Label(composite, SWT.LEFT).setText("API Key：");
        apiKeyTxt = new Text(composite, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.setText(preferenceStore.getString(PreferenceConstants.API_KEY));
        apiKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                apiKeyTxt.selectAll();
            }
        });
        new Label(composite, SWT.LEFT).setText("Service Key：");
        serviceKeyTxt = new Text(composite, SWT.BORDER);
        serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceKeyTxt.setText(preferenceStore.getString(PreferenceConstants.SERVICE_KEY));
        serviceKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                serviceKeyTxt.selectAll();
            }
        });

        new Label(composite, SWT.LEFT).setText("Username：");
        userNameTxt = new Text(composite, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(preferenceStore.getString(PreferenceConstants.USERNAME));
        userNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userNameTxt.selectAll();
            }
        });

        Button getOrgBtn = new Button(composite, SWT.NULL);
        GridData getOrgBtnGrDt = new GridData();
        getOrgBtnGrDt.horizontalSpan = 2;
        getOrgBtnGrDt.horizontalAlignment = SWT.RIGHT;
        getOrgBtn.setLayoutData(getOrgBtnGrDt);
        getOrgBtn.setText("組織情報を取得");
        getOrgBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            }

            public void widgetSelected(SelectionEvent event) {
                Api api = new OrganizationApi(preferenceStore, contrastUrlTxt.getText(), userNameTxt.getText(), serviceKeyTxt.getText(), apiKeyTxt.getText());
                try {
                    Organization organization = (Organization) api.get();
                    orgNameTxt.setText(organization.getName());
                    orgIdTxt.setText(organization.getOrganization_uuid());
                } catch (ApiException re) {
                    MessageDialog.openError(composite.getShell(), "組織情報の取得", re.getMessage());
                } catch (Exception e) {
                    MessageDialog.openError(composite.getShell(), "組織情報の取得", e.getMessage());
                }
            }
        });

        new Label(composite, SWT.LEFT).setText("組織名：");
        orgNameTxt = new Text(composite, SWT.BORDER);
        orgNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgNameTxt.setText(preferenceStore.getString(PreferenceConstants.ORG_NAME));
        orgNameTxt.setEditable(false);

        new Label(composite, SWT.LEFT).setText("組織ID：");
        orgIdTxt = new Text(composite, SWT.BORDER);
        orgIdTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgIdTxt.setText(preferenceStore.getString(PreferenceConstants.ORG_ID));
        orgIdTxt.setEditable(false);

        Button applyBtn = new Button(composite, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtnGrDt.horizontalSpan = 2;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("適用");
        applyBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });
        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        if (this.contrastUrlTxt != null) {
            ps.setValue(PreferenceConstants.CONTRAST_URL, this.contrastUrlTxt.getText());
        }
        if (this.apiKeyTxt != null) {
            ps.setValue(PreferenceConstants.API_KEY, this.apiKeyTxt.getText());
        }
        if (this.serviceKeyTxt != null) {
            ps.setValue(PreferenceConstants.SERVICE_KEY, this.serviceKeyTxt.getText());
        }
        if (this.userNameTxt != null) {
            ps.setValue(PreferenceConstants.USERNAME, this.userNameTxt.getText());
        }
        if (this.orgNameTxt != null) {
            ps.setValue(PreferenceConstants.ORG_NAME, this.orgNameTxt.getText());
        }
        if (this.orgIdTxt != null) {
            ps.setValue(PreferenceConstants.ORG_ID, this.orgIdTxt.getText());
        }
        return true;
    }
}
