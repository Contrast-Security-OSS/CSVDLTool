package com.contrastsecurity.csvdltool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class AttackGetProgressMonitorDialog extends ProgressMonitorDialog {

    public AttackGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("attackgetprogressmonitordialog.title")); //$NON-NLS-1$
    }

}
