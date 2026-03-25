/*
Licensed to the Apache Software Foundation (ASF)
 */
package org.netbeans.modules.php.symfony.editor.completion;

import java.util.Arrays;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.symfony.editor.EditorUtils;
import org.netbeans.modules.php.symfony.project.ProjectUtils;
import org.netbeans.modules.php.symfony.utils.SymfonyUtils;
import org.netbeans.modules.php.symfony.utils.StringUtils;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.filesystems.FileObject;

@MimeRegistrations({
    @MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = CompletionProvider.class),
    @MimeRegistration(mimeType = "text/x-twig", service = CompletionProvider.class)   
})
public class SymfonyCompletionProvider implements CompletionProvider {

    private String methodName;

    public static String[] QUERY_METHODS = new String[]{
    }; // NOI18N 

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        FileObject currentFile = NbEditorUtilities.getFileObject(component.getDocument());

        if (currentFile == null) {
            return null;
        }

        PhpModule module = ProjectUtils.getPhpModule(currentFile);

        if (module == null) {
            return null;
        }

        if (!ProjectUtils.isInSymfonyModule(module)) {
            return null;
        }

        String reference = getQueryString(component.getDocument(), component.getCaretPosition());
        //

        if (reference != null) {
            AsyncCompletionQuery completionQuery;

            switch (methodName) {
                default:
                    return null;
            }

            //return new AsyncCompletionTask(completionQuery, component);
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (component.getDocument() == null) {
            return 0;
        }
        if (!ProjectUtils.isInSymfonyModule(component.getDocument())) {
            return 0;
        }
        return COMPLETION_QUERY_TYPE;
    }

    private String getQueryString(Document doc, int offset) {

        TokenSequence<PHPTokenId> tokensq = EditorUtils.getTokenSequence(doc, offset);

        if (tokensq == null) {
            return null;
        }

        Token<PHPTokenId> currentToken = tokensq.token();

        if (currentToken == null) {
            return null;
        }

        PHPTokenId openParenToken = null;

        String quotedReference = ""; // NOI18N
        int tokenCount = 0;
        while (tokensq.movePrevious() && tokenCount <= 6) {
            Token<PHPTokenId> token = tokensq.token();
            if (token == null) {
                break;
            }

            String text = token.text().toString();
            PHPTokenId id = token.id();

            if (id.equals(PHPTokenId.WHITESPACE) || text.equals("[") || text.equals(",")) {
                continue;
            }

            tokenCount++;
            if (openParenToken != null && id.equals(PHPTokenId.PHP_STRING)) {
                if (Arrays.asList(QUERY_METHODS).indexOf(text) > -1) {
                    methodName = text;
                    quotedReference = currentToken.text().toString();
                }
                break;
            }

            if (EditorUtils.isOpenParenToken(id, text)) {
                openParenToken = id;
            }
        }

        if (quotedReference.length() < 2 || SymfonyUtils.isVariable(quotedReference)) {
            return null;
        }
        if (StringUtils.isQuotedString(quotedReference)) {
            String reference = quotedReference.substring(1, quotedReference.length() - 1);
            return reference;
        }
        return null;
    }

}
