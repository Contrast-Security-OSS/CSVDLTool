package com.contrastsecurity.csvdltool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class VulGetProgressMonitorDialog extends ProgressMonitorDialog {

    public VulGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("vulgetprogressmonitordialog.title")); //$NON-NLS-1$
    }

}
