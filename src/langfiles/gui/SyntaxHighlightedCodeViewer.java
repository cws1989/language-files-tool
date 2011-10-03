package langfiles.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import langfiles.project.CodeViewer;
import langfiles.util.SyncFile;
import syntaxhighlighter.Brushes.BrushJava;
import syntaxhighlighter.Parser.MatchResult;
import syntaxhighlighter.SyntaxHighlighter;
import syntaxhighlighter.SyntaxHighlighterPane;
import syntaxhighlighter.Theme.Style;
import syntaxhighlighter.Themes.ThemeDefault;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class SyntaxHighlightedCodeViewer implements CodeViewer {

    protected SyntaxHighlighter highlighter;

    public SyntaxHighlightedCodeViewer() {
        highlighter = new SyntaxHighlighter(new BrushJava(), new ThemeLangFilesTool(), new LangFilesToolHighlighterPane());
    }

    @Override
    public void setCode(final SyncFile syncFile) {
        try {
            highlighter.setContent(syncFile.getFile());
        } catch (IOException ex) {
            Logger.getLogger(SyntaxHighlightedCodeViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JComponent getGUI() {
        return highlighter;
    }

    protected class ThemeLangFilesTool extends ThemeDefault {

        public ThemeLangFilesTool() {
            super();
            setFont(new Font("Tahoma", Font.PLAIN, 11));
            setGutterTextFont(new Font("Verdana", Font.PLAIN, 10));

            Style stringStyle = new Style();
            stringStyle.setColor(Color.red);
            setString(stringStyle);
        }
    }

    protected class LangFilesToolHighlighterPane extends SyntaxHighlighterPane {

        public LangFilesToolHighlighterPane() {
            super();
        }

        @Override
        public void setStyle(Map<String, List<MatchResult>> styleList) {
            Map<Integer, MatchResult> orderedMap = new TreeMap<Integer, MatchResult>();
            for (List<MatchResult> results : styleList.values()) {
                for (MatchResult result : results) {
                    orderedMap.put(result.getOffset(), result);
                }
            }

            super.setStyle(styleList);

            FontMetrics fontMetrics = getFontMetrics(getFont());
            int fontHeight = fontMetrics.getHeight() - fontMetrics.getLeading();
            float alignmentY = (float) fontMetrics.getAscent() / (float) fontHeight;

            int minusedLength = 0;
            for (MatchResult result : orderedMap.values()) {
                if (!result.getStyleKey().equals("string") || result.getLength() == 0) {
                    continue;
                }

                int offset = result.getOffset() - minusedLength;

                String _content = "";
                try {
                    _content = getDocument().getText(offset, result.getLength());
                    getDocument().remove(offset, result.getLength());
                } catch (BadLocationException ex) {
                    Logger.getLogger(LangFilesToolHighlighterPane.class.getName()).log(Level.SEVERE, null, ex);
                }
                setCaretPosition(offset);

                //<editor-fold defaultstate="collapsed" desc="checkbox">
                JCheckBox checkBox = new JCheckBox(_content);

                checkBox.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                checkBox.setOpaque(false);
                checkBox.setAlignmentY(alignmentY);

                Style style = getTheme().getStyle(result.getStyleKey());
                checkBox.setForeground(style.getColor());
                checkBox.setBackground(style.getBackground());
                checkBox.setFont(setFont(getFont(), style.isBold(), style.isItalic()));

                checkBox.setPreferredSize(new Dimension(checkBox.getPreferredSize().width, fontHeight));

                insertComponent(checkBox);
                //</editor-fold>

                minusedLength += result.getLength() - 1;
            }

            setCaretPosition(0);
        }
    }
}
