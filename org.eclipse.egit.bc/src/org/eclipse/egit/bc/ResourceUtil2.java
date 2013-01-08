package org.eclipse.egit.bc;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class ResourceUtil2 {
	
	public static IResource getResourceForLocation(IPath location) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFileForLocation(location);
		if (file != null && file.exists())
			return file;
		IContainer container = root.getContainerForLocation(location);
		if (container != null && container.exists())
			return container;
		return null;
	}
	
}
