package org.netbeans.modules.php.symfony.commands;

import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;

/**
 *
 * @author bhaidu
 */
public class ConsoleCommand extends FrameworkCommand {
    public static final String CONSOLE_COMMAND = "bin/console"; // NOI18N

    public ConsoleCommand(String command, String description, String displayName) {
        super(command, description, displayName);
    }

    @Override
    protected String getHelpInternal() {
        return ""; // NOI18N
    }

    @Override
    public String getPreview() {
        return CONSOLE_COMMAND + " " + super.getPreview(); // NOI18N
    }
}
