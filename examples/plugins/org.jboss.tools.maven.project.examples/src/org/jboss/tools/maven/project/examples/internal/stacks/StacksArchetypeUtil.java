/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.project.examples.internal.stacks;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.jdf.stacks.model.ArchetypeVersion;
import org.jboss.jdf.stacks.model.Runtime;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.maven.project.examples.MavenProjectExamplesActivator;

/**
 * Utility class to handle Stacks Archetypes
 * 
 * @author Fred Bricon
 *
 * @since 1.5.3
 */
@SuppressWarnings("nls")
public class StacksArchetypeUtil {

	//Target types
	private static final String TARGET_PRODUCT = "product";

	private static final String TARGET_COMMUNITY = "community";

	//Archetype Labels keys
	private static final String ARCHETYPE_IS_BLANK = "isBlank";

	private static final String ARCHETYPE_ENVIRONMENT = "environment";

	private static final String ARCHETYPE_TARGET = "target";

	private static final String ARCHETYPE_TYPE = "type";

	//Environments
	private static final String WEB_EE6 = "web-ee6";

	private static final String FULL_EE6 = "full-ee6";

	private static final String WEB_EE7 = "web-ee7";

	private static final String FULL_EE7 = "full-ee7";

	private static final String UNDEFINED_EE = "undefined";
	
	private static Map<String, List<String>> ENVIRONMENTS_MAP = new HashMap<String, List<String>>(4);
	static {
		ENVIRONMENTS_MAP.put(FULL_EE7, Arrays.asList(FULL_EE7, WEB_EE7, FULL_EE6, WEB_EE6));
		ENVIRONMENTS_MAP.put(WEB_EE7, Arrays.asList(WEB_EE7, WEB_EE6));
		ENVIRONMENTS_MAP.put(FULL_EE6, Arrays.asList(FULL_EE6, WEB_EE6));
		ENVIRONMENTS_MAP.put(WEB_EE6, Arrays.asList(WEB_EE6));
	}
	
	private Set<org.eclipse.wst.common.project.facet.core.runtime.IRuntime> facetedRuntimes;
	
	public StacksArchetypeUtil() {
		this(null);
	}
	
	/**
	 * Not API - For testing purposes only
	 */
	public StacksArchetypeUtil(Set<org.eclipse.wst.common.project.facet.core.runtime.IRuntime> facetedRuntimes) {
		if (facetedRuntimes == null) {
			this.facetedRuntimes = RuntimeManager.getRuntimes();
		} else {
			this.facetedRuntimes = facetedRuntimes; 
		}
	}
	
	private String getEnvironment(IRuntime runtime) {
		org.eclipse.wst.common.project.facet.core.runtime.IRuntime facetedRuntime = getFacetedRuntime(runtime);
		if (facetedRuntime == null) {
			return UNDEFINED_EE;
		}
		if (facetedRuntime.supports(IJ2EEFacetConstants.ENTERPRISE_APPLICATION_70)) {
			return FULL_EE7;
		}
		if (facetedRuntime.supports(IJ2EEFacetConstants.DYNAMIC_WEB_31)) {
			return WEB_EE7;
		}
		if (facetedRuntime.supports(IJ2EEFacetConstants.ENTERPRISE_APPLICATION_60)) {
			return FULL_EE6;
		}
		if (facetedRuntime.supports(IJ2EEFacetConstants.DYNAMIC_WEB_30)) {
			return WEB_EE6;
		}
		return UNDEFINED_EE;
	}
	
	private org.eclipse.wst.common.project.facet.core.runtime.IRuntime getFacetedRuntime(
			IRuntime runtime) {
		if (runtime == null) {
			return null;
		}

		String id = runtime.getId();
		if (id == null)
			return null;
		
		for (org.eclipse.wst.common.project.facet.core.runtime.IRuntime fr : facetedRuntimes) {
			if (id.equals(fr.getProperty("id")))
				return fr;
		}
		return null;
	}

	/**
	 * Looks for the stacks archetype matching best the given requirements (isBlank, runtime)
	 */
	public ArchetypeVersion getArchetype(String archetypeType, boolean isBlank, IRuntime runtime, Stacks stacks) {
		if (archetypeType == null) {
			throw new IllegalArgumentException("Archetype type cannot be null");
		}
		
		String targetProduct = RuntimeUtils.isEAP(runtime)?TARGET_PRODUCT:TARGET_COMMUNITY;
		String environment = getEnvironment(runtime);
		String runtimeTypeId = (runtime == null || runtime.getRuntimeType() == null) ? null : runtime.getRuntimeType().getId();
		return getArchetype(archetypeType, isBlank, targetProduct, environment, runtimeTypeId, stacks);
	}
	

	/**
	 * Looks for the stacks archetype matching best the given requirements (isBlank, targetProduct, environment)
	 */
	public ArchetypeVersion getArchetype(String archetypeType, boolean isBlank, String targetProduct, String environment, Stacks stacks) {
		return getArchetype(archetypeType, isBlank, targetProduct, environment, null, stacks);
	}

	/**
	 * Looks for the stacks archetype matching best the given requirements (isBlank, targetProduct, environment)
	 */
	public ArchetypeVersion getArchetype(String archetypeType, boolean isBlank, String targetProduct, String environment, String serverId, Stacks stacks) {
		if (archetypeType == null) {
			throw new IllegalArgumentException("Archetype type cannot be null");
		}
		
		Map<ArchetypeVersion, Integer> matchingArchetypes = getBestMatchingArchetype(archetypeType, isBlank, targetProduct, environment, serverId, stacks);
		if (!matchingArchetypes.isEmpty()) {
			return matchingArchetypes.keySet().iterator().next();
		}
		return null;
	}

	private Map<ArchetypeVersion, Integer> getBestMatchingArchetype(String archetypeType, boolean isBlank, String targetProduct /*community or product*/, String environment /* *-ee6 or *-ee7 */, String serverId, Stacks stacks) {
		Map<ArchetypeVersion, Integer> matchingArchetypes = new HashMap<ArchetypeVersion, Integer>();
		if (targetProduct == null) {
			targetProduct = TARGET_COMMUNITY;
		}
		for ( ArchetypeVersion archetype : stacks.getAvailableArchetypeVersions()) {
			Properties labels = archetype.getLabels();
			String type = labels.getProperty(ARCHETYPE_TYPE);
			if (!archetypeType.equals(type)) {
				continue;
			}
			int score = 0;
			String archetypeTargetProduct= labels.getProperty(ARCHETYPE_TARGET);
			if (targetProduct.equals(archetypeTargetProduct)) {
				score+=2;
			}
			List<String> supportedEnvironments = ENVIRONMENTS_MAP.get(environment);
			if (supportedEnvironments != null) {
				String tEnv = labels.getProperty(ARCHETYPE_ENVIRONMENT);
				int envScore = supportedEnvironments.size();
				for (String env : supportedEnvironments) {
					if (env.equals(tEnv)) {
						break;
					} else {
						envScore--;
					}
				}
				score += envScore;
			}
			if (isBlank == isBlank(archetype)) {
				score++;
			}			
			matchingArchetypes.put(archetype, score);
		}

		if (serverId != null) {
			Runtime rt = getRuntimeFromWtp(stacks, serverId);
			if (rt != null && rt.getArchetypes() != null) {
				for (ArchetypeVersion a : rt.getArchetypes()) {
					//Archetypes belonging to the selected wtp runtime get an extra bonus
					if (matchingArchetypes.containsKey(a)) {
						matchingArchetypes.put(a, (matchingArchetypes.get(a))+3);
					}
				}
			}
		}
		
		//Yuck!
		List<Entry<ArchetypeVersion, Integer>> entries = new ArrayList<Map.Entry<ArchetypeVersion,Integer>>(matchingArchetypes.entrySet());
		//Sort higher scores first, then higher versions of identical archetypes first
		Collections.sort(entries, new Comparator<Map.Entry<ArchetypeVersion,Integer>>() {
			@Override
			public int compare(Entry<ArchetypeVersion, Integer> e1,
					Entry<ArchetypeVersion, Integer> e2) {
				//Compare archetype score
				int compare = e2.getValue().compareTo(e1.getValue());
				if (compare == 0) {
					//If identical archetypes, compare their version
					if (e1.getKey().getArchetype().equals(e2.getKey().getArchetype())) {
						ComparableVersion v1 = new ComparableVersion(e1.getKey().getVersion());
						ComparableVersion v2 = new ComparableVersion(e2.getKey().getVersion());
						return v2.compareTo(v1);
					}
				}
				return compare;
			}
		});
		Map<ArchetypeVersion, Integer> result = new LinkedHashMap<ArchetypeVersion, Integer>(entries.size());
		for (Entry<ArchetypeVersion, Integer> e: entries) {
			result.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	private Runtime getRuntimeFromWtp(Stacks stacks, String wtpRuntimeId) {
		if (wtpRuntimeId != null) {
			for (Runtime runtime : stacks.getAvailableRuntimes()) {
				Properties p = runtime.getLabels();
				if (p != null && wtpRuntimeId.equals(p.get("wtp-runtime-type"))) { //$NON-NLS-1$
					return runtime;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a map of additional Maven repositories defined on a given stacks archetype.
	 * <ul><li>The key corresponds to the repository id or profile id</li>
	 * <li>The value corresponds to the repository url</li>
	 * </ul>
	 * 
	 */
	public static Map<String,String> getAdditionalRepositories(ArchetypeVersion archetype) {
		if (archetype != null) {
			Object additionalRepos = archetype.getLabels().get("additionalRepositories");
			if (additionalRepos instanceof Map<?,?>) {
				return new LinkedHashMap<String, String>((Map<String, String>)additionalRepos);
			} else if (additionalRepos instanceof Collection<?>) {
				//In case we ca
				Collection<String> additionalReposList = (Collection<String>) additionalRepos;
				Map<String, String> additionalReposMap = new LinkedHashMap<String, String>(additionalReposList.size());
				for(String url : additionalReposList) {
					String id = inferIdFromUrl(url);
					if (id != null) {
						additionalReposMap.put(id, url);
					}
				}
				return additionalReposMap;
			}
			
		}
		return null;
	}
	
	private static String inferIdFromUrl(String sUrl) {
		try {
			URI url = new URI(sUrl);
			String id = url.getHost();
			if (id != null) {
				return (id+url.getPath()).replace('/', ' ').trim().replaceAll("[^a-z\\d]", "-");
			}
		} catch (Exception e) {
			MavenProjectExamplesActivator.log(e);
		}
		return null;
	}


	/**
	 * Returns a set of required Maven dependencies defined on a given stacks archetype.
	 */
	public static Set<String> getRequiredDependencies(ArchetypeVersion archetype) {
		if (archetype != null) {
			Object essentialDeps = archetype.getLabels().get("essentialDependencies");
			if (essentialDeps instanceof Collection<?>) {
				return new LinkedHashSet<String>((Collection<String>) essentialDeps);
			}
		}
		return null;
	}

	public boolean hasBlankArchetype(ArchetypeVersion archetype,
			IRuntime runtime, Stacks stacks) {
		if (archetype == null) {
			return false;
		}
		if (isBlank(archetype)) {
			return true;
		}
		Properties labels = archetype.getLabels();
		String type = labels.getProperty(ARCHETYPE_TYPE);
		if (type == null) {
			return archetype.getArchetype().getBlank() != null || archetype.getArchetype().getArtifactId().contains("-blank");
		}
		ArchetypeVersion candidate = getArchetype(type, true, runtime, stacks);
		
		return isBlank(candidate);
	}

	private boolean isBlank(ArchetypeVersion archetype){
		Properties labels = archetype.getLabels();
		boolean isArchetypeBlank = Boolean.parseBoolean(""+labels.get(ARCHETYPE_IS_BLANK)); 
		return isArchetypeBlank;
	}
	
}
