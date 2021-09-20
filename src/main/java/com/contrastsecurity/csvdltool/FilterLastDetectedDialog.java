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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class FilterLastDetectedDialog extends Dialog {

    private DateTime frCalendar;
    private DateTime toCalendar;
    private Date frDate;
    private Date toDate;

    public FilterLastDetectedDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLt = new GridLayout(3, false);
        compositeLt.marginWidth = 25;
        compositeLt.marginHeight = 10;
        compositeLt.horizontalSpacing = 10;
        composite.setLayout(compositeLt);
        GridData compositeGrDt = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(compositeGrDt);
        frCalendar = new DateTime(composite, SWT.CALENDAR);
        new Label(composite, SWT.NULL).setText("～");
        toCalendar = new DateTime(composite, SWT.CALENDAR);
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
        Calendar cal = Calendar.getInstance();
        cal.set(frCalendar.getYear(), frCalendar.getMonth(), frCalendar.getDay(), 0, 0, 0);
        this.frDate = cal.getTime();
        cal.set(toCalendar.getYear(), toCalendar.getMonth(), toCalendar.getDay(), 23, 59, 59);
        this.toDate = cal.getTime();
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(525, 250);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("最終検出日の指定");
    }

    public Date getFrDate() {
        return this.frDate;
    }

    public Date getToDate() {
        return this.toDate;
    }

}
