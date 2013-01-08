/*******************************************************************************
 * Copyright (C) 2010, 2012 Mathias Kinzler <mathias.kinzler@sap.com> and others.
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
import org.eclipse.egit.ui.internal.dialogs.CompareTargetSelectionDialog;
import org.eclipse.egit.ui.internal.dialogs.CompareTreeView;
import org.eclipse.jface.window.Window;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * The "compare with ref" action. This action opens a diff editor comparing the
 * file as found in the working directory and the version in the selected ref.
 */
public class BeyondCompareWithRefActionHandler extends BeyondCompareRepositoryActionHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Repository repo = getRepository(true, event);
		// assert all resources map to the same repository
		if (repo == null)
			return null;
		final IResource[] resources = getSelectedResources(event);

		CompareTargetSelectionDialog dlg = new CompareTargetSelectionDialog(
				getShell(event), repo, resources.length == 1 ? resources[0]
						.getFullPath().lastSegment() : null);
		if (dlg.open() == Window.OK) {

			if (resources.length == 1 && resources[0] instanceof IFile) {
				final IFile baseFile = (IFile) resources[0];
				
				try {
					RepositoryMapping mapping = RepositoryMapping.getMapping(resources[0]);
					String repoRelativeBasePath = mapping.getRepoRelativePath(baseFile);
					String refName = dlg.getRefName();
					ObjectId commitId = repo.resolve(refName);

					Repository localRepo = mapping.getRepository();
					RevWalk rw = new RevWalk(localRepo);
					RevCommit commit = rw.parseCommit(commitId);
					rw.release();

					String rightFilePath = BeyondCompareUtil.getCompareFilePath(repoRelativeBasePath, commit, localRepo);
					String leftFilePath = baseFile.getLocation().toFile().getAbsolutePath();
					BeyondCompareUtil.execBeyondCompare(leftFilePath, rightFilePath);

				} catch (IOException e) {
					Activator.handleError(UIText.CompareWithIndexActionHandler_onError, e, true);
					return null;
				}
				
			} else {
				CompareTreeView view;
				try {
					view = (CompareTreeView) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.showView(CompareTreeView.ID);
					view.setInput(resources, dlg.getRefName());
				} catch (PartInitException e) {
					Activator.handleError(e.getMessage(), e, true);
				}
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return getRepository() != null;
	}
}
