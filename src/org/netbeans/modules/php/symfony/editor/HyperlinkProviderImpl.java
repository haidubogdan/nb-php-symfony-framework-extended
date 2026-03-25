package org.netbeans.modules.php.symfony.editor;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.symfony.project.ProjectUtils;
import org.netbeans.modules.php.symfony.utils.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * Similar to a declaration finder
 *
 * @author bhaidu
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = HyperlinkProviderExt.class),
//    @MimeRegistration(mimeType = "text/x-twig", service = HyperlinkProviderExt.class)
})
public class HyperlinkProviderImpl implements HyperlinkProviderExt {

    private static final int MIN_QUOTED_QUERY_TEXT_LENGTH = 5;

    //move to a handler
    public enum DeclarationType {
        TEMPLATE_PATH,
        ROUTE_LABEL,
        CONFIG_PATH;
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION, HyperlinkType.ALT_HYPERLINK);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        if (!isInSymfonyModule(doc)) {
            return false;
        }
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        if (!isInSymfonyModule(doc)) {
            return null;
        }
        if (!type.equals(HyperlinkType.GO_TO_DECLARATION)) {
            return null;
        }

        TokenSequence<PHPTokenId> tokensq = tokenSequenceForDeclFinder(doc, offset);

        if (tokensq == null) {
            return null;
        }

        return null;
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        switch (type) {
            case GO_TO_DECLARATION:
                PhpModule module = ProjectUtils.getPhpModule(doc);
                FileObject sourceDir = module.getSourceDirectory();
                if (sourceDir == null) {
                    return;
                }
                HyperlinkInfo info = getContextHyperlinkInfo(doc, offset, sourceDir);

                if (info != null) {
                    openDocument(info.getGoToFile(), info.getOffset());
                }

                break;
            case ALT_HYPERLINK:
                JTextComponent focused = EditorRegistry.focusedComponent();
                if (focused != null && focused.getDocument() == doc) {
                    focused.setCaretPosition(offset);
                }
                break;
        }
    }

    private void openDocument(FileObject f, int offset) {
        try {
            DataObject dob = DataObject.find(f);
            NbDocument.openDocument(dob, offset, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean isInSymfonyModule(Document doc) {
        return ProjectUtils.isInSymfonyModule(doc);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        PhpModule module = ProjectUtils.getPhpModule(doc);
        FileObject sourceDir = module.getSourceDirectory();
        String tooltipText = "";
        if (sourceDir != null) {
            HyperlinkInfo info = getContextHyperlinkInfo(doc, offset, sourceDir);
            if (info != null) {
                tooltipText = info.generateTooltipText();
            }
        }

        return "<html><body>" + tooltipText + "</body></html>";
    }

    private HyperlinkInfo getContextHyperlinkInfo(Document doc, int offset, FileObject sourceDir) {
        TokenSequence<PHPTokenId> tokensq = tokenSequenceForDeclFinder(doc, offset);

        if (tokensq == null) {
            return null;
        }

        return null;
    }

    private TokenSequence<PHPTokenId> tokenSequenceForDeclFinder(Document doc, int offset) {
        TokenSequence<PHPTokenId> tokensq = EditorUtils.getTokenSequence(doc, offset);

        if (tokensq == null) {
            return null;
        }

        Token<PHPTokenId> token = tokensq.token();

        if (token == null) {
            return null;
        }

        String focusedText = token.text().toString();

        //2 char config are not that relevant
        if (focusedText.length() < MIN_QUOTED_QUERY_TEXT_LENGTH
                || !StringUtils.isQuotedString(focusedText)) {
            return null;
        }
        return tokensq;
    }

    private static class HyperlinkInfo {

        private final String queryText;
        private final FileObject goToFile;
        private final int offset;
        private final DeclarationType type;

        public HyperlinkInfo(String queryText, FileObject goToFile, int offset, DeclarationType type) {
            this.queryText = queryText;
            this.goToFile = goToFile;
            this.offset = offset;
            this.type = type;
        }

        public String getQueryText() {
            return queryText;
        }

        public FileObject getGoToFile() {
            return goToFile;
        }

        public int getOffset() {
            return offset;
        }

        public String generateTooltipText() {
            StringBuilder builder = new StringBuilder();

            String prefixText = "";
            builder.append(prefixText);
            builder.append(getGoToFile().getPath());
            builder.append("<b></b><br><br><i style='margin-left:20px;'>");
            builder.append(getQueryText());
            builder.append("</i>");
            return builder.toString();
        }
    }
}
