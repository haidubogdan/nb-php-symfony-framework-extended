package org.netbeans.modules.php.symfony.project;

import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.symfony.editor.parser.ControllersClassParser;
import static org.netbeans.modules.php.symfony.PhpNbConsts.NB_PHP_PROJECT_TYPE;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class SymfonyAppSupport {

    private final FileObject projectDir;
    private final ControllersClassParser controllerClassParser = new ControllersClassParser();
    private boolean scanned = false;

    public SymfonyAppSupport(Project project) {
        this.projectDir = project.getProjectDirectory();
    }

    public synchronized void scannProject() {
        controllerClassParser.parseControllers(projectDir);
        scanned = true;
    }

    public synchronized boolean filesScanned() {
        return scanned;
    }

    public ControllersClassParser getControllerClassParser() {
        return controllerClassParser;
    }

    @LookupProvider.Registration(projectType = NB_PHP_PROJECT_TYPE)
    public static LookupProvider createJavaBaseProvider() {
        return new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                Project project = baseContext.lookup(Project.class);
                return Lookups.fixed(new SymfonyAppSupport(project)
                );
            }
        };
    }
    
    public static SymfonyAppSupport getInstance(Document doc) {
        Project project = ProjectUtils.get(doc);
        SymfonyAppSupport support = project.getLookup().lookup(SymfonyAppSupport.class);

        if (support == null) {
            return null;
        }

        if (!support.filesScanned()) {
            support.scannProject();
            NotificationDisplayer.getDefault().notify("Symfony: Project files parsed",
                    ImageUtilities.loadImageIcon("org/netbeans/modules/git/resources/icons/info.png", false),
                    "",
                    evt -> {
                    }
            );
        }

        return support;
    }
}
