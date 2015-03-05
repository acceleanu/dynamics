package alexh;

import alexh.weak.Dynamic;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.InputSource;
import java.io.StringReader;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DynamicXmlTest {

    static {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
    }

    static final String XML_CONTENT_2_GUTS =
        "<string_element>This is a 'String'</string_element>" +
        "<multi_element>123</multi_element>" +
        "<multi_element>3214</multi_element>" +
        "<attribute_element is_that_right=\"true\">555</attribute_element>" +
        "<attr_clasher age=\"old\">" +
        "  <age>young</age>" +
        "</attr_clasher>";

    static final String XML_CONTENT_2 =
        "<content_2>"+ XML_CONTENT_2_GUTS +"</content_2>";

    static final String XML =
        "<xml>" +
        "  <content_1>" +
        "    <empty_element/>" +
        "    <int_element>12345</int_element>" +
        "    <large_decimal>12341234123412341234.334234</large_decimal>" +
        "    <multi_empty_element/>" +
        "    <multi_empty_element/>" +
        "    <multi_empty_element/>" +
        "    <just_attrs hello=\"world\" foo=\"bar\" />" +
        "  </content_1>" + XML_CONTENT_2 +
        "</xml>";

    Dynamic root = Dynamic.fromXml(XML);

    @Test
    public void rootIsPresent() {
        assertTrue(root.isPresent());
    }

    @Test
    public void immediateChildrenPresence() {
        assertTrue(root.get("xml.content_1", ".").isPresent());
        assertFalse(root.get("xml.foo", ".").isPresent());
    }

    @Test
    public void allChildrenCouldBeMultiples() {
        assertEquals(root.get("xml.content_1", "."), root.get("xml[0].content_1[0]", "."));
    }

    @Test
    public void singleEmptyElementHasEmptyStringValue() {
        root.get("xml.content_1.empty_element", ".").isPresent();
        assertEquals("", root.get("xml.content_1.empty_element", ".").asObject());
    }

    @Test
    public void multiEmptyElementHasEmptyStringValue() {
        assertEquals("", root.get("xml.content_1.multi_empty_element", ".").asObject());
    }

    @Test
    public void multiEmptyElementAsListAccessAllEmpty() {
        assertEquals("", root.get("xml.content_1.multi_empty_element[0]", ".").asObject());
        assertEquals("", root.get("xml.content_1.multi_empty_element[1]", ".").asObject());
        assertEquals("", root.get("xml.content_1.multi_empty_element[2]", ".").asObject());
        assertFalse(root.get("xml.content_1.multi_empty_element[3]", ".").isPresent());
    }

    @Test
    public void emptyWithAttrs() {
        assertEquals("", root.get("xml.content_1.just_attrs", ".").asObject());
        assertEquals("world", root.get("xml.content_1.just_attrs.hello", ".").asObject());
        assertEquals("bar", root.get("xml.content_1.just_attrs.foo", ".").asObject());
    }

    @Test
    public void nestedInt() {
        assertEquals("12345", root.get("xml.content_1.int_element", ".").asObject());
    }

    @Test
    public void nestedLargeDecimal() {
        assertEquals("12341234123412341234.334234", root.get("xml.content_1.large_decimal", ".").asObject());
    }

    @Test
    public void nestedString() {
        assertEquals("This is a 'String'", root.get("xml.content_2.string_element", ".").asObject());
    }

    @Test
    public void nestedMultipleDefaultsToFirstFound() {
        assertEquals("123", root.get("xml.content_2.multi_element", ".").asObject());
    }

    @Test
    public void defaultAttributeGetting() {
        assertEquals("true", root.get("xml.content_2.attribute_element", ".").get("is_that_right").asObject());
    }

    @Test
    public void explicitAttributeGetting() {
        assertEquals("true", root.get("xml.content_2.attribute_element", ".").get("@is_that_right").asObject());
        assertEquals("true", root.get("xml.content_2.attribute_element[0].@is_that_right", ".").asObject());
    }

    @Test
    public void elementAttributeNoClashGetting() {
        assertEquals("555", root.get("xml.content_2.attribute_element", ".").asObject());
    }

    @Test
    public void defaultGettingElementsOverrideAttributes() {
        assertEquals("young", root.get("xml.content_2.attr_clasher.age", ".").asObject());
    }

    @Test
    public void explicitGettingClashingElementsAndAttributse() {
        Dynamic attrClasher = root.get("xml.content_2.attr_clasher", ".");
        assertEquals("young", attrClasher.get("age").asObject());
        assertEquals("old", attrClasher.get("@age").asObject());
    }

    @Test
    public void equalsImplementation() {
        assertEquals(root, Dynamic.fromXml(XML));
    }

    @Test
    public void hashCodeImplementation() {
        assertEquals(root.hashCode(), Dynamic.fromXml(XML).hashCode());
    }

    @Test
    public void complexElementValueShouldBeStringContents() {
        assertThat(root.get("xml.content_2", ".").asString().replaceAll("\\s", ""),
            is(XML_CONTENT_2_GUTS.replaceAll("\\s", "")));
    }

    @Test
    public void toStringImplementation() {
        try {
            Diff diff = new Diff(XML, root.toString());
            assertTrue(diff.toString(), diff.identical());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("\""+ root.toString() + "\"" + " != \"" + XML_CONTENT_2_GUTS + "\"");
        }
    }

    @Test
    public void childToStringImplementation() {
        try {
            Diff diff = new Diff(XML_CONTENT_2, root.get("xml.content_2", ".").toString());
            assertTrue(diff.toString(), diff.identical());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("\""+ root.get("xml.content_2", ".").toString() + "\"" + " != \"" + XML_CONTENT_2_GUTS + "\"");
        }
    }

    @Test
    public void constructors() {
        assertThat(root, is(Dynamic.fromXml(new StringReader(XML))));
        assertThat(root, is(Dynamic.fromXml(new InputSource(new StringReader(XML)))));
    }
}
