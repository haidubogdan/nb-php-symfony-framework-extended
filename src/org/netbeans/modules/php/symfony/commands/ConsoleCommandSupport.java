package org.netbeans.modules.php.symfony.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import static org.netbeans.modules.php.symfony.PhpNbConsts.NB_PHP_PROJECT_TYPE;
import static org.netbeans.modules.php.symfony.project.ComposerPackages.fromProjectDir;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author bhaidu
 */
public class ConsoleCommandSupport {

    public List<FrameworkCommand> commands = new ArrayList<>();

    private ConsoleCommandSupport() {

    }
    
    public List<FrameworkCommand> getCommands(){
        return commands;
    }
    
    public void addCommand(ConsoleCommand command) {
        commands.add(command);
    }
    
    public static ConsoleCommandSupport fromPhpModule(PhpModule phpModule) {
        Project project = phpModule.getLookup().lookup(Project.class);
        return project.getLookup().lookup(ConsoleCommandSupport.class);
    }
    
    @LookupProvider.Registration(projectType = NB_PHP_PROJECT_TYPE)
    public static LookupProvider createJavaBaseProvider() {
        return new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                return Lookups.fixed(new ConsoleCommandSupport()
                );
            }
        };
    }
}
