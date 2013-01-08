/*******************************************************************************
 * Copyright (C) 2011, Mathias Kinzler <mathias.kinzler@sap.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.egit.bc;


/**
 * The "compare with ref" action. This action opens a Ref selection dialog to
 * select a branch or tag and then a compare editor comparing the version.
 */
public class BeyondCompareWithCommitAction extends BeyondCompareRepositoryAction {
	/**
	 *
	 */
	public BeyondCompareWithCommitAction() {
		super(BeyondCompareActionCommands.COMPARE_WITH_COMMIT_ACTION,
				new BeyondCompareWithCommitActionHandler());
	}
}
