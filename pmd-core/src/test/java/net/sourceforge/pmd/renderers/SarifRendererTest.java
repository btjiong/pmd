/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.renderers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.ReportTest;
import net.sourceforge.pmd.Rule;

public class SarifRendererTest extends AbstractRendererTest {
    @Override
    public Renderer getRenderer() {
        return new SarifRenderer();
    }

    @Override
    public String getExpected() {
        return readFile("expected.sarif.json");
    }

    @Override
    public String getExpectedEmpty() {
        return readFile("empty.sarif.json");
    }

    @Override
    public String getExpectedMultiple() {
        return readFile("expected-multiple.sarif.json");
    }

    @Override
    public String getExpectedError(Report.ProcessingError error) {
        String expected = readFile("expected-error.sarif.json");
        expected = expected.replace("###REPLACE_ME###", error.getDetail()
                .replaceAll("\r", "\\\\r")
                .replaceAll("\n", "\\\\n")
                .replaceAll("\t", "\\\\t"));
        return expected;
    }

    @Override
    public String getExpectedError(Report.ConfigurationError error) {
        return readFile("expected-configerror.sarif.json");
    }

    @Override
    public String getExpectedErrorWithoutMessage(Report.ProcessingError error) {
        String expected = readFile("expected-error-nomessage.sarif.json");
        expected = expected.replace("###REPLACE_ME###", error.getDetail()
                .replaceAll("\r", "\\\\r")
                .replaceAll("\n", "\\\\n")
                .replaceAll("\t", "\\\\t"));
        return expected;
    }

    @Override
    public String filter(String expected) {
        return expected.replaceAll("\r\n", "\n") // make the test run on Windows, too
                .replaceAll("\"version\": \".+\",", "\"version\": \"unknown\",");
    }

    /**
     * Multiple occurrences of the same rule should be reported as individual results.
     * 
     * @see <a href="https://github.com/pmd/pmd/issues/3768"> [core] SARIF formatter reports multiple locations
     *      when it should report multiple results #3768</a>
     */
    @Test
    public void testRendererMultipleLocations() throws Exception {
        Report rep = reportThreeViolationsTwoRules();
        String actual = ReportTest.render(getRenderer(), rep);

        JSONObject json = new JSONObject(actual);
        JSONArray results = json.getJSONArray("runs").getJSONObject(0).getJSONArray("results");
        assertEquals(3, results.length());
        assertEquals(filter(readFile("expected-multiple-locations.sarif.json")), filter(actual));
    }

    private Report reportThreeViolationsTwoRules() {
        Rule fooRule = createFooRule();
        Rule booRule = createBooRule();

        Report report = new Report();
        report.addRuleViolation(newRuleViolation(1, 1, 1, 10, fooRule));
        report.addRuleViolation(newRuleViolation(5, 1, 5, 11, fooRule));
        report.addRuleViolation(newRuleViolation(2, 2, 3, 1, booRule));
        return report;
    }

    private String readFile(String name) {
        try (InputStream in = SarifRendererTest.class.getResourceAsStream("sarif/" + name)) {
            String asd = IOUtils.toString(in, StandardCharsets.UTF_8);
            return asd;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
