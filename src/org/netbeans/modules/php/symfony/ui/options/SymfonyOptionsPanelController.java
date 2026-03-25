/*
Licensed to the Apache Software Foundation (ASF)
 */
package org.netbeans.modules.php.symfony.ui.options;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@UiUtils.PhpOptionsPanelRegistration(
        id = SymfonyOptionsPanelController.ID,
        displayName = "Symfony",
        position = 2200
)
public class SymfonyOptionsPanelController extends OptionsPanelController {

    static final String ID = "Symfony"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH + "/" + ID; // NOI18N

    private SymfonyOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
    @Override
    public void update() {
        getPanel().load();
    }

    @Override
    public void applyChanges() {
        getPanel().store();
    }

    @Override
    public void cancel() {
        getPanel().cancel();
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return getPanel().changed();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SymfonyOptions.OPTIONS_ID); //NOI18N
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private SymfonyOptionsPanel getPanel() {
        if (panel == null) {
            panel = new SymfonyOptionsPanel(this);
        }
        return panel;
    }

    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }
}
