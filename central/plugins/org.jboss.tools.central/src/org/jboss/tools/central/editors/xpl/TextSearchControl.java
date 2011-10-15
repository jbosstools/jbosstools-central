/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     JBoss by Red Hat
 *******************************************************************************/
package org.jboss.tools.central.editors.xpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylyn.internal.commons.ui.CommonsUiPlugin;
import org.eclipse.mylyn.internal.provisional.commons.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.central.JBossCentralActivator;

/**
 * @author Shawn Minto
 * @author snjeza
 */
public class TextSearchControl extends Composite {

	private static final int MAX_PROPOSAL_ITEMS = 50;

	/**
	 * Image descriptor for enabled clear button.
	 */
	private static final String CLEAR_ICON = "org.eclipse.ui.internal.dialogs.CLEAR_ICON"; //$NON-NLS-1$

	/**
	 * Image descriptor for enabled clear button.
	 */
	private static final String FIND_ICON = "org.eclipse.ui.internal.dialogs.FIND_ICON"; //$NON-NLS-1$

	private static final String SEARCH_SECTION = "searchSection";

	private static final String SEARCH_KEY = "searchKey";

	/* SWT STYLE BIT AVAILABLE ON 3.5 AND HIGHER */
	public static final int ICON_SEARCH = 1 << 9;

	/* SWT STYLE BIT AVAILABLE ON 3.5 AND HIGHER */
	public static final int ICON_CANCEL = 1 << 8;

	private static final String INITIAL_MESSAGE = "initialMessage";

	/**
	 * Get image descriptors for the clear button.
	 */
	static {
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PlatformUI.PLUGIN_ID,
				"$nl$/icons/full/etool16/clear_co.gif"); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(CLEAR_ICON, descriptor);
		}

		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(CommonsUiPlugin.ID_PLUGIN,
				"$nl$/icons/etool16/find.png"); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(FIND_ICON, descriptor);
		}
	}

	private Text textControl;

	private Control clearControl;

	private Control findControl;

	private final Set<SelectionListener> selectionListeners = new HashSet<SelectionListener>();

	private static Boolean useNativeSearchField;
	
	private String initialMessage;

	private String[] proposals;

	@SuppressWarnings("restriction")
	public TextSearchControl(Composite parent, boolean automaticFind) {
		super(parent, getCompositeStyle(automaticFind, parent));
		
		int textStyle = SWT.SINGLE;
		int numColumns = 1;
		if (useNativeSearchField(automaticFind, parent)) {
			if (automaticFind) {
				textStyle |= SWT.SEARCH | ICON_CANCEL;
			} else {
				textStyle |= SWT.SEARCH | ICON_SEARCH | ICON_CANCEL;
			}
		} else {
			super.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			super.setFont(parent.getFont());
			numColumns = 2;
			if (!automaticFind) {
				numColumns += 1;
			}
		}
		GridLayoutFactory.swtDefaults()
				.margins(0, 0)
				.extendedMargins(0, 0, 0, 0)
				.spacing(0, 1)
				.numColumns(numColumns)
				.applyTo(this);

		
		textControl = new Text(this, textStyle);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
	
		textControl.setLayoutData(gridData);

		KeyStroke stroke;
		String proposalKey = null;
		try {
			stroke = KeyStroke.getInstance("M1+SPACE"); //$NON-NLS-1$
			proposalKey = stroke.format();
		} catch (ParseException e1) {
			JBossCentralActivator.log(e1);
		}
		stroke = null;
		final ControlDecoration dec = new ControlDecoration(textControl, SWT.TOP
					| SWT.LEFT);
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		FieldDecoration fd = registry.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		dec.setImage(fd.getImage());
		String description;
		if (proposalKey == null) {
			description = fd.getDescription();
		} else {
			description = fd.getDescription() + "(" + proposalKey + ")";
		}
		dec.setDescriptionText(description);
		
		dec.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(
					FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());

		dec.setShowOnlyOnFocus(true);
		dec.setShowHover(true);

		ContentProposalAdapter adapter = new ContentProposalAdapter(textControl, 
				new TextContentAdapter(), 
				new SearchContentProposalProvider(), 
				null, 
				null);
		adapter.setPropagateKeys(true);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		
		if (useNativeSearchField == null || !useNativeSearchField) {
			findControl = createLabelButtonControl(this,
					JFaceResources.getImageRegistry().getDescriptor(FIND_ICON),
					Messages.TextControl_AccessibleListenerFindButton, Messages.TextControl_FindToolTip, ICON_SEARCH);
			Point size = findControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			GridData data = (GridData) textControl.getLayoutData();
			data.heightHint = size.y;
			clearControl = createLabelButtonControl(this,
					JFaceResources.getImageRegistry().getDescriptor(CLEAR_ICON),
					WorkbenchMessages.FilteredTree_ClearToolTip,//FilteredTree_AccessibleListenerClearButton,
					WorkbenchMessages.FilteredTree_ClearToolTip, ICON_CANCEL);
			addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					updateButtonVisibilityAndEnablement();

				}
			});
			updateButtonVisibilityAndEnablement();
		}

		IDialogSettings section = getSearchSettings();
		proposals = section.getArray(SEARCH_KEY);
		if (proposals == null) {
			proposals = new String[0];
			section.put(SEARCH_KEY, proposals);
		}
		String message = section.get(INITIAL_MESSAGE);
		if (message == null || message.isEmpty()) {
			message = JBossCentralActivator.SEARCH_RED_HAT_CUSTOMER_PORTAL;
		}
		setInitialMessage(message);
		
		registerListeners();
	}

	
	private void registerListeners() {
		addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (textControl != null && !textControl.isDisposed()) {
					addSearchSettings();
				}

			}
		});
		textControl.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				if (textControl != null && !textControl.isDisposed()) {
					// addSearchSettings();
					if (textControl.getText().isEmpty()) {
						setInitialMessage(initialMessage);
					}
					updateButtonVisibilityAndEnablement();
				}
			}

		});
	}

	private static int getCompositeStyle(boolean automaticFind, Composite parent) {
		if (useNativeSearchField(automaticFind, parent)) {
			return SWT.NONE;
		}
		return SWT.BORDER;
	}

	private static boolean useNativeSearchField(boolean automaticFind, Composite parent) {
		if (parent != null) {
			if (useNativeSearchField == null) {
				useNativeSearchField = Boolean.FALSE;
				Text testText = null;
				try {
					int style = SWT.SEARCH | ICON_CANCEL;
					if (automaticFind) {
						style |= ICON_SEARCH;
					}
					testText = new Text(parent, style);
					useNativeSearchField = new Boolean((testText.getStyle() & ICON_CANCEL) != 0
							&& (!automaticFind || (testText.getStyle() & ICON_SEARCH) != 0));
				} finally {
					if (testText != null) {
						testText.dispose();
					}
				}

			}
		} else {
			useNativeSearchField = Boolean.FALSE;
		}
		return useNativeSearchField.booleanValue();
	}

	private Control createLabelButtonControl(Composite parent,
			ImageDescriptor activeImageDescriptor, final String accessibilityText, String toolTipText, final int detail) {

		final Image nativeImage = parent.getDisplay().getSystemImage(detail);

		final Image activeImage = nativeImage != null ? nativeImage : activeImageDescriptor.createImage();
		final Image inactiveImage = new Image(parent.getDisplay(), activeImage, SWT.IMAGE_GRAY);
		final Image pressedImage = inactiveImage;

		final Label labelButton = new Label(parent, SWT.NONE);
		labelButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		labelButton.setImage(inactiveImage);
		labelButton.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		labelButton.setToolTipText(toolTipText);
		labelButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (nativeImage == null && activeImage != null && !activeImage.isDisposed()) {
					activeImage.dispose();
				}
				if (inactiveImage != null && !inactiveImage.isDisposed()) {
					inactiveImage.dispose();
				}
				if (pressedImage != null && !pressedImage.isDisposed()) {
					pressedImage.dispose();
				}
			}
		});
		labelButton.addMouseListener(new MouseAdapter() {
			private MouseMoveListener fMoveListener;

			@Override
			public void mouseDown(MouseEvent e) {
				labelButton.setImage(pressedImage);
				fMoveListener = new MouseMoveListener() {
					private boolean fMouseInButton = true;

					public void mouseMove(MouseEvent e) {
						boolean mouseInButton = isMouseInButton(e);
						if (mouseInButton != fMouseInButton) {
							fMouseInButton = mouseInButton;
							labelButton.setImage(mouseInButton ? pressedImage : inactiveImage);
						}
					}
				};
				labelButton.addMouseMoveListener(fMoveListener);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (fMoveListener != null) {
					labelButton.removeMouseMoveListener(fMoveListener);
					fMoveListener = null;
					boolean mouseInButton = isMouseInButton(e);
					labelButton.setImage(mouseInButton ? activeImage : inactiveImage);
					if (mouseInButton) {
						if (textControl != null && textControl.isEnabled() && textControl.getText().length() > 0) {
							notifySelectionChanged(detail);
						}
					}
				}
			}

			private boolean isMouseInButton(MouseEvent e) {
				Point buttonSize = labelButton.getSize();
				return 0 <= e.x && e.x < buttonSize.x && 0 <= e.y && e.y < buttonSize.y;
			}
		});

		labelButton.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				if (labelButton.getImage() != activeImage) {
					labelButton.setImage(activeImage);
				}
			}

			public void mouseExit(MouseEvent e) {
				if (labelButton.getImage() != inactiveImage) {
					labelButton.setImage(inactiveImage);
				}
			}

			public void mouseHover(MouseEvent e) {
			}
		});

		labelButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = accessibilityText;
			}
		});
		labelButton.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_PUSHBUTTON;
			}
		});
		return labelButton;
	}

	@Override
	public void addTraverseListener(TraverseListener listener) {
		textControl.addTraverseListener(listener);
	}

	@Override
	public void removeTraverseListener(TraverseListener listener) {
		textControl.removeTraverseListener(listener);
	}

	public void addSelectionListener(SelectionListener listener) {
		textControl.addSelectionListener(listener);
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		textControl.removeSelectionListener(listener);
		selectionListeners.remove(listener);
	}

	public void addModifyListener(ModifyListener listener) {
		textControl.addModifyListener(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		textControl.removeModifyListener(listener);
	}

	private void notifySelectionChanged(int detail) {
		if (useNativeSearchField != null && useNativeSearchField) {
			// notification should happen via the widgets selection listener
			return;
		}
		Event event = new Event();
		event.detail = detail;
		event.widget = textControl;
		event.display = textControl.getDisplay();

		SelectionEvent e = new SelectionEvent(event);
		for (Object element : selectionListeners) {
			((SelectionListener) element).widgetDefaultSelected(e);
		}
	}

	private void updateButtonVisibilityAndEnablement() {
		if (textControl != null && !textControl.isDisposed()) {
			boolean hasText = textControl.getText().length() > 0;

			//setFindButtonVisibility(!(hasText && automaticFind));
			setFindButtonVisibility(hasText);
			setClearButtonVisibility(hasText);
		}
	}

	private void setFindButtonVisibility(boolean visible) {
		if (findControl != null && !findControl.isDisposed()) {
			findControl.setVisible(visible);
			if (findControl.getLayoutData() instanceof GridData) {
				((GridData) findControl.getLayoutData()).exclude = !visible;
				findControl.getParent().layout();
			}
		}
	}

	private void setClearButtonVisibility(boolean visible) {
		if (clearControl != null && !clearControl.isDisposed()) {
			clearControl.setVisible(visible);
			if (clearControl.getLayoutData() instanceof GridData) {
				((GridData) clearControl.getLayoutData()).exclude = !visible;
				clearControl.getParent().layout();
			}
		}
	}

	public Text getTextControl() {
		return textControl;
	}

	public String getText() {
		if (textControl != null && !textControl.isDisposed()) {
			return textControl.getText();
		}
		return ""; //$NON-NLS-1$
	}

	public void setText(String text) {
		if (textControl != null && !textControl.isDisposed()) {
			textControl.setText(text);
		}
	}

	@Override
	public void setBackground(Color color) {
		if (useNativeSearchField != null && useNativeSearchField) {
			super.setBackground(color);
		}
	}
	
	public String getInitialMessage() {
		return initialMessage;
	}

	public void setInitialMessage(String initialMessage) {
		this.initialMessage = initialMessage;
		if (textControl != null && !textControl.isDisposed() 
				&& textControl.getText().isEmpty()) {
			textControl.setMessage(initialMessage);
			IDialogSettings section = getSearchSettings();
			section.put(INITIAL_MESSAGE, initialMessage);
		}
	}

	private IDialogSettings getSearchSettings() {
		IDialogSettings settings = JBossCentralActivator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(SEARCH_SECTION);
		if (section == null) {
			section = settings.addNewSection(SEARCH_SECTION);
		}
		return section;
	}
	
	private void addSearchSettings() {
		String text = textControl.getText().trim();
		if (!text.isEmpty()) {
			boolean found = false;
			for (String proposal:proposals) {
				if (text.equals(proposal)) {
					found = true;
					break;
				}
			}
			if (!found) {
				int length = proposals.length;
				if (length > MAX_PROPOSAL_ITEMS) {
					length = MAX_PROPOSAL_ITEMS;
				}
				String[] newProposals = new String[length + 1];
				System.arraycopy(proposals, 0, newProposals, 1, length);
				newProposals[0] = text;
				proposals = newProposals;
				IDialogSettings section = getSearchSettings();
				section.put(SEARCH_KEY, proposals);
			}
		}
	}

	private class SearchContentProposalProvider implements IContentProposalProvider {

		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			if (contents != null) {
				if (position > 0) {
					contents = contents.substring(0, position);
				}
				contents = contents.trim();
			}
			List<IContentProposal> contentProposals = new ArrayList<IContentProposal>();
			for (String proposal:proposals) {
				if (proposal != null && proposal.startsWith(contents)) {
					contentProposals.add(new ContentProposal(proposal));
				}
			}
			return contentProposals.toArray(new IContentProposal[0]);
		}
		
	}

}
