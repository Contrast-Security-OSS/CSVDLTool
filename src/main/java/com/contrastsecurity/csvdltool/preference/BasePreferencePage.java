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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.ApiKeyApi;
import com.contrastsecurity.csvdltool.api.OrganizationApi;
import com.contrastsecurity.csvdltool.api.OrganizationsApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
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
        IPreferenceStore preferenceStore = getPreferenceStore();

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Composite baseGrp = new Composite(composite, SWT.NONE);
        GridLayout baseGrpLt = new GridLayout(3, false);
        baseGrpLt.marginWidth = 15;
        baseGrpLt.horizontalSpacing = 10;
        baseGrp.setLayout(baseGrpLt);
        GridData baseGrpLtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        baseGrp.setLayoutData(baseGrpLtGrDt);

        new Label(baseGrp, SWT.LEFT).setText("Contrast URL：");
        new Label(baseGrp, SWT.LEFT).setText("");
        contrastUrlTxt = new Text(baseGrp, SWT.BORDER);
        contrastUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contrastUrlTxt.setText(preferenceStore.getString(PreferenceConstants.CONTRAST_URL));
        contrastUrlTxt.setMessage("4つの項目をすべて埋めててください。");
        contrastUrlTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                contrastUrlTxt.selectAll();
            }
        });

        new Label(baseGrp, SWT.LEFT).setText("API Key：");
        new Label(baseGrp, SWT.LEFT).setText("");
        apiKeyTxt = new Text(baseGrp, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.setText(preferenceStore.getString(PreferenceConstants.API_KEY));
        apiKeyTxt.setMessage("これらの情報はすべてTeamServerの");
        apiKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                apiKeyTxt.selectAll();
            }
        });

        new Label(baseGrp, SWT.LEFT).setText("Service Key：");
        new Label(baseGrp, SWT.LEFT).setText("");
        serviceKeyTxt = new Text(baseGrp, SWT.BORDER);
        serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceKeyTxt.setText(preferenceStore.getString(PreferenceConstants.SERVICE_KEY));
        serviceKeyTxt.setMessage("「あなたのアカウント」で確認できます。");
        serviceKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                serviceKeyTxt.selectAll();
            }
        });

        new Label(baseGrp, SWT.LEFT).setText("Username：");
        Label icon = new Label(baseGrp, SWT.NONE);
        Image iconImg = new Image(parent.getDisplay(), Main.class.getClassLoader().getResourceAsStream("help.png"));
        icon.setImage(iconImg);
        icon.setToolTipText("設定するユーザーの権限について\r\n・組織ロールはAdmin権限が必要です。\r\n・アプリケーションアクセスグループはView権限以上が必要です。");
        userNameTxt = new Text(baseGrp, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(preferenceStore.getString(PreferenceConstants.USERNAME));
        userNameTxt.setMessage("すべて埋めたら、下のボタンで組織情報を取得します。");
        userNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userNameTxt.selectAll();
            }
        });

        Button getOrgBtn = new Button(baseGrp, SWT.NULL);
        GridData getOrgBtnGrDt = new GridData();
        getOrgBtnGrDt.horizontalSpan = 3;
        getOrgBtnGrDt.heightHint = 30;
        getOrgBtnGrDt.widthHint = 150;
        getOrgBtnGrDt.horizontalAlignment = SWT.RIGHT;
        getOrgBtn.setLayoutData(getOrgBtnGrDt);
        getOrgBtn.setText("組織情報を取得");
        getOrgBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            }

            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent event) {
                String url = contrastUrlTxt.getText();
                String usr = userNameTxt.getText();
                String svc = serviceKeyTxt.getText();
                String api = apiKeyTxt.getText();
                if (url.isEmpty() || usr.isEmpty() || svc.isEmpty() || api.isEmpty()) {
                    MessageDialog.openInformation(composite.getShell(), "組織情報の取得", "4つの項目を埋めてください。");
                    return;
                }
                Api orgApi = new OrganizationApi(preferenceStore, url, usr, svc, api);
                Api orgsApi = new OrganizationsApi(preferenceStore, url, usr, svc, api);
                try {
                    Organization organization = (Organization) orgApi.get();
                    List<Organization> organizations = (List<Organization>) orgsApi.get();
                    for (Organization org : organizations) {
                        System.out.println(org.getName());
                        Api apiKeyApi = new ApiKeyApi(preferenceStore, org.getOrganization_uuid());
                        String apiKey = (String) apiKeyApi.get();
                        System.out.println(apiKey);
                    }
                    orgNameTxt.setText(organization.getName());
                    orgIdTxt.setText(organization.getOrganization_uuid());
                    MessageDialog.openInformation(composite.getShell(), "組織情報の取得", "組織情報を取得しました。");
                } catch (ApiException e) {
                    MessageDialog.openWarning(composite.getShell(), "組織情報の取得", String.format("TeamServerからエラーが返されました。\r\n%s", e.getMessage()));
                } catch (NonApiException e) {
                    MessageDialog.openError(composite.getShell(), "組織情報の取得", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", e.getMessage()));
                } catch (Exception e) {
                    MessageDialog.openError(composite.getShell(), "組織情報の取得", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", e.getMessage()));
                }
            }
        });

        Composite orgGrp = new Composite(composite, SWT.NONE);
        GridLayout orgGrpLt = new GridLayout(2, false);
        orgGrpLt.marginWidth = 15;
        orgGrpLt.horizontalSpacing = 10;
        orgGrp.setLayout(orgGrpLt);
        GridData orgGrpLtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        orgGrp.setLayoutData(orgGrpLtGrDt);

        new Label(orgGrp, SWT.LEFT).setText("組織名：");
        orgNameTxt = new Text(orgGrp, SWT.BORDER);
        orgNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgNameTxt.setText(preferenceStore.getString(PreferenceConstants.ORG_NAME));
        orgNameTxt.setEditable(false);

        new Label(orgGrp, SWT.LEFT).setText("組織ID：");
        orgIdTxt = new Text(orgGrp, SWT.BORDER);
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
        String url = contrastUrlTxt.getText();
        String usr = userNameTxt.getText();
        String svc = serviceKeyTxt.getText();
        String api = apiKeyTxt.getText();
        if (url.isEmpty() || usr.isEmpty() || svc.isEmpty() || api.isEmpty()) {
            MessageDialog.openError(getShell(), "基本設定", "4つの項目を埋めて、組織情報を取得してください。");
            return false;
        }
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
