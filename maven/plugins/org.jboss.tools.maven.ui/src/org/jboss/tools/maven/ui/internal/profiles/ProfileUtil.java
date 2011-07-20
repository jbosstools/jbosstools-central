package org.jboss.tools.maven.ui.internal.profiles;

import java.util.Collection;

public class ProfileUtil {

	private ProfileUtil(){} 
	
	private static final String COMMA = ", "; 
	
	public static String toString(Collection<ProfileSelection> profiles) {
		StringBuilder sb = new StringBuilder();
		if(profiles != null && !profiles.isEmpty()) {
			boolean addComma = false;
			for (ProfileSelection ps : profiles) {
				if (Boolean.TRUE.equals(ps.getSelected())) {
					if (addComma) {
						sb.append(COMMA);
					}
					sb.append(ps.toMavenString());
					addComma = true;
				}
			}
		}
		return sb.toString();
	}
}
