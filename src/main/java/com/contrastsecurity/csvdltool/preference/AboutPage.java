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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class AboutPage extends PreferencePage {

    public AboutPage() {
        super("CSVDLToolについて");
    }

    @Override
    protected Control createContents(Composite parent) {

        GridLayout parentGrLt = new GridLayout(1, false);
        parentGrLt.verticalSpacing = 20;
        parent.setLayout(parentGrLt);

        Composite appGrp = new Composite(parent, SWT.NONE);
        appGrp.setLayout(new GridLayout(3, false));
        GridData appGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        appGrp.setLayoutData(appGrpGrDt);

        Label versionTitleLbl = new Label(appGrp, SWT.NONE);
        GridData versionTitleLblGrDt = new GridData();
        versionTitleLblGrDt.widthHint = 100;
        versionTitleLbl.setLayoutData(versionTitleLblGrDt);
        versionTitleLbl.setText("Version:");
        Label versionValueLbl = new Label(appGrp, SWT.NONE);
        GridData versionValueLblGrDt = new GridData();
        versionValueLbl.setLayoutData(versionValueLblGrDt);
        versionValueLbl.setText("1.0.0");

        Label copyrightLbl = new Label(appGrp, SWT.NONE);
        GridData copyrightLblGrDt = new GridData();
        copyrightLblGrDt.horizontalSpan = 2;
        copyrightLbl.setLayoutData(copyrightLblGrDt);
        copyrightLbl.setText("Copyright (c) 2020 Contrast Security Japan G.K.");

        Composite licenseGrp = new Composite(parent, SWT.NONE);
        GridLayout licenseGrpGrLt = new GridLayout(1, false);
        licenseGrp.setLayout(licenseGrpGrLt);
        GridData licenseGroupGrDt = new GridData(GridData.FILL_BOTH);
        licenseGrp.setLayoutData(licenseGroupGrDt);

        Label licenseLbl = new Label(licenseGrp, SWT.NONE);
        licenseLbl.setText("This software includes the work that is distributed in the Apache License 2.0");

        Label snakeYamlLbl = new Label(licenseGrp, SWT.NONE);
        snakeYamlLbl.setText("- SnakeYAML");

        noDefaultAndApplyButton();
        return parent;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        return true;
    }
}
