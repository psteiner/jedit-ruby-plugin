/*
 * RubySideKickParser.java - Side Kick Parser for Ruby
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
package org.jedit.ruby;

import sidekick.SideKickParser;
import sidekick.SideKickParsedData;
import org.gjt.sp.jedit.Buffer;
import org.jruby.lexer.yacc.SourcePosition;
import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author robmckinnon at users.sourceforge.net
 */
public class RubySideKickParser extends SideKickParser {

    public RubySideKickParser() {
        super("ruby");
    }

    public SideKickParsedData parse(final Buffer buffer, final DefaultErrorSource errorSource) {
        String text = buffer.getText(0, buffer.getLength());

        RubyParser.WarningListener listener = new RubyParser.WarningListener() {
            public void warn(SourcePosition position, String message) {
                addWarning(message, position, buffer, errorSource);
            }
            public void warn(String message) {
                addWarning(message, null, buffer, errorSource);
            }
            public void warning(SourcePosition position, String message) {
                addWarning(message, position, buffer, errorSource);
            }
            public void warning(String message) {
                addWarning(message, null, buffer, errorSource);
            }

            public void error(SourcePosition position, String message) {
                addError(message, position, buffer, errorSource);
            }
        };

        SideKickParsedData data = new SideKickParsedData(buffer.getName());
        DefaultMutableTreeNode parentNode = data.root;
        Member[] members = RubyParser.getMembers(text, buffer.getPath(), listener, true).getMembers();
        addNodes(parentNode, members, buffer);

        return data;
    }

    private void addWarning(String message, SourcePosition position, Buffer buffer, DefaultErrorSource errorSource) {
        addToErrorList(ErrorSource.WARNING, position, buffer, errorSource, message);
    }

    private void addError(String message, SourcePosition position, Buffer buffer, DefaultErrorSource errorSource) {
        addToErrorList(ErrorSource.ERROR, position, buffer, errorSource, message);
    }

    private void addToErrorList(int type, SourcePosition position, Buffer buffer, DefaultErrorSource errorSource, String message) {
        int line = position == null ? 0 : position.getLine() - 1;
        String lineText = buffer.getLineText(line);
        if(lineText.length() == 0) {
            buffer.insert(buffer.getLineStartOffset(line), "---");
        }
//                int line2 = line == 0 ? line : line - 1;
//        int lineStartOffset = buffer.getLineStartOffset(line);
//        int lineEndOffset = buffer.getLineEndOffset(line);
//        int end = lineEndOffset - lineStartOffset;
        errorSource.addError(type, buffer.getPath(), line, 0, 0, message);
    }

    private void addNodes(DefaultMutableTreeNode parentNode, Member[] members, Buffer buffer) {
        if(members != null) {
            for(Member member : members) {
                MemberNode node = new MemberNode(member);
                node.start = buffer.createPosition(member.getOffset());
                node.end = buffer.createPosition(member.getOffset());
                DefaultMutableTreeNode treeNode = node.createTreeNode();
                if(member.hasChildMembers()) {
                    Member[] childMembers = member.getChildMembers();
                    addNodes(treeNode, childMembers, buffer);
                }
                parentNode.add(treeNode);
            }
        }
    }
}
