package com.contrastsecurity.csvdltool.ui.monitordialog.routecoverage;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.Messages;

public class RouteCoverageGetProgressMonitorDialog extends ProgressMonitorDialog {

    public RouteCoverageGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("routecoveragegetprogressmonitordialog.title")); //$NON-NLS-1$
    }

}
