package com.contrastsecurity.csvdltool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class ServerGetProgressMonitorDialog extends ProgressMonitorDialog {

    public ServerGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("サーバ一覧の読み込み");
    }

}
