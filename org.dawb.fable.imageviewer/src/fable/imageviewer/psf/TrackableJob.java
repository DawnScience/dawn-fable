package fable.imageviewer.psf;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The <code>TrackableJob</code> class is <code>Job</code> based class.
 * It supports traceability, that is its started and finished state can be
 * gotten.
 * <p>
 * A <code>TrackableJob</code> can be started, started and aborted, or just
 * aborted. In all cases it will be finished.
 * </p>
 * 
 * @author  Gabor Naray
 * @version 1.00 20/12/2011
 * @since   20111220
 */
public abstract class TrackableJob extends Job {
	protected boolean started = false;
	protected boolean aborted = false;
	protected boolean finished = false;
	protected ExecutableManager trackerThread = null;

	public TrackableJob(ExecutableManager thread, String name) {
		super(name);
		setTracker(thread);
	}

	public TrackableJob(String name) {
		this(null, name);
	}

	public TrackableJob(ExecutableManager thread) {
		this(thread, "Noname");
	}

	public TrackableJob() {
		this((ExecutableManager)null);
	}

	protected void setStarted() {
		started = true;
	}

	protected boolean isStarted() {
		return started;
	}

	protected void setAborted() {
		aborted = true;
	}

	protected boolean isAborted() {
		return aborted;
	}

	protected boolean isAborting() {
		return trackerThread.isAbortCurrentRequest();
	}

	protected void setFinished() {
		finished = true;
	}

	protected boolean isFinished() {
		return finished;
	}

	protected ExecutableManager addThisToTracker() {
		if( trackerThread == null || !trackerThread.addRequestThis(this) ) {
			trackerThread = new ExecutableManager();
			trackerThread.addRequestThis(this);
			trackerThread.start();
		}
		return trackerThread;
	}

	protected ExecutableManager setThisInTracker() {
		if( trackerThread == null || !trackerThread.setRequestThis(this) ) {
			return addThisToTracker();
		}
		return trackerThread;
	}

	protected ExecutableManager getTracker() {
		return trackerThread;
	}

	protected void setTracker(ExecutableManager thread) {
		if( trackerThread == thread )
			return;
		trackerThread = thread;
	}

	public abstract IStatus runThis(IProgressMonitor monitor);

	protected synchronized void waitForFinish() throws InterruptedException {
		if( !isStarted() && isFinished() ) //Finished without started
			return;
		setStarted();
		notify();
		wait();
	}
/*
	protected synchronized void abort() {// throws InterruptedException {
		if( isFinished() )
			return;
		setAborted();
	}
*/
	/**
	 * Be sure this method is not overridden, clients must override runThis method.
	 */
	@Override
	protected synchronized IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		if( !isStarted() ) {
			try {
				wait();
				setStarted();
			} catch (InterruptedException e) {
				setFinished();
				return Status.CANCEL_STATUS;
			}
		}
		try {
			status = runThis(monitor);
		} finally {
			setFinished();
			notify();
		}
		return status;
	}

}
