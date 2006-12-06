package org.eclipse.compare.patch;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.patch.PatchWizard;
import org.eclipse.compare.internal.patch.PatchWizardDialog;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;

/**
 * An operation that provides an interface to the Apply Patch Wizard. Users specify
 * the input in terms of an <code>IStorage</code> (note: input must be in unified diff
 * format), an <code>IResource</code> target to apply the patch to and can provide <code>CompareConfiguration</code>
 * elements to supply the label and images used on the preview page and hunk merge page. Finally, the
 * user can also supply a title and image to override the default ones provided by the Apply Patch Wizard.
 * Note that the Apply Patch Wizard does not require any particular set of inputs, and in the absence of
 * any user supplied values, it will work in default mode.
 * 
 * @since 3.3
 *
 */
public class ApplyPatchOperation implements Runnable {

	private IWorkbenchPart part;
	
	/**
	 * Used for the Preview Patch page.
	 */
	private CompareConfiguration configuration;
	
	/**
	 * The patch to use as an input into the Apply Patch wizard
	 */
	private IStorage patch;
	
	/**
	 * Specific <code>IResource</code> target to patch.
	 */
	private IResource target;
	
	/**
	 * An optional image for the patch wizard
	 */
	private ImageDescriptor patchWizardImage;
	
	
	/**
	 * An optional title for the patchWizard
	 */
	private String patchWizardTitle;
	
	/**
	 * Creates a new ApplyPatchOperation with the supplied compare configuration, patch and target.
	 * The behaviour of the Apply Patch wizard is controlled by the number of parameters supplied:
	 * <ul>
	 * <li>If a patch is supplied, the initial input page is skipped. If a patch is not supplied the wizard
	 * will open on the input page.</li>
	 * <li>If the patch is a workspace patch, the target selection page is skipped and the preview page is 
	 * displayed.</li>
	 * <li>If the patch is not a workspace patch and the target is specified, the target page is still
	 * shown with the target selected.</li>
	 * </ul> 
	 * 
	 * @param part 	an IWorkbenchPart 
	 * @param patch		an IStorage containing a patch in unified diff format or <code>null</code>
	 * @param target	an IResource which the patch is to be applied to or <code>null</code>
	 * @param configuration	a CompareConfiguration supplying the labels and images for the preview patch page
	 */
	public ApplyPatchOperation(IWorkbenchPart part, IStorage patch, IResource target, CompareConfiguration configuration) {
		Assert.isNotNull(part);
		Assert.isNotNull(configuration);
		this.part = part;
		this.patch = patch;
		this.target = target;
		this.configuration = configuration;
	}
	
	/**
	 * Create an operation for the given part and resource. This method is a convenience
	 * method that calls {@link #ApplyPatchOperation(IWorkbenchPart, IStorage, IResource, CompareConfiguration)}
	 * with appropriate defaults for the other parameters.
	 * @param targetPart an IResource which the patch is to be applied to or <code>null</code>
	 * @param resource an IResource which the patch is to be applied to or <code>null</code>
	 * @see #ApplyPatchOperation(IWorkbenchPart, IStorage, IResource, CompareConfiguration)
	 */
	public ApplyPatchOperation(IWorkbenchPart targetPart, IResource resource) {
		this(targetPart, null, resource, new CompareConfiguration());
	}

	/**
	 * Open the Apply Patch wizard using the values associated with this operation.
	 * This method must be called from the UI thread.
	 */
	public void openWizard() {
		
		saveAllEditors();
		
		PatchWizard wizard = new PatchWizard(patch, target, configuration);
		if (patchWizardImage != null)
			wizard.setDefaultPageImageDescriptor(patchWizardImage);
		if (patchWizardTitle != null)
			wizard.setWindowTitle(patchWizardTitle);
		wizard.setNeedsProgressMonitor(true);
		
		PatchWizardDialog dialog = new PatchWizardDialog(part.getSite().getShell(), wizard);
		wizard.setDialog(dialog);
		dialog.open();
	}
	
	/**
	 * This method will save all dirty editors. It will prompt the user if the Compare preference to save
	 * dirty editors before viewing a patch is <code>false</code>. Clients can use this or provide their own
	 * implementation.
	 */
	protected void saveAllEditors(){
		IDE.saveAllEditors(new IResource[]{ResourcesPlugin.getWorkspace().getRoot()}, !ComparePreferencePage.getSaveAllEditors());
	}
	
	/**
	 * Sets the title of the patch wizard. Needs to be set before {@link #openWizard()} is called.
	 * @param title	a string to display in the title bar
	 */
	public void setPatchWizardTitle(String title){
		this.patchWizardTitle = title;
	}
	
	/**
	 * Sets the image descriptor to use in the patch wizard. Needs to be set before  {@link #openWizard()} is called.
	 * @param descriptor an image descriptor
	 */
	public void setPatchWizardImageDescriptor(ImageDescriptor descriptor){
		this.patchWizardImage = descriptor;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		openWizard();
	}
	
}
