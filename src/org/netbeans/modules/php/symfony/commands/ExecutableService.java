package org.netbeans.modules.php.symfony.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.extexecution.docker.DockerExecutable;
import org.netbeans.modules.php.symfony.PhpNbConsts;
import org.netbeans.modules.php.symfony.ui.options.SymfonyOptionsPanelController;
import org.netbeans.modules.php.symfony.preferences.SymfonyPreferences;
import org.netbeans.modules.php.symfony.project.ComposerPackages;
import org.openide.filesystems.FileUtil;
import static org.netbeans.modules.php.symfony.commands.ConsoleCommand.CONSOLE_COMMAND;

/**
 *
 * @author bhaidu
 */
public class ExecutableService {

    public static void extractConsoleCommands(PhpModule phpModule,
            ConsoleCommandSupport artisanCommandSupport) {

        CommandLineProcessor commandsProcessor = new CommandLineProcessor(artisanCommandSupport);
        List<String> params = new ArrayList<>();
        executeCommand(phpModule, params, commandsProcessor);
    }

    public static void executeCommand(PhpModule phpModule,
            List<String> params, LineProcessor outLineProcessor) {

        SymfonyPreferences prefs = SymfonyPreferences.fromPhpModule(phpModule);
        if (useDocker(prefs)) {
            if (!params.isEmpty() && params.get(0).startsWith("./vendor")) { // NOI18N
                //do nothing
            } else {
                params.add(0, PhpNbConsts.PHP_COMMAND);
                params.add(1, ConsoleScript.SCRIPT_NAME);
            }
            boolean isRemote = useRemoteConnection(prefs);
            DockerExecutable exec = new DockerExecutable(
                    getDockerContainerName(prefs),
                    getDockerBashPath(prefs),
                    params,
                    isRemote
            ).displayName(getDisplayName(phpModule))
                    .containerWorkDir(getDockerWorkdir(prefs))
                    .setUserInteractive(getDockerUseInteractive(prefs))
                    .setUseTTY(getDockerUseTTY(prefs))
                    .setDockerUser(getDockerUser(prefs))
                    ;

            ExecutionDescriptor.InputProcessorFactory2 descriptor = null;

            if (outLineProcessor != null) {
                descriptor = getOutPtyProcessorFactory(outLineProcessor);
            }

            exec.run(exec.getDescriptor(null), descriptor);
        } else {
            ExecutionDescriptor executionDescriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                    .optionsPath(SymfonyOptionsPanelController.getOptionsPath())
                    .inputVisible(true);

            ExecutionDescriptor.InputProcessorFactory2 descriptor = null;

            if (outLineProcessor != null) {
                descriptor = getOutProcessorFactory(outLineProcessor);
            }

            createPhpExecutable(phpModule)
                    .displayName(getDisplayName(phpModule))
                    .additionalParameters(params)
                    .run(executionDescriptor, descriptor);
        }
    }

    private static String getDisplayName(PhpModule phpModule) {
        ComposerPackages composerPackages = ComposerPackages.loadFromPhpModule(phpModule);

        if (composerPackages == null || composerPackages.getSymfonyVersion() == null) {
            return phpModule.getDisplayName() + " CLI"; // NOI18N
        }

        return phpModule.getDisplayName() + " " + composerPackages.getSymfonyVersion() + " CLI"; // NOI18N
    }

    private static boolean useRemoteConnection(SymfonyPreferences prefs) {
        return prefs.getRemoteConnectionFlag();
    }

    private static boolean useDocker(SymfonyPreferences prefs) {
        return prefs.getUseDocker();
    }

    private static String getDockerContainerName(SymfonyPreferences prefs) {
        return prefs.getDockerContainerName();
    }

    private static String getDockerWorkdir(SymfonyPreferences prefs) {
        return prefs.geDockerWorkdir();
    }

    private static String getDockerBashPath(SymfonyPreferences prefs) {
        return prefs.getDockerBashPath();
    }

    private static boolean getDockerUseInteractive(SymfonyPreferences prefs) {
        return prefs.getDockerUseInteractive();
    }

    private static boolean getDockerUseTTY(SymfonyPreferences prefs) {
        return prefs.getDockerUseTTy();
    }
    
    private static String getDockerUser(SymfonyPreferences prefs) {
        return prefs.getDockerUser();
    }
    
    private static PhpExecutable createPhpExecutable(PhpModule phpModule) {
        String absolutePath = FileUtil.toFile(phpModule.getSourceDirectory()).getAbsolutePath();
        return new PhpExecutable(absolutePath + "/" + CONSOLE_COMMAND)
                .environmentVariables(Collections.singletonMap("SHELL_INTERACTIVE", "true")) // NOI18N
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()));
    }

    private static ExecutionDescriptor.InputProcessorFactory2 getOutProcessorFactory(final LineProcessor lineProcessor) {
        return new ExecutionDescriptor.InputProcessorFactory2() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                //strip ansi chars before extracting the commands
                return InputProcessors.ansiStripping(InputProcessors.bridge(lineProcessor));
            }
        };
    }

    private static ExecutionDescriptor.InputProcessorFactory2 getOutPtyProcessorFactory(final LineProcessor lineProcessor) {
        return new ExecutionDescriptor.InputProcessorFactory2() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                //strip ansi chars before extracting the commands
                return InputProcessors.proxy(InputProcessors.bridge(lineProcessor));
            }
        };
    }

    public static class CommandLineProcessor implements LineProcessor {

        private final ConsoleCommandSupport consoleCommandSupport;
        private boolean collectCommands = false;

        public CommandLineProcessor(ConsoleCommandSupport consoleCommandSupport) {
            this.consoleCommandSupport = consoleCommandSupport;
        }

        @Override
        public void processLine(String line) {
            line = stripAnsiColors(line);
            if (collectCommands) {
                if (line.startsWith("  ")) {
                    String trimedLine = line.trim();
                    int endBoundry = trimedLine.indexOf(" ");
                    String commandInfo = trimedLine.substring(0, endBoundry);
                    int commandActionPos = commandInfo.indexOf(":");
                    String comment = "";
                    if (line.length() > endBoundry + 1) {
                        comment = line.substring(endBoundry + 2).trim();
                    }
                    if (commandActionPos > 0) {
                        consoleCommandSupport.addCommand(new ConsoleCommand(commandInfo,
                                comment, commandInfo));
                    } else {
                        consoleCommandSupport.addCommand(new ConsoleCommand(commandInfo,
                                comment, commandInfo));
                    }
                }
            }
            if (line.contains("Available commands:")) { // NOI18N
                collectCommands = true;
            }
        }

        private static String stripAnsiColors(String sequence) {
            StringBuilder sb = new StringBuilder(sequence.length());
            int index = 0;
            int max = sequence.length();
            while (index < max) {
                int nextEscape = sequence.indexOf("\033[", index); // NOI18N
                if (nextEscape == -1) {
                    nextEscape = sequence.length();
                }

                for (int n = (nextEscape == -1) ? max : nextEscape; index < n; index++) {
                    sb.append(sequence.charAt(index));
                }

                if (nextEscape != -1) {
                    for (; index < max; index++) {
                        char c = sequence.charAt(index);
                        if (c == 'm') {
                            index++;
                            break;
                        }
                    }
                }
            }

            return sb.toString();
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {

        }
    }
}
