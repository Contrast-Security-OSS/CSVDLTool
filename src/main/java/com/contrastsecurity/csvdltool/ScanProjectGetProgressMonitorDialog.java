package com.contrastsecurity.csvdltool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class ScanProjectGetProgressMonitorDialog extends ProgressMonitorDialog {

    public ScanProjectGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("appgetprogressmonitordialog.title")); //$NON-NLS-1$
    }

}
