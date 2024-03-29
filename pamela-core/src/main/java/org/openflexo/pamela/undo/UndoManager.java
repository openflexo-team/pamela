/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.pamela.undo;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A PAMELA {@link UndoManager} tracks and record PAMELA atomic edits into aggregates named compound edit.<br>
 * It provides a way to undo or redo the appropriate edits.<br>
 * 
 * To instantiate and enable an {@link UndoManager}, use the {@link PamelaModelFactory#createUndoManager()} method.<br>
 * 
 * {@link UndoManager} internally manages a list of {@link CompoundEdit}, which are aggregates of PAMELA atomic events ({@link AtomicEdit}).
 * <br>
 * You should use {{@link #startRecording(String)} and {{@link #stopRecording(CompoundEdit)} methods to semantically control undo/redo
 * sequences.<br>
 * 
 * The {@link UndoManager} is automatically tracking all PAMELA atomic events (see {@link AtomicEdit}). If an {@link AtomicEdit} is received
 * outside a declared recording edit, then a new edition (a {@link CompoundEdit}) is automatically instantiated. No inconsistency should be
 * raised because of unregistered edits.<br>
 * 
 * {@link UndoManager} maintains an ordered list of edits and the index of the next edit in that list. The index of the next edit is either
 * the size of the current list of edits, or if {@link UndoManager#undo()} has been invoked it corresponds to the index of the last
 * significant edit that was undone. When {@link UndoManager#undo()} is invoked all edits from the index of the next edit to the last
 * significant edit are undone, in reverse order. <br>
 * 
 * Invoking {@link UndoManager#redo()} results in invoking {@link UndoManager#redo()} on all edits between the index of the next edit and
 * the next significant edit (or the end of the list). <br>
 * 
 * Adding an edit to an <code>UndoManager</code> results in removing all edits from the index of the next edit to the end of the list.<br>
 * 
 * TODO: WARNING: if you are dealing with custom implementation of PAMELA objects with methods involved in setter/adder/remover and declared
 * as final, those calls won't be intercepted by the UndoManager, and thus results will be of undetermined state. Please fix this.<br>
 * 
 * This class is thread safe.
 * 
 * @author sylvain
 */
@SuppressWarnings("serial")
public class UndoManager extends javax.swing.undo.UndoManager implements HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(UndoManager.class.getPackage().getName());

	public static final String ENABLED = "enabled";
	public static final String START_RECORDING = "startRecording";
	public static final String STOP_RECORDING = "stopRecording";
	public static final String UNDONE = "undone";
	public static final String REDONE = "redone";
	public static final String UNIDENTIFIED_RECORDING = "<Unidentified recording>";

	private CompoundEdit currentEdition = null;
	private boolean undoInProgress = false;
	private boolean redoInProgress = false;

	private final PropertyChangeSupport pcSupport;

	private boolean enabled = true;

	public UndoManager() {
		pcSupport = new PropertyChangeSupport(this);
	}

	private static final String ANTICIPATED_RECORDING = "AnticipatedRecording";
	private static boolean allowsAnticipatedRecording = false;
	private static CompoundEdit anticipatedRecording;

	/**
	 * Called to start registering a CompoundEdit in advance (when some edition actions occurs outside an "official" recording)<br>
	 * but need to be aggregated in the next edition action
	 * 
	 * @return
	 */
	private synchronized CompoundEdit startAsAnticipatedRecording() {
		if (!isBeeingRecording()) {
			anticipatedRecording = startRecording(ANTICIPATED_RECORDING);
			getPropertyChangeSupport().firePropertyChange(ENABLED, null, currentEdition);

		}
		return null;
	}

	/**
	 * This method should be called whenever some edit might be triggered outside of an official edition action, but if those edits<br>
	 * should be aggregated in the next edition action.<br>
	 * Calling this method enable this scheme. Next received edits will be stored temporarily in this anticipated recording, which will be
	 * turned into next official edition action.
	 */
	public synchronized void enableAnticipatedRecording() {
		allowsAnticipatedRecording = true;
	}

	/**
	 * Disable 'anticipated' recording, see above
	 */
	public synchronized void disableAnticipatedRecording() {
		allowsAnticipatedRecording = false;
	}

	/**
	 * Start a new labelled edit tracking<br>
	 * All PAMELA atomic events that will be received from now will be aggregated into newly created {@link CompoundEdit}
	 * 
	 * @param presentationName
	 * @return the newly created {@link CompoundEdit}
	 */
	public synchronized CompoundEdit startRecording(String presentationName) {
		if (!enabled) {
			return null;
		}

		if (currentEdition != null && currentEdition == anticipatedRecording) {
			currentEdition.setPresentationName(presentationName);
			anticipatedRecording = null;
		}

		else {

			if (currentEdition != null) {
				logger.warning("[PLEASE TRACK ME] : UndoManager exception: already recording " + currentEdition.getPresentationName());
				if (currentEdition.getPresentationName().equals(UNIDENTIFIED_RECORDING)) {
					currentEdition.setPresentationName(presentationName);
				}
				// (new Exception("UndoManager exception: already recording " + currentEdition.getPresentationName())).printStackTrace();
				// SGU: before, we were stopping currentEdition and start a new one
				// Now, we aggregate the both compound edits
				// stopRecording(currentEdition);
			}
			else {
				currentEdition = makeCompoundEdit(presentationName);
			}
			addEdit(currentEdition);

		}

		getPropertyChangeSupport().firePropertyChange(START_RECORDING, null, currentEdition);

		return currentEdition;
	}

	protected CompoundEdit makeCompoundEdit(String presentationName) {
		return new CompoundEdit(presentationName);
	}

	/**
	 * Stops supplied edit tracking<br>
	 * After this method, this edit will be available for undo/redo
	 * 
	 * @param edit
	 * @return
	 */
	public synchronized CompoundEdit stopRecording(CompoundEdit edit) {

		if (!enabled) {
			return null;
		}
		if (currentEdition == null) {
			logger.warning("UndoManager exception: was not recording");
			// (new Exception("UndoManager exception: was not recording")).printStackTrace();
			return null;
		}
		else if (currentEdition != edit) {
			logger.warning("UndoManager exception: was not recording this edit");
			// (new Exception("UndoManager exception: was not recording this edit")).printStackTrace();
			return null;
		}

		currentEdition.end();
		// Thread.dumpStack();
		// System.out.println("----------------> Stop recording " + currentEdition.getPresentationName());
		// System.out.println(currentEdition.describe());
		currentEdition = null;

		getPropertyChangeSupport().firePropertyChange(STOP_RECORDING, null, edit);

		fireAddEdit(edit);

		return currentEdition;
	}

	/**
	 * Return flag indicating if {@link UndoManager} is currently recording.<br>
	 * Return true if start recording has been called
	 * 
	 * @return
	 */
	public synchronized boolean isBeeingRecording() {
		return currentEdition != null;
	}

	/**
	 * Return flag indicating if {@link UndoManager} is currently recording in anticipated mode
	 * 
	 * @return
	 */
	public synchronized boolean isAnticipatedRecording() {
		return (currentEdition != null && currentEdition == anticipatedRecording);
	}

	/**
	 * Returns true if edits may be undone.
	 * 
	 * @return true if there are edits to be undone
	 * @see CompoundEdit#canUndo
	 * @see #editToBeUndone
	 */
	@Override
	public synchronized boolean canUndo() {
		if (!enabled) {
			// System.out.println("1 - Cannot undo because not enabled");
			return false;
		}

		if (isBeeingRecording() && !isAnticipatedRecording()) {
			// System.out.println("2 - Cannot undo because currentEdition = " + currentEdition + " asynchronousRecording="
			// + anticipatedRecording);
			return false;
		}
		if (undoInProgress || redoInProgress) {
			// System.out.println("3 - Cannot undo because undoInProgress or redoInProgress");
			return false;
		}

		if (isAnticipatedRecording()) {
			return true;
		}

		boolean returned = super.canUndo();
		if (!returned) {
			// System.out.println("4 - Cannot undo because of super implementation");
			// debug();
		}
		return returned;
	}

	/**
	 * Returns true if edits may be undone.<br>
	 * If en edition is in progress, return true if stopping this edition will cause UndoManager to be able to undo
	 * 
	 * 
	 * @return true if there are edits to be undone
	 * @see CompoundEdit#canUndo
	 * @see #editToBeUndone
	 */
	public synchronized boolean canUndoIfStoppingCurrentEdition() {
		if (!enabled) {
			// System.out.println("2.1 - Cannot undo because not enabled");
			return false;
		}
		if (undoInProgress || redoInProgress) {
			// System.out.println("2.3 - Cannot undo because undoInProgress or redoInProgress");
			return false;
		}
		boolean returned = super.canUndo();
		if (!returned) {
			// System.out.println("2.4 - Cannot undo because of super implementation");
		}
		return returned;
	}

	/**
	 * Returns true if edits may be redone.
	 * 
	 * @return true if there are edits to be redone
	 * @see CompoundEdit#canRedo
	 * @see #editToBeRedone
	 */
	@Override
	public synchronized boolean canRedo() {
		if (!enabled) {
			return false;
		}
		if (currentEdition != null) {
			return false;
		}
		if (undoInProgress || redoInProgress) {
			return false;
		}
		return super.canRedo();
	}

	/**
	 * Adds an <code>UndoableEdit</code> to this {@link UndoManager}, if it's possible.
	 */
	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {

		// System.out.println("UndoManager: RECEIVED " + anEdit);

		if (!enabled) {
			return false;
		}

		if (anEdit instanceof AtomicEdit) {

			// If UNDO is in progress, ignore it
			if (undoInProgress) {
				// System.out.println("Ignoring " + anEdit.getPresentationName() + " because UNDO in progress");
				anEdit.die();
				return false;
			}
			// Ignore it as well when REDO in progress
			if (redoInProgress) {
				// System.out.println("Ignoring " + anEdit.getPresentationName() + " because REDO in progress");
				anEdit.die();
				return false;
			}
			// If edit is not significant, don't go further
			if (!anEdit.isSignificant()) {
				anEdit.die();
				return false;
			}
			// If somebody has decided that this edit should be ignored, we should check it now
			if (isIgnorable(anEdit)) {
				return false;
			}
			// This is an atomic edit, therefore, i should agglomerate it in current edition
			if (currentEdition == null) {
				if (allowsAnticipatedRecording) {
					startAsAnticipatedRecording();
				}
				else {
					System.err.println("[PLEASE TRACK ME] : PAMELA edit received outside official recording. Create a default one !!!");
					startRecording(UNIDENTIFIED_RECORDING);
				}
			}
			// System.out.println("[PAMELA] Register in UndoManager: " + anEdit.getPresentationName());
			currentEdition.addEdit(anEdit);
			return true;
		}
		else if (anEdit instanceof CompoundEdit) {
			if (undoInProgress) {
				(new Exception("UndoManager exception: received CompoundEdit while UNDO in progress")).printStackTrace();
				return false;
			}
			if (redoInProgress) {
				(new Exception("UndoManager exception: received CompoundEdit while REDO in progress")).printStackTrace();
				return false;
			}
			boolean returned = super.addEdit(anEdit);
			fireAddEdit(anEdit);
			return returned;
		}
		else {
			System.out.println("???? Unexpected Edit " + anEdit);
			return false;
		}
	}

	protected void fireAddEdit(UndoableEdit anEdit) {
		getPropertyChangeSupport().firePropertyChange("edits", null, anEdit);
	}

	/**
	 * This should be used as a hook to intercept some edit that we dont't want to be caught during capture of UNDO edits<br>
	 * This method should be overriden when required.<br>
	 * Default implementation return false
	 * 
	 * @param edit
	 * @return
	 */
	public boolean isIgnorable(UndoableEdit edit) {
		return false;
	}

	/**
	 * Returns the the next significant edit to be undone if <code>undo</code> is invoked. This returns <code>null</code> if there are no
	 * edits to be undone.
	 * 
	 * @return the next significant edit to be undone
	 */
	@Override
	public CompoundEdit editToBeUndone() {

		return (CompoundEdit) super.editToBeUndone();
	}

	/**
	 * Returns the the next significant edit to be redone if <code>redo</code> is invoked. This returns <code>null</code> if there are no
	 * edits to be redone.
	 * 
	 * @return the next significant edit to be redone
	 */
	@Override
	public CompoundEdit editToBeRedone() {
		return (CompoundEdit) super.editToBeRedone();
	}

	/**
	 * Undoes the appropriate edits.<br>
	 * Invokes <code>undo</code> on all edits between the index of the next edit and the last significant edit, updating the index of the
	 * next edit appropriately.
	 * 
	 * @throws CannotUndoException
	 *             if one of the edits throws <code>CannotUndoException</code> or there are no edits to be undone
	 * @see CompoundEdit#end
	 * @see #canUndo
	 * @see #editToBeUndone
	 */
	@Override
	public synchronized void undo() throws CannotUndoException {

		if (!enabled) {
			return;
		}

		if (isAnticipatedRecording()) {
			stopRecording(anticipatedRecording);
			undoInProgress = true;
			super.undo();
			undoInProgress = false;
		}

		try {
			// System.out.println("Will UNDO " + editToBeUndone().getPresentationName());
			// System.out.println(editToBeUndone().describe());
			// System.out.println("START UNDO " + editToBeUndone().getPresentationName());
			undoInProgress = true;
			super.undo();
			undoInProgress = false;
			fireUndo();
			// System.out.println("END UNDO ");
		} catch (Exception e) {
			e.printStackTrace();
			undoInProgress = false;
			discardAllEdits();
			throw new CannotUndoException();
		}
	}

	protected void fireUndo() {
		getPropertyChangeSupport().firePropertyChange(UNDONE, null, this);
	}

	/**
	 * Redoes the appropriate edits.<br>
	 * Invokes <code>redo</code> on all edits between the index of the next edit and the next significant edit, updating the index of the
	 * next edit appropriately.
	 * 
	 * @throws CannotRedoException
	 *             if one of the edits throws <code>CannotRedoException</code> or there are no edits to be redone
	 * @see CompoundEdit#end
	 * @see #canRedo
	 * @see #editToBeRedone
	 */
	@Override
	public synchronized void redo() throws CannotUndoException {
		if (!enabled) {
			return;
		}
		try {
			// System.out.println("Will REDO " + editToBeRedone().getPresentationName());
			// System.out.println(editToBeUndone().describe());
			// System.out.println("START REDO " + editToBeRedone().getPresentationName());
			redoInProgress = true;
			super.redo();
			redoInProgress = false;

			if (editToBeRedone() != null && editToBeRedone() == anticipatedRecording) {
				redoInProgress = true;
				super.redo();
				redoInProgress = false;
				// TODO: it should be nice to also "un-stop" anticipated recording which was stopped because of undo requiring
			}

			fireRedo();
			// System.out.println("END REDO ");
		} catch (Exception e) {
			e.printStackTrace();
			redoInProgress = false;
			discardAllEdits();
			throw new CannotRedoException();
		}
	}

	protected void fireRedo() {
		getPropertyChangeSupport().firePropertyChange(REDONE, null, this);
	}

	/**
	 * Return current edition
	 */
	public CompoundEdit getCurrentEdition() {
		return currentEdition;
	}

	public List<UndoableEdit> getEdits() {
		return edits;
	}

	// Debug
	public void debug() {
		System.out.println("Debugging UNDO manager");
		if (currentEdition != null) {
			System.out.println("UndoManager, currently recording " + currentEdition.getPresentationName());
		}
		else {
			System.out.println("UndoManager, currently NOT recording ");
		}
		System.out.println("Edits=" + edits.size());
		for (UndoableEdit e : edits) {
			if (e instanceof CompoundEdit) {
				System.out.println(e.getPresentationName() + " with " + ((CompoundEdit) e).getEdits().size() + " atomic edits");
			}
		}

	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	/**
	 * Returns a description of the undoable form of this edit.
	 * 
	 * @return a description of the undoable form of this edit
	 */
	@Override
	public synchronized String getUndoPresentationName() {

		if (isAnticipatedRecording()) {
			int i = edits.size();
			while (i > 0) {
				UndoableEdit edit = edits.elementAt(--i);
				if (edit.isSignificant() && edit != anticipatedRecording) {
					return edit.getUndoPresentationName();
				}
			}

			return null;
		}

		if (currentEdition != null) {
			return currentEdition.getPresentationName();
		}
		return super.getUndoPresentationName();
	}

	/**
	 * Return boolean indicating if undo is currently being processed
	 */
	public boolean isUndoInProgress() {
		return undoInProgress;
	}

	/**
	 * Return boolean indicating if redo is currently being processed
	 */
	public boolean isRedoInProgress() {
		return redoInProgress;
	}

	public int getUndoLevel() {
		return getLimit();
	}

	public void setUndoLevel(int undoLevel) {
		setLimit(undoLevel);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		getPropertyChangeSupport().firePropertyChange(ENABLED, !enabled, enabled);
	}

}
