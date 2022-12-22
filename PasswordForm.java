/*
 *
 *  * Copyright (c) 2014- MHISoft LLC and/or its affiliates. All rights reserved.
 *  * Licensed to MHISoft LLC under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. MHISoft LLC licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.mhisoft.wallet.view;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.*;
import javax.swing.text.*;



import org.mhisoft.wallet.SystemSettings;
import org.mhisoft.wallet.action.ActionResult;
import org.mhisoft.wallet.action.CreateWalletAction;
import org.mhisoft.wallet.action.LoadWalletAction;
import org.mhisoft.wallet.action.VerifyPasswordAction;
import org.mhisoft.wallet.model.PassCombinationEncryptionAdaptor;
import org.mhisoft.wallet.model.PassCombinationVO;
import org.mhisoft.wallet.model.PasswordValidator;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.BeanType;
import org.mhisoft.wallet.service.ServiceRegistry;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
@SuppressWarnings("rawtypes")
public class PasswordForm implements ActionListener {
	private JPanel mainPanel;
	private JPasswordField fldPassword;
	private JButton btnCancel;
	private JButton btnOk;
	private JLabel labelPassword;
	private JLabel labelSafeCombination;
	private JLabel labelInst1;
	private JLabel labelInst2;
	private JLabel labelInst3;
	private JLabel labelMsg;
	private JButton button1;
	private JComboBox<Item> comboBox1;
	private JComboBox<Item> comboBox2;
	private JComboBox<Item> comboBox3;
	private JTextField textField1;
	private JTextField textField2;
	private JTextField textField3;
	private MyFilter myFilter1;
	private MyFilter myFilter2;
	private MyFilter myFilter3;
	JDialog dialog;

	String title;

	WalletForm walletForm;

	List<Component> componentsList = new ArrayList<>();


	PasswordValidator passwordValidator = ServiceRegistry.instance.getService(BeanType.singleton, PasswordValidator.class);

	transient Integer spinner1Integer, spinner2Integer, spinner3Integer;


	public PasswordForm(String title) {
		passwordValidator = new PasswordValidator();
		this.title = title;
		init(); 

	}


	class Item {
		Integer value;
		String text;

		public Item(Integer value, String text) {
			this.value = value;
			this.text = text;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return getText();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Item item = (Item) o;
			return Objects.equals(value, item.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}


	/* place custom component creation code here*/
	private void createUIComponents() {
		PlainDocument doc1 = (PlainDocument) textField1.getDocument();
		PlainDocument doc2 = (PlainDocument) textField2.getDocument();
		PlainDocument doc3 = (PlainDocument) textField3.getDocument();

		myFilter1 = new MyFilter(textField1);
		myFilter2 = new MyFilter(textField2);
		myFilter3 = new MyFilter(textField3);

		doc1.setDocumentFilter(myFilter1);
		doc2.setDocumentFilter(myFilter2);
		doc3.setDocumentFilter(myFilter3);

		textField1.setText("*");
		textField2.setText("*");
		textField3.setText("*");

		textField1.addFocusListener(new MyFocusAdapter(textField1));
		textField2.addFocusListener(new MyFocusAdapter(textField2));
		textField3.addFocusListener(new MyFocusAdapter(textField3));

		textField1.addMouseWheelListener(new MyMouseWheelListener(textField1));
		textField2.addMouseWheelListener(new MyMouseWheelListener(textField2));
		textField3.addMouseWheelListener(new MyMouseWheelListener(textField3));

		spinner1Integer = 0;
		spinner2Integer = 0;
		spinner3Integer = 0;
	}


	private class IndexedFocusTraversalPolicy extends
			FocusTraversalPolicy {

		private ArrayList<Component> components =
				new ArrayList<Component>();

		public void addIndexedComponent(Component component) {
			components.add(component);
		}

		@Override
		public Component getComponentAfter(Container aContainer,
				Component aComponent) {
			int atIndex = components.indexOf(aComponent);
			int nextIndex = (atIndex + 1) % components.size();
			return components.get(nextIndex);
		}

		@Override
		public Component getComponentBefore(Container aContainer,
				Component aComponent) {
			int atIndex = components.indexOf(aComponent);
			int nextIndex = (atIndex + components.size() - 1) %
					components.size();
			return components.get(nextIndex);
		}

		@Override
		public Component getFirstComponent(Container aContainer) {
			return components.get(0);
		}

		@Override
		public Component getLastComponent(Container aContainer) {
			return components.get(components.size());
		}

		@Override
		public Component getDefaultComponent(Container aContainer) {
			return textField1;
		}
	}


	public interface Callback {
		void setResult(ActionResult result);

	}

	public static abstract class PasswordFormActionListener implements ActionListener {
		private Callback callback;

		public PasswordFormActionListener(Callback callback) {
			this.callback = callback;
		}

	}

	/**
	 *
	 */
	public static class PasswordFormCancelActionListener extends PasswordFormActionListener {
		PasswordForm passwordForm;

		public PasswordFormCancelActionListener(Callback callback, PasswordForm passwordForm) {
			super(callback);
			this.passwordForm = passwordForm;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			passwordForm.exitPasswordForm();
		}
	}


	public PasswordFormCancelActionListener defaultCancelListener = new PasswordFormCancelActionListener(null, this);

	/**
	 * @param walletForm
	 * @param okListener optional action listener. if not provided, the one in this class will be used.
	 */
	public void showPasswordForm(WalletForm walletForm, PasswordFormActionListener okListener, PasswordFormActionListener cancelListener) {
		this.walletForm = walletForm;
		dialog = new JDialog(walletForm.frame, this.title != null ? this.title : "Please enter password", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().add(mainPanel);
		dialog.setPreferredSize(new Dimension(800, 400));

		dialog.getRootPane().setDefaultButton(btnOk);


		dialog.pack();


		// Put client property
		fldPassword.putClientProperty("JPasswordField.cutCopyAllowed", true);
		if (title == null)
			labelMsg.setText("Creating New Wallet");
		else {
			labelMsg.setText(title);
		}


		if (okListener != null)
			btnOk.addActionListener(okListener);
		else {
			btnOk.addActionListener(this);
		}

		if (cancelListener != null)
			btnCancel.addActionListener(cancelListener);
		else {
			btnCancel.addActionListener(defaultCancelListener);
		}


		dialog.setLocationRelativeTo(walletForm.frame);
		dialog.setVisible(true);


		textField1.requestFocusInWindow();

	}


	public void exitPasswordForm() {
		dialog.dispose();
	}


	public String getUserInputPass() {
		return new String(fldPassword.getPassword());
	}


	public String getCombinationDisplay() {
		return spinner1Integer + "-" + spinner2Integer + "-" + spinner3Integer;
	}


	private void init() {
		createUIComponents();
	}

	static class MyFilter extends DocumentFilter {
		JTextField textField;
		String regex;

		public MyFilter(JTextField textField) {
			this.textField = textField;
			this.regex = "[0-9|*]";
		}

		public void setRegexNumber() {
			regex = "[0-9]";
		}

		public void setRegexStar() {
			regex = "[*]";
		}

		@Override
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			if (string.matches(regex)) {
				remove(fb, offset, fb.getDocument().getLength());
				super.insertString(fb, offset, string, attr);
				textField.setCaretPosition(0);
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {
			insertString(fb, offset, string, attr);
		}

		@Override
		public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb,offset,length);
		}
	}

	private static class MyMouseWheelListener implements MouseWheelListener {

		JTextField textField;
		MyMouseWheelListener(JTextField textField) {
			this.textField = textField;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int number = Integer.parseInt(textField.getText());
			int notches =e.getWheelRotation();
			number -= notches;

			if (number > 9) {
				number = 0;
			} else if (number < 0) {
				number = 9;
			}

			textField.setText(Integer.toString(number));
		}
	}


	//set the user entered pass and combination to the PassCombinationVO
	public PassCombinationVO getUserEnteredPassForVerification() {
		Integer safeValue1, safeValue2 , safeValue3;

		if (!SystemSettings.isDevMode) {
			 safeValue1 = spinner1Integer;
			 safeValue2 = spinner2Integer;
			 safeValue3 = spinner3Integer;

			if (safeValue1.equals(safeValue2) && safeValue2.equals(safeValue3)) {
				DialogUtils.getInstance().info("Cant' use the same numbers for the safe combination.");
				return null;
			}

			if (!passwordValidator.validate( String.valueOf(fldPassword.getPassword()))) {
				DialogUtils.getInstance().info("Please use a password following the above rules.");
				return null;
			}
		}
		else {
			//dev mode
			safeValue1 =1;
			safeValue2 = 2;
			safeValue3 = 3;
		}


		PassCombinationVO passVO = new PassCombinationEncryptionAdaptor();
		WalletModel model = ServiceRegistry.instance.getWalletModel();
		//set the raw data only, do not add logic here. or later we can't get the raw pass
		passVO.setCombination(safeValue1.toString(), safeValue2.toString(), safeValue3.toString());
		if (SystemSettings.isDevMode) {
			passVO.setPass("Test123!");
		} else {
			passVO.setPass(String.valueOf(fldPassword.getPassword()));
		}

		model.setPassVO(passVO);
		return model.getUserEnteredPassForVerification();

	}

	/**
	 * Default ok button listener
	 *
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		boolean createHash = ServiceRegistry.instance.getWalletModel().getPassHash() == null;
		PassCombinationVO passVO = getUserEnteredPassForVerification();

		if (passVO == null) {
			//user input is not good. try again.
		} else {
			if (createHash) {
				//user password is no good, did not pass validation.
				CreateWalletAction createWalletAction = ServiceRegistry.instance.getService(BeanType.prototype, CreateWalletAction.class);
				createWalletAction.execute(passVO, this);
			} else {
				VerifyPasswordAction verifyPasswordAction = ServiceRegistry.instance.getService(BeanType.prototype, VerifyPasswordAction.class);
				ActionResult result = verifyPasswordAction.execute(passVO,
						ServiceRegistry.instance.getWalletModel().getPassHash(),
						ServiceRegistry.instance.getWalletModel().getCombinationHash()
				);
				if (result.isSuccess()) {
					//close the password form
					exitPasswordForm();

					//load the wallet
					LoadWalletAction loadWalletAction = ServiceRegistry.instance.getService(BeanType.prototype, LoadWalletAction.class);
					loadWalletAction.execute(passVO);
				}
			}
		}
	}

	/**
	 * class for capture fcocus related actions on the safe combination controls
	 * in order to mask the control.
	 */
	class MyFocusAdapter extends FocusAdapter {

		JTextField ctl;

		public MyFocusAdapter(JTextField ctl) {
			this.ctl = ctl;
		}

		public void focusLost(FocusEvent e) {
			if (textField1 == ctl) {
				spinner1Integer = Integer.parseInt(ctl.getText());
				myFilter1.setRegexStar();
				textField1.setText("*");
			}
			if (textField2 == ctl) {
				spinner2Integer = Integer.parseInt(ctl.getText());
				myFilter2.setRegexStar();
				textField2.setText("*");
			}
			if (textField3 == ctl) {
				spinner3Integer = Integer.parseInt(ctl.getText());
				myFilter3.setRegexStar();
				textField3.setText("*");
			}

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ctl.setText("*"); // position zero is reserved with "*"
				}
			});
		}

		/*
		The focus listener events
		on focus, we need to reselect the value saved in the spinner1 to 3. 
		*/
		public void focusGained(FocusEvent e) {

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {


					if (textField1 == ctl && spinner1Integer != null) {
						myFilter1.setRegexNumber();
						textField1.setText(spinner1Integer.toString());
					} else if (textField2 == ctl && spinner2Integer != null) {
						myFilter2.setRegexNumber();
						textField2.setText(spinner2Integer.toString());
					} else if (textField3 == ctl && spinner3Integer != null) {
						myFilter3.setRegexNumber();
						textField3.setText(spinner3Integer.toString());
					}

				}
			});


		}
	}
}
