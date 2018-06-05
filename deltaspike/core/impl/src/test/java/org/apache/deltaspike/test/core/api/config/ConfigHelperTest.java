package org.apache.deltaspike.test.core.api.config;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mark Struberg
 */
public class ConfigHelperTest {

    @Test
    public void testDiffConfig() {
        ConfigResolver.ConfigHelper cfgHelper = ConfigResolver.getConfigProvider().getHelper();

        Map<String, String> oldVal = new HashMap<>();
        Map<String, String> newVal = new HashMap<>();

        oldVal.put("a", "1");

        newVal.put("b", "2");
        newVal.put("a", "1");

        assertAll(cfgHelper.diffConfig(null, newVal), "a", "b");
        assertAll(cfgHelper.diffConfig(oldVal, null), "a");
        assertAll(cfgHelper.diffConfig(oldVal, newVal), "b");
        assertAll(cfgHelper.diffConfig(oldVal, oldVal));
        assertAll(cfgHelper.diffConfig(newVal, newVal));

        newVal.put("a", "5");
        assertAll(cfgHelper.diffConfig(oldVal, newVal), "a", "b");

    }

    private void assertAll(Set<String> actualVals, String... expectedVals) {
        Assert.assertNotNull(actualVals);
        Assert.assertEquals(expectedVals.length, actualVals.size());

        for (String expectedVal : expectedVals) {
            Assert.assertTrue(actualVals.contains(expectedVal));
        }
    }

}
