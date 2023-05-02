package com.contrastsecurity.csvdltool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class LibGetProgressMonitorDialog extends ProgressMonitorDialog {

    public LibGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("libgetprogressmonitordialog.title")); //$NON-NLS-1$
    }

}
