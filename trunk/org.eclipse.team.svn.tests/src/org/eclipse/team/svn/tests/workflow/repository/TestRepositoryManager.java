package org.eclipse.team.svn.tests.workflow.repository;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * This class manages a {@link IRepositoryLocation} for testing purposes.
 * 
 * @author Nicolas Peifer
 */
public interface TestRepositoryManager {

	void createRepository() throws Exception;

	void removeRepository();

	IRepositoryLocation getRepositoryLocation();

}