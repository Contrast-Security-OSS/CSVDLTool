/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
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
 * 
 */

package com.contrastsecurity.csvdltool.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.csvdltool.Main;

public class ConnectionPreferencePage extends PreferencePage {

    private Button validFlg;
    private Text hostTxt;
    private Text portTxt;
    private Button authNone;
    private Button authInput;
    private Button authSave;
    private Text userTxt;
    private Text passTxt;
    private Button ignoreSSLCertCheckFlg;
    private Text connectionTimeoutTxt;
    private Text socketTimeoutTxt;

    public ConnectionPreferencePage() {
        super("????????????");
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore ps = getPreferenceStore();
        ps.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(PreferenceConstants.PROXY_AUTH)) {
                    if (event.getNewValue().equals("none")) {
                        if (!authNone.isDisposed()) {
                            authNone.setSelection(true);
                            authInput.setSelection(false);
                            authSave.setSelection(false);
                            userTxt.setText("");
                            userTxt.setEnabled(false);
                            passTxt.setText("");
                            passTxt.setEnabled(false);
                        }
                    }
                }
            }
        });

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Group proxyGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(1, false);
        proxyGrpLt.marginHeight = 10;
        proxyGrpLt.marginWidth = 10;
        proxyGrpLt.horizontalSpacing = 10;
        proxyGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        proxyGrp.setLayoutData(proxyGrpGrDt);
        proxyGrp.setText("??????????????????");

        validFlg = new Button(proxyGrp, SWT.CHECK);
        // GridData validFlgGrDt = new GridData();
        // validFlgGrDt.horizontalSpan = 4;
        // validFlg.setLayoutData(validFlgGrDt);
        validFlg.setText("??????????????????");
        if (ps.getBoolean(PreferenceConstants.PROXY_YUKO)) {
            validFlg.setSelection(true);
        }

        Group proxyHostGrp = new Group(proxyGrp, SWT.NONE);
        GridLayout proxyHostGrppLt = new GridLayout(4, false);
        proxyHostGrppLt.marginWidth = 10;
        proxyHostGrppLt.horizontalSpacing = 10;
        proxyHostGrp.setLayout(proxyHostGrppLt);
        GridData proxyHostGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        proxyHostGrp.setLayoutData(proxyHostGrpGrDt);

        // ========== ????????? ========== //
        new Label(proxyHostGrp, SWT.LEFT).setText("????????????");
        hostTxt = new Text(proxyHostGrp, SWT.BORDER);
        hostTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hostTxt.setText(ps.getString(PreferenceConstants.PROXY_HOST));
        hostTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                hostTxt.selectAll();
            }
        });

        // ========== ????????? ========== //
        new Label(proxyHostGrp, SWT.LEFT).setText("????????????");
        portTxt = new Text(proxyHostGrp, SWT.BORDER);
        GridData portTxtGrDt = new GridData();
        portTxtGrDt.widthHint = 100;
        portTxt.setLayoutData(portTxtGrDt);
        portTxt.setText(ps.getString(PreferenceConstants.PROXY_PORT));
        portTxt.setTextLimit(5);
        portTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                portTxt.selectAll();
            }
        });

        Group proxyAuthGrp = new Group(proxyGrp, SWT.NONE);
        GridLayout proxyAuthGrpLt = new GridLayout(2, false);
        proxyAuthGrpLt.marginWidth = 15;
        proxyAuthGrpLt.horizontalSpacing = 10;
        proxyAuthGrp.setLayout(proxyAuthGrpLt);
        GridData proxyAuthGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // dirGrpGrDt.horizontalSpan = 4;
        proxyAuthGrp.setLayoutData(proxyAuthGrpGrDt);
        proxyAuthGrp.setText("??????");

        // ========== Save or Input ========== //
        Composite authInputTypeGrp = new Composite(proxyAuthGrp, SWT.NONE);
        GridLayout authInputTypeGrpLt = new GridLayout(1, false);
        authInputTypeGrpLt.marginWidth = 0;
        authInputTypeGrpLt.marginBottom = -5;
        authInputTypeGrpLt.verticalSpacing = 10;
        authInputTypeGrp.setLayout(authInputTypeGrpLt);
        GridData authInputTypeGrpGrDt = new GridData();
        authInputTypeGrpGrDt.horizontalSpan = 2;
        authInputTypeGrp.setLayoutData(authInputTypeGrpGrDt);

        authNone = new Button(authInputTypeGrp, SWT.RADIO);
        authNone.setText("????????????");
        authNone.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    userTxt.setText("");
                    userTxt.setEnabled(false);
                    passTxt.setText("");
                    passTxt.setEnabled(false);
                }
            }

        });

        authInput = new Button(authInputTypeGrp, SWT.RADIO);
        authInput.setText("?????????????????????????????????????????????????????????????????????????????????");
        authInput.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    userTxt.setText("");
                    userTxt.setEnabled(false);
                    passTxt.setText("");
                    passTxt.setEnabled(false);
                }
            }

        });

        authSave = new Button(authInputTypeGrp, SWT.RADIO);
        authSave.setText("????????????????????????????????????????????????????????????????????????");
        authSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    userTxt.setEnabled(true);
                    passTxt.setEnabled(true);
                }
            }

        });

        Composite authGrp = new Composite(proxyAuthGrp, SWT.NONE);
        GridLayout authGrpLt = new GridLayout(2, false);
        authGrpLt.marginTop = -5;
        authGrpLt.marginLeft = 12;
        authGrp.setLayout(authGrpLt);
        GridData authGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        authGrp.setLayoutData(authGrpGrDt);
        // ========== ???????????? ========== //
        new Label(authGrp, SWT.LEFT).setText("???????????????");
        userTxt = new Text(authGrp, SWT.BORDER);
        userTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userTxt.setText(ps.getString(PreferenceConstants.PROXY_USER));
        userTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userTxt.selectAll();
            }
        });

        // ========== ??????????????? ========== //
        new Label(authGrp, SWT.LEFT).setText("??????????????????");
        passTxt = new Text(authGrp, SWT.BORDER);
        passTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        passTxt.setEchoChar('*');
        passTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                passTxt.selectAll();
            }
        });

        if (ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
            authInput.setSelection(true);
            userTxt.setText("");
            userTxt.setEnabled(false);
            passTxt.setText("");
            passTxt.setEnabled(false);
        } else if (ps.getString(PreferenceConstants.PROXY_AUTH).equals("save")) {
            authSave.setSelection(true);
            userTxt.setEnabled(true);
            passTxt.setEnabled(true);
            BasicTextEncryptor encryptor = new BasicTextEncryptor();
            encryptor.setPassword(Main.MASTER_PASSWORD);
            try {
                passTxt.setText(encryptor.decrypt(ps.getString(PreferenceConstants.PROXY_PASS)));
            } catch (Exception e) {
                MessageDialog.openError(getShell(), "????????????", "???????????????????????????????????????????????????????????????\r\n?????????????????????????????????????????????????????????");
                passTxt.setText("");
            }
        } else {
            authNone.setSelection(true);
            userTxt.setText("");
            userTxt.setEnabled(false);
            passTxt.setText("");
            passTxt.setEnabled(false);
        }

        Group sslCertGrp = new Group(composite, SWT.NONE);
        GridLayout sslCertGrpLt = new GridLayout(1, false);
        sslCertGrpLt.marginWidth = 15;
        sslCertGrpLt.horizontalSpacing = 10;
        sslCertGrp.setLayout(sslCertGrpLt);
        GridData sslCertGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        sslCertGrp.setLayoutData(sslCertGrpGrDt);
        sslCertGrp.setText("SSL???????????????");

        // ========== ????????????????????? ========== //
        ignoreSSLCertCheckFlg = new Button(sslCertGrp, SWT.CHECK);
        // GridData ignoreSSLCertCheckFlgGrDt = new GridData();
        // ignoreSSLCertCheckFlgGrDt.horizontalSpan = 4;
        // ignoreSSLCertCheckFlg.setLayoutData(ignoreSSLCertCheckFlgGrDt);
        ignoreSSLCertCheckFlg.setText("????????????????????????");
        if (ps.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
            ignoreSSLCertCheckFlg.setSelection(true);
        }

        Group timeoutGrp = new Group(composite, SWT.NONE);
        GridLayout timeoutGrpLt = new GridLayout(2, false);
        timeoutGrpLt.marginWidth = 15;
        timeoutGrpLt.horizontalSpacing = 10;
        timeoutGrp.setLayout(timeoutGrpLt);
        GridData timeoutGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        timeoutGrp.setLayoutData(timeoutGrpGrDt);
        timeoutGrp.setText("?????????????????????????????????");

        // ========== ConnetionTimeout ========== //
        new Label(timeoutGrp, SWT.LEFT).setText("ConnetionTimeout???");
        connectionTimeoutTxt = new Text(timeoutGrp, SWT.BORDER);
        connectionTimeoutTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        connectionTimeoutTxt.setText(ps.getString(PreferenceConstants.CONNECTION_TIMEOUT));
        connectionTimeoutTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                connectionTimeoutTxt.selectAll();
            }
        });
        // ========== SocketTimeout ========== //
        new Label(timeoutGrp, SWT.LEFT).setText("SocketTimeout???");
        socketTimeoutTxt = new Text(timeoutGrp, SWT.BORDER);
        socketTimeoutTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        socketTimeoutTxt.setText(ps.getString(PreferenceConstants.SOCKET_TIMEOUT));
        socketTimeoutTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                socketTimeoutTxt.selectAll();
            }
        });

        Composite buttonGrp = new Composite(parent, SWT.NONE);
        GridLayout buttonGrpLt = new GridLayout(2, false);
        buttonGrpLt.marginHeight = 15;
        buttonGrpLt.marginWidth = 5;
        buttonGrpLt.horizontalSpacing = 7;
        buttonGrpLt.verticalSpacing = 20;
        buttonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        buttonGrpGrDt.horizontalAlignment = SWT.END;
        buttonGrp.setLayoutData(buttonGrpGrDt);

        Button defaultBtn = new Button(buttonGrp, SWT.NULL);
        GridData defaultBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        defaultBtnGrDt.widthHint = 90;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText("????????????????????????");
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                connectionTimeoutTxt.setText(ps.getDefaultString(PreferenceConstants.CONNECTION_TIMEOUT));
                socketTimeoutTxt.setText(ps.getDefaultString(PreferenceConstants.SOCKET_TIMEOUT));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("??????");
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
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
        List<String> errors = new ArrayList<String>();
        ps.setValue(PreferenceConstants.PROXY_YUKO, this.validFlg.getSelection());
        if (this.validFlg.getSelection()) {
            if (this.hostTxt.getText().isEmpty()) {
                errors.add("??????????????????????????????????????????");
            }
        }
        ps.setValue(PreferenceConstants.PROXY_HOST, this.hostTxt.getText());
        if (this.validFlg.getSelection()) {
            if (this.portTxt.getText().isEmpty()) {
                errors.add("????????????????????????????????????????????????");
            } else {
                if (!StringUtils.isNumeric(this.portTxt.getText())) {
                    errors.add("?????????????????????????????????????????????????????????");
                }
            }
        }
        ps.setValue(PreferenceConstants.PROXY_PORT, this.portTxt.getText());
        if (authInput.getSelection()) {
            ps.setValue(PreferenceConstants.PROXY_AUTH, "input");
            ps.setValue(PreferenceConstants.PROXY_USER, "");
            ps.setValue(PreferenceConstants.PROXY_PASS, "");
        } else if (authSave.getSelection()) {
            ps.setValue(PreferenceConstants.PROXY_AUTH, "save");
            if (this.userTxt.getText().isEmpty() || this.passTxt.getText().isEmpty()) {
                errors.add("????????????????????????????????????????????????????????????????????????????????????");
            } else {
                ps.setValue(PreferenceConstants.PROXY_USER, this.userTxt.getText());
                BasicTextEncryptor encryptor = new BasicTextEncryptor();
                encryptor.setPassword(Main.MASTER_PASSWORD);
                ps.setValue(PreferenceConstants.PROXY_PASS, encryptor.encrypt(this.passTxt.getText()));
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, "");
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, "");
            }
        } else {
            ps.setValue(PreferenceConstants.PROXY_AUTH, "none");
            ps.setValue(PreferenceConstants.PROXY_USER, "");
            ps.setValue(PreferenceConstants.PROXY_PASS, "");
            ps.setValue(PreferenceConstants.PROXY_TMP_USER, "");
            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, "");
        }
        ps.setValue(PreferenceConstants.IGNORE_SSLCERT_CHECK, this.ignoreSSLCertCheckFlg.getSelection());

        if (this.connectionTimeoutTxt.getText().isEmpty()) {
            errors.add("???ConnetionTimeout??????????????????????????????");
        } else {
            if (!StringUtils.isNumeric(this.connectionTimeoutTxt.getText())) {
                errors.add("???ConnetionTimeout???????????????????????????????????????");
            } else {
                ps.setValue(PreferenceConstants.CONNECTION_TIMEOUT, this.connectionTimeoutTxt.getText());
            }
        }

        if (this.socketTimeoutTxt.getText().isEmpty()) {
            errors.add("???SocketTimeout??????????????????????????????");
        } else {
            if (!StringUtils.isNumeric(this.socketTimeoutTxt.getText())) {
                errors.add("???SocketTimeout???????????????????????????????????????");
            } else {
                ps.setValue(PreferenceConstants.SOCKET_TIMEOUT, this.socketTimeoutTxt.getText());
            }
        }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "????????????", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
