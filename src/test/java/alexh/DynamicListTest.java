package alexh;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;
import alexh.weak.Dynamic;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.List;

public class DynamicListTest {

    private Dynamic dy;

    @Before
    public void setupMap() {
        dy = Dynamic.from(asList(
            new Fluent.HashMap<>()
                .append("mk1", "foo")
                .append("mk2", "bar"),
            "hello",
            new Object(),
            asList(1, 2, 3, null)
        ));
        assertNotNull(dy);
    }

    @Test
    public void stringIndex() {
        assertEquals("hello", dy.get("1").asObject());
    }

    @Test
    public void intIndex() {
        assertEquals("hello", dy.get(1).asObject());
    }

    @Test
    public void numberIndex() {
        assertEquals("hello", dy.get(1l).asObject());
        assertEquals("hello", dy.get(new BigDecimal("1")).asObject());
    }

    @Test
    public void children() {
        List<Dynamic> children = dy.children().collect(toList());
        Assertions.assertThat(children.get(1).asObject())
            .as(children.toString())
            .isEqualTo("hello");
        Assertions.assertThat(children.get(3).asObject())
            .as(children.toString())
            .isEqualTo(asList(1, 2, 3, null));
    }

    @Test
    public void numberIndex_notInt() {
        assertFalse(dy.get(Long.MAX_VALUE).isPresent());
        assertFalse(dy.get(Integer.MAX_VALUE + "12341234").isPresent());
    }

    @Test
    public void equalsImplementation() {
        assertEquals(Dynamic.from(asList(1, 2, 3, null)), Dynamic.from(asList(1, 2, 3, null)));
    }

    @Test
    public void hashCodeImplementation() {
        assertEquals(Dynamic.from(asList(1, 2, 3)).hashCode(), Dynamic.from(asList(1, 2, 3)).hashCode());
    }

    @Test
    public void toStringImplementation() {
        assertThat(dy.toString(), allOf(containsString("root"), containsString("0..3")));
        System.out.println("list dynamic toString: "+ dy);
    }

    @Test
    public void toStringImplementationSize0() {
        Dynamic dynamic = Dynamic.from(emptyList());
        assertThat(dynamic.toString(), containsString("Empty-List"));
        System.out.println("list dynamic toString: "+ dynamic);
    }

    @Test
    public void toStringImplementationSize1() {
        Dynamic dynamic = Dynamic.from(singletonList("foo"));
        assertThat(dynamic.toString(), containsString("[0]"));
        System.out.println("list dynamic toString: "+ dynamic);
    }

    @Test
    public void toStringImplementationSize2() {
        Dynamic dynamic = Dynamic.from(asList("foo", "bar"));
        assertThat(dynamic.toString(), containsString("[0, 1]"));
        System.out.println("list dynamic toString: "+ dynamic);
    }

    private final Dynamic dy2 = Dynamic.from(asList(
        new Fluent.HashMap<>()
            .append("mk1", "foo")
            .append("mk2", "bar"),
        "hello",
        new Object(),
        asList(1, 2, 3, null)
    ));

    @Test
    public void childEqualsImplementation() {
        assertEquals(dy.get(1).get("foo"), dy2.get(1).get("foo"));
        assertEquals(dy.get(3).get(1), dy2.get(3).get(1));

    }

    @Test
    public void convertedKeyEquals() {
        assertEquals("non-null value index conversion", dy.get(3).get("0"), dy2.get(3).get(0));
        assertEquals("null value index conversion", dy.get(3).get("3"), dy2.get(3).get(3));
        assertEquals("out-of-bounds value index conversion", dy.get(3).get("999"), dy2.get(3).get(999));
    }

    @Test
    public void childHashCodeImplementation() {
        assertEquals(dy.get(1).get("foo").hashCode(), dy2.get(1).get("foo").hashCode());
        assertEquals(dy.get(3).get(1).hashCode(), dy2.get(3).get(1).hashCode());
    }

    @Test
    public void convertedKeyHashCode() {
        assertEquals("non-null value index conversion", dy.get(3).get("0").hashCode(), dy2.get(3).get(0).hashCode());
        assertEquals("null value index conversion", dy.get(3).get("3").hashCode(), dy2.get(3).get(3).hashCode());
        assertEquals("out-of-bounds value index conversion", dy.get(3).get("999").hashCode(), dy2.get(3).get(999).hashCode());
    }

    @Test
    public void convertedPresentKeyShouldBeInteger() {
        assertTrue("non-converted", dy.get(3).key().is(Integer.class));
        assertTrue("converted", dy.get("3").key().is(Integer.class));
    }

    @Test
    public void childToStringImplementation() {
        Dynamic presentChild = dy.get(3);
        Dynamic absentChild = dy.get(1).get("foo");

        assertTrue("oh dear", presentChild.isPresent());
        assertFalse("oh dear", absentChild.isPresent());

        assertThat(presentChild.toString().toLowerCase(), allOf(containsString("root->3"), containsString("list")));

        assertThat(absentChild.toString().toLowerCase(), allOf(containsString("root"), containsString("1"),
            containsString("foo")));

        System.out.println("list-child dynamic toString: "+ presentChild);
    }
}
