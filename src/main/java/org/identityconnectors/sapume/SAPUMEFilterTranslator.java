/**
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description :
 * Source code : SAPUMEFilterTranslator.java
 * Author :Chellappan.Sampath
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.openspml.message.Filter;
import org.openspml.message.FilterTerm;

public class SAPUMEFilterTranslator extends AbstractFilterTranslator<Object>
		implements SAPUMEConstants {

	private static final Log LOG = Log.getLog(SAPUMEFilterTranslator.class);
	private final ObjectClass objectClass;
	private final SAPUMEConnection connection;
	private final SAPUMEConfiguration configuration;

	/**
	 * Constructor
	 * 
	 * @param objClass
	 *            Object class to handle
	 * @param connection
	 *            {@link org.identityconnectors.sapume.SAPUMEConnection} to be
	 *            used
	 * @param configuration
	 *            {@link org.identityconnectors.sapume.SAPUMEConfiguration} to
	 *            be used
	 */
	public SAPUMEFilterTranslator(ObjectClass objClass,
			SAPUMEConnection connection, SAPUMEConfiguration configuration) {
		this.objectClass = objClass;
		this.connection = connection;
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Filter createEqualsExpression(EqualsFilter filter, boolean not) {
		LOG.info("BEGIN");
		Filter oFilter = new Filter();
		FilterTerm oSub1FilterTerm = new FilterTerm();
		oSub1FilterTerm.setOperation(FilterTerm.OP_EQUAL);
		String name = filter.getName();
		// To search/get user details from target, we have to set the filter
		// name
		// as 'logonname' if object class is '__ACCOUNT__' or '__UID'.
		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			if (Uid.NAME.equals(name)) {
				oSub1FilterTerm.setName(ID);
			} else if (Name.NAME.equals(name)) {
				oSub1FilterTerm.setName(LOGONNAME);
			} else {
				oSub1FilterTerm.setName(name);
			}
		} // if we deal with group opertaion
		else if ((objectClass.is(ObjectClass.GROUP_NAME) || objectClass
				.is(OBJCLSGROUP))
				&& ((Uid.NAME.equals(name) || Name.NAME.equals(name)))) {
			oSub1FilterTerm.setName(ID);
		} // if we deal with role opertaion
		else if ((objectClass.is(OBJCLSROLE))
				&& ((Uid.NAME.equals(name) || Name.NAME.equals(name)))) {
			oSub1FilterTerm.setName(ID);
		} else {
			oSub1FilterTerm.setName(name);
		}
		oSub1FilterTerm.setValue(filter.getAttribute().getValue().get(0));
		oFilter.addTerm(oSub1FilterTerm);
		LOG.info("RETURN");
		return oFilter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Filter createContainsExpression(ContainsFilter filter, boolean not) {
		LOG.info("BEGIN");
		Filter oFilter = new Filter();

		List<String> alSubstring = new ArrayList<String>();
		alSubstring.add((String) filter.getAttribute().getValue().get(0));

		FilterTerm oSub2FilterTerm = new FilterTerm();
		oSub2FilterTerm.setOperation(FilterTerm.OP_SUBSTRINGS);
		oSub2FilterTerm.setName(filter.getName());
		oSub2FilterTerm.setSubstrings(alSubstring);
		oFilter.addTerm(oSub2FilterTerm);
		LOG.info("RETURN");
		return oFilter;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Filter createStartsWithExpression(StartsWithFilter filter,
			boolean not) {
		LOG.info("BEGIN");
		Filter oFilter = new Filter();
		FilterTerm oSub2FilterTerm = new FilterTerm();
		oSub2FilterTerm.setOperation(FilterTerm.OP_SUBSTRINGS);
		oSub2FilterTerm.setName(filter.getName());
		oSub2FilterTerm.setInitialSubstring((String) filter.getAttribute()
				.getValue().get(0));
		oFilter.addTerm(oSub2FilterTerm);
		LOG.info("RETURN");
		return oFilter;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Filter createEndsWithExpression(EndsWithFilter filter, boolean not) {

		LOG.info("BEGIN");
		Filter oFilter = new Filter();
		FilterTerm oSub2FilterTerm = new FilterTerm();
		oSub2FilterTerm.setOperation(FilterTerm.OP_SUBSTRINGS);
		oSub2FilterTerm.setName(filter.getName());
		oSub2FilterTerm.setFinalSubstring((String) filter.getAttribute()
				.getValue().get(0));
		oFilter.addTerm(oSub2FilterTerm);
		LOG.info("RETURN");
		return oFilter;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Filter createContainsAllValuesExpression(
			ContainsAllValuesFilter filter, boolean not) {

		LOG.info("BEGIN");
		Filter oFilter = new Filter();

		List<Object> values = filter.getAttribute().getValue();
		List alSubstring = new ArrayList();
		for (Object value : values) {
			alSubstring.add((String) value);
		}
		FilterTerm oSub2FilterTerm = new FilterTerm();
		oSub2FilterTerm.setOperation(FilterTerm.OP_EQUAL);
		oSub2FilterTerm.setName(filter.getName());
		oSub2FilterTerm.setValues(alSubstring);
		oFilter.addTerm(oSub2FilterTerm);
		LOG.info("RETURN");
		return oFilter;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Filter createAndExpression(final Object leftExpressionFilter,
			final Object rightExpressionFilter) {
		LOG.info("BEGIN");
		Filter filter = new Filter();
		FilterTerm filterTerm = new FilterTerm();
		filterTerm.setOperation(FilterTerm.OP_AND);
		
		List<FilterTerm> lstFilterTerms = ((Filter)leftExpressionFilter).getTerms();
		if (lstFilterTerms.get(0).getOperation().equalsIgnoreCase("and")) {
			List<FilterTerm> lstFilter = lstFilterTerms.get(0).getOperands();
			lstFilter.add((FilterTerm) ((Filter) rightExpressionFilter).getTerms().get(0));
			for (FilterTerm term : lstFilter) {
				filterTerm.addOperand(term);
			}
		} else {
			filterTerm.addOperand((FilterTerm) ((Filter) leftExpressionFilter).getTerms()
					.get(0));
			filterTerm.addOperand((FilterTerm) ((Filter) rightExpressionFilter).getTerms()
					.get(0));
		}

		filter.addTerm(filterTerm);
		LOG.info("RETURN");
		return filter;
	}

	@Override
	protected Filter createOrExpression(final Object leftExpressionFilter,
			final Object rightExpressionFilter) {

		LOG.info("BEGIN");
		Filter filter = new Filter();
		FilterTerm filterTerm = new FilterTerm();
		filterTerm.setOperation(FilterTerm.OP_OR);

		List<FilterTerm> lstFilterTerms = ((Filter) leftExpressionFilter).getTerms();
		if (lstFilterTerms.get(0).getOperation().equalsIgnoreCase("or")) {
			List<FilterTerm> lstFilter = lstFilterTerms.get(0).getOperands();
			lstFilter.add((FilterTerm) ((Filter) rightExpressionFilter).getTerms().get(0));
			for (FilterTerm term : lstFilter) {
				filterTerm.addOperand(term);
			}
		} else {
			filterTerm.addOperand((FilterTerm) ((Filter) leftExpressionFilter).getTerms()
					.get(0));
			filterTerm.addOperand((FilterTerm) ((Filter) rightExpressionFilter).getTerms()
					.get(0));
		}

		filter.addTerm(filterTerm);
		LOG.info("RETURN");
		return filter;
	}

}
