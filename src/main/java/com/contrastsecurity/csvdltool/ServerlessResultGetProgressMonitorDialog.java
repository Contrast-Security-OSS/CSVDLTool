package com.contrastsecurity.csvdltool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class ServerlessResultGetProgressMonitorDialog extends ProgressMonitorDialog {

    public ServerlessResultGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("サーバレス結果一覧の読み込み");
    }

}
