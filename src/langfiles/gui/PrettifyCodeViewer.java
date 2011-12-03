package langfiles.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import langfiles.project.CodeViewer;
import langfiles.util.SyncFile;
import prettify.SyntaxHighlighter;
import prettify.gui.SyntaxHighlighterPane;
import prettify.theme.Style;
import prettify.theme.ThemeDefault;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class PrettifyCodeViewer implements CodeViewer {

    protected SyntaxHighlighter highlighter;

    public PrettifyCodeViewer() {
        highlighter = new SyntaxHighlighter(new ThemeLangFilesTool(), new LangFilesToolHighlighterPane());
    }

    @Override
    public void setCode(SyncFile syncFile) {
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

        protected ThemeLangFilesTool() {
            super();
            setFont(new Font("Tahoma", Font.PLAIN, 11));
            setGutterTextFont(new Font("Verdana", Font.PLAIN, 10));

            Style stringStyle = new Style();
            stringStyle.setColor(Color.red);
            setString(stringStyle);
        }
    }

    protected class LangFilesToolHighlighterPane extends SyntaxHighlighterPane {

        private static final long serialVersionUID = 1L;

        protected LangFilesToolHighlighterPane() {
            super();
        }

        @Override
        public void setStyle(List<Object> styleList) {
            super.setStyle(styleList);

            FontMetrics fontMetrics = getFontMetrics(getFont());
            int fontHeight = fontMetrics.getHeight() - fontMetrics.getLeading();
            float alignmentY = (float) fontMetrics.getAscent() / (float) fontHeight;

            int minusedLength = 0;
            for (int i = 0, iEnd = styleList.size(); i < iEnd; i += 2) {
                Integer offset = (Integer) styleList.get(i);
                Integer length = (i + 2 < iEnd ? (Integer) styleList.get(i + 2) : getDocument().getLength()) - offset;
                String styleKeyword = (String) styleList.get(i + 1);

                offset -= minusedLength;

                if (!styleKeyword.equals("str") || length == 0) {
                    continue;
                }

                String _content = "";
                try {
                    _content = getDocument().getText(offset, length);
                    getDocument().remove(offset, length);
                } catch (BadLocationException ex) {
                    Logger.getLogger(LangFilesToolHighlighterPane.class.getName()).log(Level.SEVERE, null, ex);
                }
                setCaretPosition(offset);

                //<editor-fold defaultstate="collapsed" desc="checkbox">
                JCheckBox checkBox = new JCheckBox(_content);

                checkBox.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                checkBox.setOpaque(false);
                checkBox.setAlignmentY(alignmentY);

                Style style = getTheme().getStyle(styleKeyword);
                checkBox.setForeground(style.getColor());
                checkBox.setBackground(style.getBackground());
                checkBox.setFont(setFont(getFont(), style.isBold(), style.isItalic()));

                checkBox.setPreferredSize(new Dimension(checkBox.getPreferredSize().width, fontHeight));

                insertComponent(checkBox);
                //</editor-fold>

                minusedLength += length - 1;
            }

            setCaretPosition(0);
        }
    }
}
