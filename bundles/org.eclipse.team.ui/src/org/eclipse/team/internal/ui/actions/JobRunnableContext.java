/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IWorkbenchSite;

/**
 * This runnable context executes it's operation in the context of a background job.
 */
public class JobRunnableContext implements ITeamRunnableContext {

	private IJobChangeListener listener;
	private IWorkbenchSite site;
	private String jobName;
	private ISchedulingRule schedulingRule;
	private boolean postponeBuild;
	
	/*
	 * Interface that provides access to the runnable of the job so
	 * that subclasses can do belongsTo family checking.
	 */
	public interface IContextJob {
	    IRunnableWithProgress getRunnable();
	}
	
	/*
	 * Class that provides a basic job (i.e. no resource specific interactions)
	 */
	private class BasicJob extends Job implements IContextJob {
        private final IRunnableWithProgress runnable;
        public BasicJob(String name, IRunnableWithProgress runnable) {
            super(name);
            this.runnable = runnable;
        }
        public IStatus run(IProgressMonitor monitor) {
			return JobRunnableContext.this.run(runnable, monitor);
		}
		public boolean belongsTo(Object family) {
		    return JobRunnableContext.this.belongsTo(this, family);
		}
        public IRunnableWithProgress getRunnable() {
            return runnable;
        }
	}
	
	/*
	 * Class that provides a resource job (i.e. resource specific interactions)
	 */
	private class ResourceJob extends WorkspaceJob implements IContextJob {
        private final IRunnableWithProgress runnable;
        public ResourceJob(String name, IRunnableWithProgress runnable) {
            super(name);
            this.runnable = runnable;
        }
        public IStatus runInWorkspace(IProgressMonitor monitor) {
			return JobRunnableContext.this.run(runnable, monitor);
		}
		public boolean belongsTo(Object family) {
		    return JobRunnableContext.this.belongsTo(this, family);
		}
        public IRunnableWithProgress getRunnable() {
            return runnable;
        }
	}
	public JobRunnableContext(String jobName, IJobChangeListener listener, IWorkbenchSite site) {
		this.jobName = jobName;
		this.listener = listener;
		this.site = site;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITeamRunnableContext#run(java.lang.String, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(IRunnableWithProgress runnable) {
		Job job;
		if (schedulingRule == null && !postponeBuild) {
			job = new BasicJob(jobName, runnable);
		} else {
			job = new ResourceJob(jobName, runnable);
		}
		if (listener != null) {
			job.addJobChangeListener(listener);
		}
		configureJob(job);
		Utils.schedule(job, site);
	}
	
	/**
	 * Configure the job. By default, the job is configured to be a user
	 * job meaning that it will make use of the progress service.
	 * Subclasses can tailor how the job appears in the progress service.
	 * @param job the job that will provide the execution context
	 */
	protected void configureJob(Job job) {
		if (schedulingRule != null) {
			job.setRule(schedulingRule);
		}
	    job.setUser(isUser());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.ITeamRunnableContext#getShell()
	 */
	public Shell getShell() {
		final Shell[] newShell = new Shell[] { null };
		Display.getDefault().syncExec(
			new Runnable() {
				public void run() {
					newShell[0] = Utils.getShell(site);
				}
			});
		return newShell[0];
	}
	
	/**
	 * Returns whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 * @return whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 */
	public boolean isPostponeBuild() {
		return postponeBuild;
	}
	
	/**
	 * Set whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 * @param postponeBuild whether to postpone the auto-build.
	 */
	public void setPostponeBuild(boolean postponeBuild) {
		this.postponeBuild = postponeBuild;
	}
	
	/**
	 * Return the scheduling rule that will be obtained before the context
	 * executes a runnable or <code>null</code> if no scheduling rule is to be onbtained.
	 * @return the schedulingRule to be obtained or <code>null</code>.
	 */
	public ISchedulingRule getSchedulingRule() {
		return schedulingRule;
	}
	
	/**
	 * Return whether this job context is user initiated. Subclasses may override.
	 * @param boolean <code>true</code> if the job is a result of a user initiated actions
	 * and <code>false</code> otherwise.
	 */
	protected boolean isUser() {
		return true;
	}
	
	/**
	 * Set the scheduling rule that will be obtained before the context
	 * executes a runnable or <code>null</code> if no scheduling rule is to be onbtained.
	 * @param schedulingRule The schedulingRule to be obtained or <code>null</code>.
	 */
	public void setSchedulingRule(ISchedulingRule schedulingRule) {
		this.schedulingRule = schedulingRule;
	}
	
	/* private */ IStatus run(IRunnableWithProgress runnable, IProgressMonitor monitor) {
		try {
			runnable.run(monitor);
		} catch (InvocationTargetException e) {
			return TeamException.asTeamException(e).getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return getCompletionStatus();
	}
    
	/**
	 * Return the completions status for the job.
	 * By default, <code>Status.OK_STATUS</code>
	 * is returned.
     * @return the completions status for the job
     */
    protected IStatus getCompletionStatus() {
        return Status.OK_STATUS;
    }

    /**
	 * Return whether the job for this context is in the given family.
	 * By default, <code>false</code> is returned. Subclasses may override.
     * @param family the job family being queried
     */
    protected boolean belongsTo(IContextJob job, Object family) {
        return false;
    }

}
