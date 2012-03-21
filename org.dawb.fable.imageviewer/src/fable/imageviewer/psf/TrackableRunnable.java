package fable.imageviewer.psf;

/**
 * The <code>TrackableRunnable</code> class is <code>Runnable</code> based class.
 * It supports traceability, that is its started and finished state can be
 * gotten.
 * A <code>TrackableRunnable</code> can be started, started and aborted, or just
 * aborted. In all cases it will be finished.
 * </p>
 * 
 * @author  Gabor Naray
 * @version 1.00 20/12/2011
 * @since   20111220
 */
public abstract class TrackableRunnable implements Runnable {
	protected boolean started = false;
	protected boolean aborted = false;
	protected boolean finished = false;
	protected ExecutableManager trackerThread = null;

	public TrackableRunnable(ExecutableManager thread) {
		setTracker(thread);
	}

	public TrackableRunnable() {
		this(null);
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

	public abstract void runThis();

	protected synchronized void waitForFinish() throws InterruptedException {
		if( !isStarted() && isFinished() )
			return;
		setStarted();
		notify();
		wait();
	}

	protected synchronized void abort() {// throws InterruptedException {
		if( isFinished() )
			return;
		setAborted();
	}
	/**
	 * Be sure this method is not overridden, clients must override runThis method.
	 */
	public synchronized void run() {
		if( !isStarted() ) {
			try {
				wait();
				setStarted();
			} catch (InterruptedException e) {
				setFinished();
				return;
			}
		}
		try {
			runThis();
		} finally {
			setFinished();
			notify();
		}
	}

}
