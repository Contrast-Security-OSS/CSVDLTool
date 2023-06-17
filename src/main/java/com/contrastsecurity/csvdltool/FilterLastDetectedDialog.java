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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FilterLastDetectedDialog extends Dialog {

    private DateTime frCalendar;
    private DateTime toCalendar;
    private Text frDateText;
    private Text toDateText;
    private Date frDate;
    private Date toDate;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E)"); //$NON-NLS-1$
    private Calendar cal = Calendar.getInstance();

    public FilterLastDetectedDialog(Shell parentShell, Date frDate, Date toDate) {
        super(parentShell);
        this.frDate = frDate;
        this.toDate = toDate;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLt = new GridLayout(5, false);
        compositeLt.marginWidth = 25;
        compositeLt.marginHeight = 10;
        compositeLt.horizontalSpacing = 15;
        composite.setLayout(compositeLt);
        GridData compositeGrDt = new GridData(GridData.FILL_HORIZONTAL);
        compositeGrDt.horizontalAlignment = SWT.CENTER;
        composite.setLayoutData(compositeGrDt);

        frCalendar = new DateTime(composite, SWT.CALENDAR);
        GridData frCalendarGrDt = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1);
        frCalendar.setLayoutData(frCalendarGrDt);
        frCalendar.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                cal.set(frCalendar.getYear(), frCalendar.getMonth(), frCalendar.getDay(), 0, 0, 0);
                frDateText.setText(sdf.format(cal.getTime()));
                cal.set(frCalendar.getYear(), frCalendar.getMonth(), frCalendar.getDay(), 0, 0, 0);
                frDate = cal.getTime();
            }
        });

        new Label(composite, SWT.NULL).setText("～"); //$NON-NLS-1$

        toCalendar = new DateTime(composite, SWT.CALENDAR);
        GridData toCalendarGrDt = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        toCalendar.setLayoutData(toCalendarGrDt);
        toCalendar.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
                cal.set(toCalendar.getYear(), toCalendar.getMonth(), toCalendar.getDay(), 0, 0, 0);
                toDateText.setText(sdf.format(cal.getTime()));
                cal.set(toCalendar.getYear(), toCalendar.getMonth(), toCalendar.getDay(), 23, 59, 59);
                toDate = cal.getTime();
            }
        });

        Button frBtn = new Button(composite, SWT.NULL);
        GridData frBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        frBtn.setLayoutData(frBtnGrDt);
        frBtn.setText(Messages.getString("filterLastdetecteddialog.clear.date.filter.from")); //$NON-NLS-1$
        frBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                frDateText.setText(""); //$NON-NLS-1$
                frDate = null;
            }
        });
        frDateText = new Text(composite, SWT.BORDER);
        GridData frDateTextGrDt = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        frDateTextGrDt.minimumWidth = 100;
        frDateText.setLayoutData(frDateTextGrDt);
        frDateText.setEditable(false);
        if (frDate != null) {
            cal.setTime(frDate);
            frCalendar.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
            frDateText.setText(sdf.format(frDate));
        } else {
            cal.set(frCalendar.getYear(), frCalendar.getMonth(), frCalendar.getDay(), 0, 0, 0);
            // frDateText.setText(sdf.format(cal.getTime()));
        }
        new Label(composite, SWT.NULL).setText("～"); //$NON-NLS-1$
        toDateText = new Text(composite, SWT.BORDER);
        GridData toDateTextGrDt = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        toDateTextGrDt.minimumWidth = 100;
        toDateText.setLayoutData(toDateTextGrDt);
        toDateText.setEditable(false);
        if (toDate != null) {
            cal.setTime(toDate);
            toCalendar.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
            toDateText.setText(sdf.format(toDate));
        } else {
            cal.set(toCalendar.getYear(), toCalendar.getMonth(), toCalendar.getDay(), 0, 0, 0);
            // toDateText.setText(sdf.format(cal.getTime()));
        }
        Button toBtn = new Button(composite, SWT.NULL);
        GridData toBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        toBtn.setLayoutData(toBtnGrDt);
        toBtn.setText(Messages.getString("filterLastdetecteddialog.clear.date.filter.to")); //$NON-NLS-1$
        toBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                toDateText.setText(""); //$NON-NLS-1$
                toDate = null;
            }
        });
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        Button deSelectButton = createButton(parent, IDialogConstants.DESELECT_ALL_ID, Messages.getString("filterLastdetecteddialog.clear.date.filter.both"), false); //$NON-NLS-1$
        okButton.setEnabled(true);
        deSelectButton.setEnabled(true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.DESELECT_ALL_ID) {
            this.frDate = null;
            this.toDate = null;
            super.okPressed();
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 300);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("filterLastdetecteddialog.dialog.title")); //$NON-NLS-1$
    }

    public Date getFrDate() {
        return this.frDate;
    }

    public Date getToDate() {
        return this.toDate;
    }

}
