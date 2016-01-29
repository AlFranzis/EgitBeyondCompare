package org.eclipse.egit.bc;

import java.lang.reflect.Method;

import org.eclipse.jgit.revwalk.RevWalk;

/**
 * This class provides a utility method for closing/releasing instances
 * of {@link RevWalk} that is independent of the name of the method.
 * <p>
 * The {@code release} method was renamed to {@code close} in the version
 * of EGit that ships with Mars (Eclipse 4.5).
 */
public class RevWalkUtil {
	private static Method closeMethod = null;
	
	// Query which version of RevWalk were using.  Determines 
	// if we call close() or release().
	static {
		Method[] methods = RevWalk.class.getMethods();
		for (Method method : methods) {
			if ("close".equals(method.getName())) {
				closeMethod = method;
			} else if ("release".equals(method.getName())) {
				closeMethod = method;
			}
		}
	}
	
	public static void close(RevWalk rw) {
		try {
			closeMethod.invoke(rw, (Object[])null);
		} catch (Exception e) {
			// close/release doesn't throw a checked exception, so 
			// just rethrow a RuntimeException
			if (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			}
			throw new RuntimeException();
		}
	}
}
