package com.dashboard.budget.DAO;

import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.dashboard.budget.Util;

public class TableRow extends PageElement {

	private Label lblTransactionDate;
	private Integer valTransactionDateFormat;
	private Label lblTransactionAmount;
	private Label lblTransactionDescription;
	private Label lblTransactionCategory;

	public TableRow(String name, By locDate, Integer dateFormat, By locAmount, By locDescription, By locCategory,
			SearchContext searchContext) {
		super(name, null, searchContext);

		lblTransactionDate = new Label("transaction date", locDate, searchContext);
		valTransactionDateFormat = dateFormat;
		lblTransactionAmount = new Label("transaction amount", locAmount, searchContext);
		lblTransactionDescription = new Label("transaction description", locDescription, searchContext);
		lblTransactionCategory = new Label("transaction category", locCategory, searchContext);

	}

	public Date getDate() throws PageElementNotFoundException {
		return Util.convertStringToDateByType(lblTransactionDate.getText(), valTransactionDateFormat);
	}

	public Double getAmount() throws PageElementNotFoundException {
		return Util.convertStringAmountToDouble(lblTransactionAmount.getText());
	}

	public String getDescription() throws PageElementNotFoundException {
		return lblTransactionDescription.getText().trim().replace("\n", "-");
	}

	public String getCategory() throws PageElementNotFoundException {
		if (lblTransactionCategory.getLocator() == null)
			return "";
		else
			return lblTransactionCategory.getText();
	}

	@Override
	public String toString() {
		return "Table row [name=" + name + ", locator=" + locator + ", webElement=" + webElement + "]";
	}

}
