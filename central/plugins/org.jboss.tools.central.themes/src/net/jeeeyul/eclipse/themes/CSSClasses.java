// Copyright 2012 Jeeeyul Lee, Seoul, Korea
// https://github.com/jeeeyul/pde-tools
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
// EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
// LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
// GPL, GNU General Public License, V2 or later, http://www.gnu.org/licenses/gpl.html
// AL, Apache License, V2.0 or later, http://www.apache.org/licenses
// BSD, BSD License, http://www.opensource.org/licenses/bsd-license.php
// MIT, MIT License, http://www.opensource.org/licenses/MIT
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.
package net.jeeeyul.eclipse.themes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.swt.widgets.Widget;

@SuppressWarnings("restriction")
public class CSSClasses {

	private List<String> classes = new ArrayList<String>();

	public CSSClasses(String source) {
		if (source != null && !source.trim().isEmpty())
			for (String name : source.split(" "))
				classes.add(name);
	}

	public boolean add(String className) {
		if (classes.contains(className))
			return false;
		return classes.add(className);
	}

	public boolean contains(String className) {
		return classes.contains(className);
	}

	public boolean remove(String className) {
		return classes.remove(className);
	}

	public static CSSClasses getStyleClasses(Widget w) {
		String literal = (String) w.getData(CSSSWTConstants.CSS_CLASS_NAME_KEY);
		return new CSSClasses(literal);
	}

	public static void setStyleClasses(Widget w, CSSClasses newStyleClasses) {
		w.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY,
				newStyleClasses.toString());
	}

	public String toString() {
		if (classes.isEmpty())
			return "";
		Iterator<String> iter = classes.iterator();
		StringBuilder sb = new StringBuilder(iter.next());
		while (iter.hasNext())
			sb.append(" ").append(iter.next());
		return sb.toString();
	}
}