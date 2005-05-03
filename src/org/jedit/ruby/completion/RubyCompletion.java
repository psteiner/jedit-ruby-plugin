/*
 * RubyCompletion.java -
 *
 * Copyright 2005 Robert McKinnon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jedit.ruby.completion;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.jedit.ruby.ast.Method;
import org.jedit.ruby.ri.RDocViewer;
import org.jedit.ruby.RubyPlugin;
import sidekick.SideKickCompletion;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;

/**
 * @author robmckinnon at users.sourceforge.net
 */
public class RubyCompletion extends SideKickCompletion {

    private List<Method> methods;
    private String partialMethod;
    private JWindow frame;
    private JTextPane textPane;

    public RubyCompletion(View view, String partialMethod, List<Method> methods) {
        super(view, partialMethod == null ? "." : "." + partialMethod, methods);
        this.methods = methods;
        this.partialMethod = partialMethod;
        frame = new JWindow((Frame)null);
        frame.setFocusable(false);
        textPane = new JTextPane();
        textPane.setEditorKit(new HTMLEditorKit());
		JScrollPane scroller = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.getContentPane().add(scroller, BorderLayout.CENTER);
        frame.setSize(400,400);
    }

    public boolean handleKeystroke(int selectedIndex, char keyChar) {
        boolean emptyPopup = selectedIndex == -1;
        boolean space = keyChar == ' ';
        boolean stillTyping = !space && keyChar != '\t' && keyChar != '\n';

        if (stillTyping || emptyPopup) {
            textArea.userInput(keyChar);
        } else {
            insert(selectedIndex);
            if(space) {
                textArea.userInput(' ');
            }
        }

        return stillTyping;
    }

    public void insert(int index) {
        insert(methods.get(index));
    }

    public String getCompletionDescription(int index) {
        RDocViewer.setMethod(methods.get(index));
        return null;
    }

    private void insert(Method method) {
        Buffer buffer = view.getBuffer();
        RubyPlugin.log("method: " + method.getName(), getClass());
        int caretPosition = textArea.getCaretPosition();
        int offset = caretPosition;

        if (partialMethod != null) {
            offset -= partialMethod.length();
            buffer.remove(offset, partialMethod.length());
        }

        buffer.insert(offset, method.getName() + "()");
        textArea.setCaretPosition(textArea.getCaretPosition()-1);
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

}