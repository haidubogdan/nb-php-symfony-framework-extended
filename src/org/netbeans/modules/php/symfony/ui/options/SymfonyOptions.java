package org.netbeans.modules.php.symfony.ui.options;

/**
 *
 * @author bogdan
 */
public class SymfonyOptions {

    public static final String OPTIONS_ID = "org.netbeans.modules.php.symfony.editor.Options"; // NOI18N

    private static final SymfonyOptions INSTANCE = new SymfonyOptions();

    public static SymfonyOptions getInstance() {
        return INSTANCE;
    }

}
