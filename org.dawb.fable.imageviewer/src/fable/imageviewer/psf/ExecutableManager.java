package fable.imageviewer.psf;

/**
 * The <code>RunnableTracker</code> class is <code>Thread</code> based class.
 * It can receive, store and run <code>TrackableRunnable</code> objects. These
 * <code>Runnable</code> objects are considered as doing similar things, thus
 * if there are more at any given moment, then only the last one is executed and
 * the earlier gotten objects are dropped.
 * This behavior is ideal for example when the user drags a Scale, of which
 * ModifyListener slowly draws an image, that is the user can drag the Scale
 * faster than the event handler draws the image. In this case we do not need
 * every image drawn, only the last one at any given moment.
 * <p>
 * </p>
 *
 * @author  Gabor Naray
 * @version 1.00 20/12/2011
 * @since   20111220
 */
import java.util.Vector;

import org.eclipse.ui.PlatformUI;

public class ExecutableManager extends Thread {
	protected Vector<TrackableRunnable> runnableQueue = new Vector<TrackableRunnable>();
	protected Vector<TrackableJob> jobQueue = new Vector<TrackableJob>();
	Boolean canReceiveRequest = true;
	TrackableRunnable currentRun = null;
	TrackableJob currentJob = null;
	boolean abortCurrentRequest = false;

	public ExecutableManager(ThreadGroup group, String name, long stackSize) {
		super(group, null, name, stackSize);
	}

	public ExecutableManager(ThreadGroup group, String name) {
		this(group, name, 0);
	}

	public ExecutableManager(String name) {
		this(null, name, 0);
	}

	public ExecutableManager(ThreadGroup group) {
		super(group, (Runnable)null);
	}

	public ExecutableManager() {
		this((ThreadGroup)null);
	}

	protected void setAbortCurrentRequest() {
		abortCurrentRequest = true;
	}

	protected void clearAbortCurrentRequest() {
		abortCurrentRequest = false;
	}

	protected boolean isAbortCurrentRequest() {
		return abortCurrentRequest;
	}

	@Override
	public void interrupt() {
		setAbortCurrentRequest();
		super.interrupt();
	}

	public void run() {
		do {
			synchronized( canReceiveRequest ) {
				if( runnableQueue.size() == 0 && jobQueue.size() == 0 ) {
					canReceiveRequest = false;
					break;
				}
			}
			try {
				if( runnableQueue.size() > 0 ) {
					currentRun = runnableQueue.lastElement();
					runnableQueue.clear();
					PlatformUI.getWorkbench().getDisplay().asyncExec(currentRun);
					currentRun.waitForFinish();
					currentRun = null;
				} else {
					currentJob = jobQueue.lastElement();
					jobQueue.clear();
					currentJob.schedule();
					currentJob.waitForFinish();
					currentJob = null;
				}
				if( isAbortCurrentRequest() )
					clearAbortCurrentRequest();
			} catch (InterruptedException e) {
				synchronized( canReceiveRequest ) {
					canReceiveRequest = false;
				}
				runnableQueue.clear();
				jobQueue.clear();
				break;
			}
		} while( true );
	}
	
	public boolean addRequestThis(TrackableRunnable run) {
		synchronized( canReceiveRequest ) {
			if( canReceiveRequest ) {
				runnableQueue.add(run);
				return true;
			} else
				return false;
		}
	}

	public boolean addRequestThis(TrackableJob job) {
		synchronized( canReceiveRequest ) {
			if( canReceiveRequest ) {
				jobQueue.add(job);
				return true;
			} else
				return false;
		}
	}

	public boolean setRequestThis(TrackableRunnable run) {
		synchronized( canReceiveRequest ) {
			if( canReceiveRequest ) {
				runnableQueue.clear();
				jobQueue.clear();
				runnableQueue.add(run);
				setAbortCurrentRequest();
				return true;
			} else
				return false;
		}
	}

	public boolean setRequestThis(TrackableJob job) {
		synchronized( canReceiveRequest ) {
			if( canReceiveRequest ) {
				runnableQueue.clear();
				jobQueue.clear();
				jobQueue.add(job);
				setAbortCurrentRequest();
				return true;
			} else
				return false;
		}
	}

	public static ExecutableManager addRequest(TrackableRunnable run) {
		return run.addThisToTracker();
	}

	public static ExecutableManager addRequest(TrackableJob job) {
		return job.addThisToTracker();
	}

	public static ExecutableManager setRequest(TrackableRunnable run) {
		return run.setThisInTracker();
	}

	public static ExecutableManager setRequest(TrackableJob job) {
		return job.setThisInTracker();
	}

	public static ExecutableManager addRequest(ExecutableManager tracker, TrackableRunnable run) {
		run.setTracker(tracker);
		return ExecutableManager.addRequest(run);
	}

	public static ExecutableManager addRequest(ExecutableManager tracker, TrackableJob job) {
		job.setTracker(tracker);
		return ExecutableManager.addRequest(job);
	}

	public static ExecutableManager setRequest(ExecutableManager tracker, TrackableRunnable job) {
		job.setTracker(tracker);
		return ExecutableManager.setRequest(job);
	}

	public static ExecutableManager setRequest(ExecutableManager tracker, TrackableJob job) {
		job.setTracker(tracker);
		return ExecutableManager.setRequest(job);
	}

}
