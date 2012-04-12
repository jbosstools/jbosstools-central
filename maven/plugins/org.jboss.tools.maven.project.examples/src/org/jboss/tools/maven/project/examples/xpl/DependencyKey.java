package org.jboss.tools.maven.project.examples.xpl;

import java.io.Serializable;

import org.eclipse.osgi.util.NLS;

public class DependencyKey implements Serializable {
  private static final long serialVersionUID = -8984509272834024387L;
  
  private final String groupId;
  private final String type;
  private final String artifactId;
  private final String version;
  private final String classifier;

  public DependencyKey(String groupId, String artifactId, String version, String classifier, String type) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
    this.type = type;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof DependencyKey) {
      DependencyKey other = (DependencyKey) o;
      return equals(groupId, other.groupId)
          && equals(artifactId, other.artifactId)
          && equals(version, other.version)
          && equals(type, other.type)
          && equals(classifier, other.classifier);
    }
    return false;
  }

  public int hashCode() {
    int hash = 17;
    hash = hash * 31 + (groupId != null? groupId.hashCode(): 0);
    hash = hash * 31 + (artifactId != null? artifactId.hashCode(): 0);
    hash = hash * 31 + (version != null? version.hashCode(): 0);
    hash = hash * 31 + (type != null? type.hashCode(): 0);
    hash = hash * 31 + (classifier != null? classifier.hashCode(): 0);
    return hash;
  }

  private static boolean equals(Object o1, Object o2) {
    return o1 == null? o2 == null: o1.equals(o2);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(groupId).append(':').append(artifactId).append(':')
      .append(type).append(':').append(version);
    if(classifier != null) {
      sb.append(':').append(classifier);
    }
    return sb.toString();
  }

  public static DependencyKey fromPortableString(String str) {
    int p, c;

    p = 0;  c = nextColonIndex(str, p);
    String groupId = substring(str, p, c); 

    p = c + 1; c = nextColonIndex(str, p);
    String artifactId = substring(str, p, c); 
    
    p = c + 1; c = nextColonIndex(str, p);
    String type = substring(str, p, c); 

    p = c + 1; c = nextColonIndex(str, p);
    String version = substring(str, p, c);
    
    p = c + 1; c = nextColonIndex(str, p);
    String classifier = substring(str, p, c);
    
    return new DependencyKey(groupId, artifactId, version, classifier, type);
  }
  
  private static String substring(String str, int start, int end) {
    String substring = str.substring(start, end);
    return "".equals(substring)? null: substring; //$NON-NLS-1$
  }

  private static int nextColonIndex(String str, int pos) {
    int idx = str.indexOf(':', pos);
    if (idx < 0) throw new IllegalArgumentException(NLS.bind("Invalid portable string: {0}", str));
    return idx;
  }

  public String toPortableString() {
    StringBuilder sb = new StringBuilder();
    if (groupId != null) sb.append(groupId); sb.append(':');
    if (artifactId != null) sb.append(artifactId); sb.append(':');
    if (type != null) sb.append(type); sb.append(':');
    if (version != null) sb.append(version); sb.append(':');
    if (classifier != null) sb.append(classifier); sb.append(':');
    return sb.toString();
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public String getClassifier() {
    return classifier;
  }

  public String getType() {
	    return type;
  }
  
}
