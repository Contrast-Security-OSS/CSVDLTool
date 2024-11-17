package com.contrastsecurity.csvdltool.ui.monitordialog.sbom;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.Messages;

public class SBOMGetProgressMonitorDialog extends ProgressMonitorDialog {

    public SBOMGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("sbomgetprogressmonitordialog.title")); //$NON-NLS-1$
    }

}
