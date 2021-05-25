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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.OrganizationApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.model.Organization;

public class OrganizationDialog extends Dialog {

    private IPreferenceStore preferenceStore;
    private String url;
    private String usr;
    private String svc;
    private Text orgIdTxt;
    private Text apiKeyTxt;

    private Organization org;

    public OrganizationDialog(Shell parentShell, IPreferenceStore preferenceStore, String url, String usr, String svc) {
        super(parentShell);
        this.preferenceStore = preferenceStore;
        this.url = url;
        this.usr = usr;
        this.svc = svc;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        new Label(composite, SWT.LEFT).setText("組織ID：");
        orgIdTxt = new Text(composite, SWT.BORDER);
        orgIdTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgIdTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String orgStr = orgIdTxt.getText();
                String apikeyStr = apiKeyTxt.getText();
                if (orgStr.isBlank() || apikeyStr.isBlank()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
        new Label(composite, SWT.LEFT).setText("API Key：");
        apiKeyTxt = new Text(composite, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String orgStr = orgIdTxt.getText();
                String apikeyStr = apiKeyTxt.getText();
                if (orgStr.isBlank() || apikeyStr.isBlank()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
        return composite;
    }

    public Organization getOrg() {
        return org;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(false);
    }

    @Override
    protected void okPressed() {
        Organization organization = new Organization();
        organization.setApikey(apiKeyTxt.getText());
        organization.setOrganization_uuid(orgIdTxt.getText());
        organization.setValid(false);
        Api orgApi = new OrganizationApi(preferenceStore, organization, url, usr, svc);
        try {
            Organization rtnOrg = (Organization) orgApi.get();
            organization.setName(rtnOrg.getName());
            this.org = organization;
        } catch (ApiException e) {
            MessageDialog.openWarning(getShell(), "組織情報の確認", String.format("TeamServerからエラーが返されました。\r\n%s", e.getMessage()));
        } catch (NonApiException e) {
            MessageDialog.openError(getShell(), "組織情報の確認", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", e.getMessage()));
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "組織情報の確認", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", e.getMessage()));
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(480, 150);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("組織の追加");
    }
}
