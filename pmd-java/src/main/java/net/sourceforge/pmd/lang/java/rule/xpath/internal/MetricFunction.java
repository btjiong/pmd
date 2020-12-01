/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.xpath.internal;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.internal.JavaLanguageHandler.JavaMetricsProvider;
import net.sourceforge.pmd.lang.metrics.Metric;
import net.sourceforge.pmd.lang.metrics.MetricOptions;
import net.sourceforge.pmd.lang.rule.xpath.internal.AstElementNode;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.SequenceType;


/**
 * Implements the {@code metric()} XPath function. Takes the
 * string name of a metric and the context node and returns
 * the result if the metric can be computed, otherwise returns
 * {@link Double#NaN}.
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
public final class MetricFunction extends BaseJavaXPathFunction {

    public static final MetricFunction INSTANCE = new MetricFunction();

    private static final JavaMetricsProvider METRICS = new JavaMetricsProvider();

    private MetricFunction() {
        super("metric");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] {SequenceType.SINGLE_STRING};
    }


    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_DECIMAL;
    }


    @Override
    public boolean dependsOnFocus() {
        return true;
    }


    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {

            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                Node contextNode = ((AstElementNode) context.getContextItem()).getUnderlyingNode();
                String metricKey = arguments[0].head().getStringValue();

                return new BigDecimalValue(getMetric(contextNode, metricKey));
            }
        };
    }


    static String badMetricKeyMessage(String constantName) {
        return String.format("'%s' is not the name of a metric", constantName);
    }


    static String genericBadNodeMessage() {
        return "Incorrect node type: the 'metric' function cannot be applied";
    }

    private static double getMetric(Node n, String metricKeyName) throws XPathException {
        Metric<?, ?> metric = METRICS.getMetricWithName(metricKeyName);
        if (metric == null) {
            throw new XPathException(badMetricKeyMessage(metricKeyName));
        }

        Number computed = Metric.compute(metric, MetricOptions.emptyOptions(), n);
        return computed == null ? Double.NaN : computed.doubleValue();
    }

}
