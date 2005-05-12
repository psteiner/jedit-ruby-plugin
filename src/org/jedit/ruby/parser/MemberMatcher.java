/*
 * MemberMatcher.java - 
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
package org.jedit.ruby.parser;

import gnu.regexp.REException;
import gnu.regexp.REMatch;
import gnu.regexp.RE;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.jedit.ruby.ast.*;
import org.jedit.ruby.RubyPlugin;

/**
 * @author robmckinnon at users.sourceforge.net
 */
interface MemberMatcher {

    List<Match> getMatches(String text) throws REException;

    Member createMember(String name, String filePath, int startOuterOffset, int startOffset, String params);

    static class Match {
        String value;
        int startOuterOffset;
        int startOffset;
        String params;

        public Match(String value, int startOuterOffset, int startOffset, String params) {
            this.params = params;
            this.startOffset = startOffset;
            this.startOuterOffset = startOuterOffset;
            this.value = value;
        }
    }

    static class ModuleMatcher extends AbstractMatcher {

        public List<Match> getMatches(String text) throws REException {
            return getMatchList("([ ]*)(module[ ]+)(\\w+[^;\\s]*)", text);
        }

        public Member createMember(String name, String filePath, int startOuterOffset, int startOffset, String params) {
            return new Module(name, startOuterOffset, startOffset);
        }
    }

    static class ClassMatcher extends AbstractMatcher {

        public List<Match> getMatches(String text) throws REException {
            return getMatchList("([ ]*)(class[ ]+)(\\w+[^;\\s]*)", text);
        }

        public Member createMember(String name, String filePath, int startOuterOffset, int startOffset, String params) {
            RubyPlugin.log("class: " + name, getClass());
            return new ClassMember(name, startOuterOffset, startOffset);
        }
    }

    static class MethodMatcher extends AbstractMatcher {

        public List<Match> getMatches(String text) throws REException {
            String paramPattern =
                    "(\\(.*\\))"+
                    "|"+
                    "(.*)";
            return getMatchList("([ ]*)(def[ ]+)([^;\\(\\s]*)(" + paramPattern + ")?", text);
        }

        public Member createMember(String name, String filePath, int startOuterOffset, int startOffset, String params) {
            String fileName = (new File(filePath)).getName();
            if(params.startsWith(" ")) {
                params = '('+params.trim()+')';
            }
            return new Method(name, params, filePath, fileName, startOuterOffset, startOffset, false);
        }
    }

    static abstract class AbstractMatcher implements MemberMatcher {

        private REMatch[] getMatches(String expression, String text) throws REException {
            RE re = new RE(expression, 0);
            return re.getAllMatches(text);
        }

        protected List<Match> getMatchList(String pattern, String text) throws REException {
            REMatch[] matches = getMatches(pattern, text);
            List<Match> matchList = new ArrayList<Match>();
            int start = 0;

            for(REMatch reMatch : matches) {
                if(onlySpacesBeforeMatch(reMatch, text, start)) {
                    String value = reMatch.toString(3).trim();
                    int startOuterOffset = reMatch.getStartIndex(1);
                    int startIndex = reMatch.getStartIndex(3);
                    String params = reMatch.toString(4);
                    if(params != null && params.indexOf(';') != -1) {
                        params = params.substring(0, params.indexOf(';'));
                    }
                    Match match = new Match(value, startOuterOffset, startIndex, params);
                    matchList.add(match);
                    start = text.indexOf(reMatch.toString()) + reMatch.toString().length();
                }
            }

            return matchList;
        }

        private boolean onlySpacesBeforeMatch(REMatch match, String text, int stop) {
            int index = match.getStartIndex() - 1;
            boolean onlySpaces = true;

            if(index >= stop) {
                char nextCharacter = text.charAt(index);

                while(onlySpaces && index >= stop && nextCharacter != '\n' && nextCharacter != '\r') {
                    char character = text.charAt(index--);
                    onlySpaces = character == ' ' || character == '\t' || character == ';';
                    if(index >= stop) {
                        nextCharacter = text.charAt(index);
                    }
                }
            }
            return onlySpaces;
        }
    }

}
