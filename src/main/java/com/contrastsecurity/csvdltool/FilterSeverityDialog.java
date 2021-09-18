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

package com.contrastsecurity.csvdltool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.model.Filter;

public class FilterSeverityDialog extends Dialog {

    private Set<Filter> filters;
    private List<Button> buttons;
    private List<String> labels;

    public FilterSeverityDialog(Shell parentShell, Set<Filter> filters) {
        super(parentShell);
        this.filters = filters;
        this.buttons = new ArrayList<Button>();
        this.labels = new ArrayList<String>();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginWidth = 25;
        compositeLt.marginHeight = 5;
        compositeLt.horizontalSpacing = 5;
        composite.setLayout(compositeLt);
        GridData compositeGrDt = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(compositeGrDt);
        boolean checkFlg = false;
        for (Filter filter : this.filters) {
            Button button = new Button(composite, SWT.CHECK);
            button.setText(filter.getLabel());
            if (filter.isValid()) {
                button.setSelection(true);
                checkFlg |= true;
            } else {
                button.setSelection(false);
            }
            this.buttons.add(button);
        }
        if (!checkFlg) {
            for (Button button : buttons) {
                button.setSelection(true);
            }
        }
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        // okButton.setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        boolean nonCheck = false;
        for (Button button : buttons) {
            if (button.getSelection()) {
                labels.add(button.getText());
            } else {
                nonCheck |= true;
            }
        }
        if (!nonCheck) {
            labels.clear();
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(275, 200);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("重大度の選択");
    }

    public List<String> getLabels() {
        return labels;
    }

}
