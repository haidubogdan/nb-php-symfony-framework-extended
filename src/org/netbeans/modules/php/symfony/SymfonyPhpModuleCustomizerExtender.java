/*
Licensed to the Apache Software Foundation (ASF)
 */
package org.netbeans.modules.php.symfony;

import java.util.EnumSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.symfony.preferences.SymfonyPreferences;
import org.netbeans.modules.php.symfony.ui.customizer.SymfonyCustomizerPanel;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.openide.util.HelpCtx;
import org.netbeans.modules.php.symfony.project.ComposerPackages;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author bogdan
 */
public class SymfonyPhpModuleCustomizerExtender extends PhpModuleCustomizerExtender {

    private final PhpModule phpModule;
    private final ComposerPackages composerPackages;
    private final boolean isFrameworkEnabledOnProject;

    // @GuardedBy(EDT)
    private SymfonyCustomizerPanel component;
    
    SymfonyPhpModuleCustomizerExtender(PhpModule phpModule) {
        this.phpModule = phpModule;
        composerPackages = ComposerPackages.fromProjectDir(phpModule.getProjectDirectory());
        isFrameworkEnabledOnProject = SymfonyPreferences.fromPhpModule(phpModule).hasEnabledConfigured();
    }

    @Messages("LBL_Symfony=Symfony")
    @Override
    public String getDisplayName() {
        return Bundle.LBL_Symfony();
    }
    
    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        return composerPackages != null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    //TODO check flow
    @Override
    public EnumSet<Change> save(PhpModule pm) {
        if (component == null){
            return null;
        }

        component.saveChanges(pm);
        
        if (isFrameworkEnabledOnProject != component.isFrameworkEnabled()){
            //?? what is the purpose
            return EnumSet.of(Change.FRAMEWORK_CHANGE);
        }
              
        return null;
    }
    
    private SymfonyCustomizerPanel getPanel() {
        if (component == null) {
            component = new SymfonyCustomizerPanel(SymfonyPreferences.fromPhpModule(phpModule));
            component.setSymfonyVersion(composerPackages.getSymfonyVersion());
            component.initModuleValues();
            component.initPluginInfo(phpModule.getSourceDirectory());
        }
        return component;
    }
}
