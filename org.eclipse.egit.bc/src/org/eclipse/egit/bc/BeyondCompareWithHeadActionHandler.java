/*******************************************************************************
 * Copyright (C) 2009, Stefan Lay <stefan.lay@sap.com>
 * Copyright (C) 2012, Robin Stocker <robin@nibor.org>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.egit.bc;

import java.io.IOException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.egit.ui.internal.dialogs.CompareTreeView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Compares the working tree content of a file with the version of the file in
 * the HEAD commit.
 */
public class BeyondCompareWithHeadActionHandler extends BeyondCompareRepositoryActionHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Repository repository = getRepository(true, event);
		// assert all resources map to the same repository
		if (repository == null)
			return null;
		final IResource[] resources = getSelectedResources(event);

		if (resources.length == 1 && resources[0] instanceof IFile) {
			final IFile baseFile = (IFile) resources[0];

			try {
				RepositoryMapping mapping = RepositoryMapping.getMapping(baseFile.getProject());
				String repoRelativeBasePath = mapping.getRepoRelativePath(baseFile);
				Repository localRepo = mapping.getRepository();
				RevCommit commit = getHeadRevision(repository, repoRelativeBasePath);

				String rightFilePath = BeyondCompareUtil.getCompareFilePath(repoRelativeBasePath, commit, localRepo);
				String leftFilePath = baseFile.getLocation().toFile().getAbsolutePath();
				BeyondCompareUtil.execBeyondCompare(leftFilePath, rightFilePath);
			} catch (Exception e) {
				Activator.handleError(UIText.CompareWithHeadActionHandler_onError, e, true);
			}
			return null;

		} else {
			CompareTreeView view;
			try {
				view = (CompareTreeView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CompareTreeView.ID);
				try {
					Ref head = repository.getRef(Constants.HEAD);
					if (head == null || head.getObjectId() == null) {
						// Initial commit case
						Shell shell = HandlerUtil.getActiveShell(event);
						MessageDialog.openInformation(shell,
								UIText.CompareWithHeadActionHandler_NoHeadTitle,
								UIText.CompareWithHeadActionHandler_NoHeadMessage);
					} else
						view.setInput(resources, Repository.shortenRefName(head.getTarget().getName()));
				} catch (IOException e) {
					Activator.handleError(e.getMessage(), e, true);
					return null;
				}
			} catch (PartInitException e) {
				Activator.handleError(e.getMessage(), e, true);
				return null;
			}
			return null;
		}
	}

	@Override
	public boolean isEnabled() {
		return getRepository() != null;
	}

	private static RevCommit getHeadRevision(Repository repository, String repoRelativePath) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		Ref head = repository.getRef(Constants.HEAD);
		if (head == null || head.getObjectId() == null)
			// Initial import, not yet a HEAD commit
			return null;

		RevCommit latestFileCommit;
		RevWalk rw = new RevWalk(repository);
		try {
			RevCommit headCommit = rw.parseCommit(head.getObjectId());
			rw.markStart(headCommit);
			rw.setTreeFilter(AndTreeFilter.create(
					PathFilter.create(repoRelativePath),
					TreeFilter.ANY_DIFF));
			latestFileCommit = rw.next();
			// Fall back to HEAD
			if (latestFileCommit == null)
				latestFileCommit = headCommit;
		} finally {
			rw.release();
		}

		return latestFileCommit;

	}

}
