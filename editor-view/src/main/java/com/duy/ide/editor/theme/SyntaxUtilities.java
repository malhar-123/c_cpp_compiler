/*
 * SyntaxUtilities.java - Syntax and styles utility utility functions
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2008 Matthieu Casanova, Slava Pestov
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

package com.duy.ide.editor.theme;


import android.graphics.Color;

import com.duy.common.DLog;
import com.duy.ide.editor.theme.model.SyntaxStyle;

import org.gjt.sp.jedit.awt.Font;
import org.gjt.sp.jedit.syntax.Token;

import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Syntax utilities that depends on JDK only and syntax package.
 *
 * @author Matthieu Casanova
 * @version $Id: StandardUtilities.java 9871 2007-06-28 16:33:20Z Vampire0 $
 * @since 4.3pre13
 */
public class SyntaxUtilities {


    private static final String TAG = "SyntaxUtilities";


    private SyntaxUtilities() {
    }

    /**
     * @since jEdit 4.3pre13
     */
    public static Integer parseColor(String name, Integer defaultColor) {
        try {
            return Color.parseColor(name);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    /**
     * Converts a style string to a style object.
     *
     * @param str            The style string
     * @param defaultFgColor Default foreground color (if not specified in style string)
     * @throws IllegalArgumentException if the style is invalid
     * @since jEdit 4.3pre17
     */
    public static SyntaxStyle parseStyle(String str, int defaultFgColor)
            throws IllegalArgumentException {
        int fgColor = defaultFgColor;
        Integer bgColor = null;
        boolean italic = false;
        boolean bold = false;
        StringTokenizer st = new StringTokenizer(str);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.startsWith("color:")) {
                String substring = s.substring(6);
                fgColor = parseColor(substring, Color.BLACK);
            } else if (s.startsWith("bgColor:")) {
                bgColor = parseColor(s.substring(8), null);
            } else if (s.startsWith("style:")) {
                for (int i = 6; i < s.length(); i++) {
                    if (s.charAt(i) == 'i')
                        italic = true;
                    else if (s.charAt(i) == 'b')
                        bold = true;
                    else
                        throw new IllegalArgumentException(
                                "Invalid style: " + s);
                }
            } else
                throw new IllegalArgumentException(
                        "Invalid directive: " + s);
        }
        return new SyntaxStyle(fgColor, bgColor, new Font((italic ? Font.ITALIC : 0) | (bold ? Font.BOLD : 0)));
    }

    /**
     * Converts a style string to a style object.
     *
     * @param str The style string
     * @throws IllegalArgumentException if the style is invalid
     * @since jEdit 4.3pre13
     */
    public static SyntaxStyle parseStyle(String str)
            throws IllegalArgumentException {
        return parseStyle(str, Color.BLACK);
    }


    /**
     * Loads the syntax styles from the properties, giving them the specified
     * base font family and size.
     *
     * @since jEdit 4.3pre13
     */
    public static SyntaxStyle[] loadStyles(Properties properties) {
        SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

        // start at 1 not 0 to skip Token.NULL
        for (int i = 1; i < styles.length; i++) {
            String styleName = "view.style." + Token.tokenToString((byte) i).toLowerCase(Locale.ENGLISH);
            try {
                String property = properties.getProperty(styleName);
                if (DLog.DEBUG) DLog.d(TAG, "styleName = " + styleName);
                if (DLog.DEBUG) DLog.d(TAG, "property = " + property);
                styles[i] = parseStyle(property);
            } catch (Exception e) {
                if (DLog.DEBUG) DLog.e(TAG, "loadStyles: failed " + styleName);
            }
        }

        return styles;
    }
}