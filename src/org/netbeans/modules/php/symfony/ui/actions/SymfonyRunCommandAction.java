/*
Licensed to the Apache Software Foundation (ASF)
 */
package org.netbeans.modules.php.symfony.ui.actions;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.symfony.SymfonyPhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author bogdan
 */
public final class SymfonyRunCommandAction extends RunCommandAction {

    private static final SymfonyRunCommandAction INSTANCE = new SymfonyRunCommandAction();

    private SymfonyRunCommandAction() {
    }

    public static SymfonyRunCommandAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(PhpModule phpModule) {
        if (!SymfonyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule)) {
            return;
        }

        SymfonyPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).openPanel();
    }

    @Messages({
        "# {0} - command name",
        "SymfonyRunCommandAction.name=Symfony: {0}",
    })
    @Override
    protected String getFullName() {
        return Bundle.SymfonyRunCommandAction_name(getPureName());
    }
}
