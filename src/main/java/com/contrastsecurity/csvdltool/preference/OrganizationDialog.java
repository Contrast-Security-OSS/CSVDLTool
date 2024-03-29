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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.OrganizationApi;
import com.contrastsecurity.csvdltool.api.OrganizationForBasicApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.model.Organization;

public class OrganizationDialog extends Dialog {

    private IPreferenceStore ps;
    private String url;
    private String usr;
    private String svc;
    private Text orgIdTxt;
    private Text apiKeyTxt;
    private CSVDLToolShell shell;

    private Organization org;

    public OrganizationDialog(CSVDLToolShell parentShell, IPreferenceStore ps, String url, String usr, String svc) {
        super(parentShell);
        this.shell = parentShell;
        this.ps = ps;
        this.url = url;
        this.usr = usr;
        this.svc = svc;
    }

    public OrganizationDialog(CSVDLToolShell parentShell, IPreferenceStore ps, String url, String usr) {
        super(parentShell);
        this.shell = parentShell;
        this.ps = ps;
        this.url = url;
        this.usr = usr;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        new Label(composite, SWT.LEFT).setText(Messages.getString("organizationdialog.org.id.label")); //$NON-NLS-1$
        orgIdTxt = new Text(composite, SWT.BORDER);
        GridData orgIdTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        orgIdTxtGrDt.minimumWidth = 300;
        orgIdTxt.setLayoutData(orgIdTxtGrDt);
        orgIdTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                orgIdTxt.selectAll();
            }
        });
        orgIdTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String orgStr = orgIdTxt.getText().trim();
                String apikeyStr = apiKeyTxt.getText().trim();
                if (orgStr.isEmpty() || apikeyStr.isEmpty()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
        orgIdTxt.setFocus();
        new Label(composite, SWT.LEFT).setText(Messages.getString("organizationdialog.org.apikey.label")); //$NON-NLS-1$
        apiKeyTxt = new Text(composite, SWT.BORDER);
        GridData apiKeyTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        apiKeyTxtGrDt.minimumWidth = 300;
        apiKeyTxt.setLayoutData(apiKeyTxtGrDt);
        apiKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                apiKeyTxt.selectAll();
            }
        });
        apiKeyTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String orgStr = orgIdTxt.getText().trim();
                String apikeyStr = apiKeyTxt.getText().trim();
                if (orgStr.isEmpty() || apikeyStr.isEmpty()) {
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
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        orgIdTxt.setEditable(false);
        apiKeyTxt.setEnabled(false);
        Organization org = new Organization();
        org.setApikey(apiKeyTxt.getText().trim());
        org.setOrganization_uuid(orgIdTxt.getText().trim());
        org.setValid(false);
        Api orgApi = null;
        if (svc == null) {
            orgApi = new OrganizationForBasicApi(this.shell, this.ps, org, url, usr);
        } else {
            orgApi = new OrganizationApi(this.shell, this.ps, org, url, usr, svc);
        }
        try {
            Organization rtnOrg = (Organization) orgApi.getWithoutCheckTsv();
            if (rtnOrg == null) {
                MessageDialog.openError(getShell(), Messages.getString("organizationdialog.message.dialog.title"), //$NON-NLS-1$
                        Messages.getString("organizationdialog.message.dialog.organization.notfound.error.message")); //$NON-NLS-1$
            } else {
                org.setName(rtnOrg.getName());
                this.org = org;
            }
        } catch (ApiException e) {
            MessageDialog.openWarning(getShell(), Messages.getString("organizationdialog.message.dialog.title"), //$NON-NLS-1$
                    String.format("%s\r\n%s", Messages.getString("organizationdialog.message.dialog.teamserver.return.error.message"), e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (NonApiException e) {
            MessageDialog.openError(getShell(), Messages.getString("organizationdialog.message.dialog.title"), //$NON-NLS-1$
                    String.format("%s %s\r\n%s", Messages.getString("organizationdialog.message.dialog.unexpected.status.code.error.message"), e.getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
                            Messages.getString("organizationdialog.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
        } catch (TsvException e) {
            MessageDialog.openError(getShell(), Messages.getString("organizationdialog.message.dialog.title"), e.getMessage()); //$NON-NLS-1$
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.getString("organizationdialog.message.dialog.title"), //$NON-NLS-1$
                    String.format("%s\r\n%s", Messages.getString("organizationdialog.message.dialog.unknown.error.message"), e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
        }
        super.okPressed();
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("organizationdialog.dialog.title")); //$NON-NLS-1$
    }
}
