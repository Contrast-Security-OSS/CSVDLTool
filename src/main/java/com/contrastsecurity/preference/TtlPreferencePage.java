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

package com.contrastsecurity.preference;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TtlPreferencePage extends PreferencePage {

    private Text ttlCharCodeTxt;
    private Text ttlAuthPwdHideTxt;

    public TtlPreferencePage() {
        super("TTL出力設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        IPreferenceStore preferenceStore = getPreferenceStore();

        // ========== TTLファイルの文字コード ========== //
        new Label(composite, SWT.LEFT).setText("出力文字コード：");
        ttlCharCodeTxt = new Text(composite, SWT.BORDER);
        GridData ttlCharCodeTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttlCharCodeTxtGrDt.horizontalSpan = 2;
        ttlCharCodeTxt.setLayoutData(ttlCharCodeTxtGrDt);
        ttlCharCodeTxt.setText(preferenceStore.getString(PreferenceConstants.TTL_CHARCODE));
        ttlCharCodeTxt.setMessage("省略時は Shift-JIS となります。");
        new Label(composite, SWT.LEFT).setText("");
        Label ttlCharCodeDescLbl = new Label(composite, SWT.LEFT);
        GridData ttlCharCodeDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttlCharCodeDescLblGrDt.horizontalSpan = 2;
        ttlCharCodeDescLbl.setLayoutData(ttlCharCodeDescLblGrDt);
        ttlCharCodeDescLbl.setText("例) Shift-JIS, UTF-8など。省略や不正な場合はShift-JISになります。");

        // ========== 認証パスワードの伏字 ========== //
        new Label(composite, SWT.LEFT).setText("認証パスワードの伏字文字列：");
        ttlAuthPwdHideTxt = new Text(composite, SWT.BORDER);
        GridData ttlAuthPwdHideTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttlAuthPwdHideTxtGrDt.horizontalSpan = 2;
        ttlAuthPwdHideTxt.setLayoutData(ttlAuthPwdHideTxtGrDt);
        ttlAuthPwdHideTxt.setText(preferenceStore.getString(PreferenceConstants.TTL_AUTH_PWD_HIDE));
        ttlAuthPwdHideTxt.setMessage("省略時は PASSWORD となります。");
        new Label(composite, SWT.LEFT).setText("");
        Label ttlAuthPwdHideTxtDescLbl = new Label(composite, SWT.LEFT);
        GridData ttlAuthPwdHideTxtDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttlAuthPwdHideTxtDescLblGrDt.horizontalSpan = 2;
        ttlAuthPwdHideTxtDescLbl.setLayoutData(ttlAuthPwdHideTxtDescLblGrDt);
        StringBuilder builder = new StringBuilder();
        builder.append("生成されるTTLファイルに認証パスワードをべた書きしたくない場合に\r\n");
        builder.append("利用するTTL内の変数名です。デフォルトではPASSWORDです。\r\n");
        builder.append("通常は'${authpassword}'とnegotiation内に書くことで実行時に\r\n");
        builder.append("認証パスワードに置換されますが、ttlを開くと認証パスワードが丸見えです。\r\n");
        builder.append("そこで、sendln '${authpassword}'と書くところをsendln PASSWORDと\r\n");
        builder.append("と書いておけば変数化されるので安心です。このパスワード変数にはttl実行時に\r\n");
        builder.append("引数としてパスワードが渡される仕組みになっています。");
        ttlAuthPwdHideTxtDescLbl.setText(builder.toString());

        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        if (this.ttlCharCodeTxt != null) {
            ps.setValue(PreferenceConstants.TTL_CHARCODE, this.ttlCharCodeTxt.getText());
        }
        if (this.ttlAuthPwdHideTxt != null) {
            ps.setValue(PreferenceConstants.TTL_AUTH_PWD_HIDE, this.ttlAuthPwdHideTxt.getText());
        }
        return true;
    }
}
