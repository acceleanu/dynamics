/*
 * Copyright 2015 Alex Butler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package alexh.weak;

import static alexh.weak.DynamicChildLogic.using;
import alexh.LiteJoiner;
import java.util.stream.Stream;

class DynamicSomething extends AbstractDynamic<Object> implements Dynamic, Describer {

    public DynamicSomething(Object inner) {
        super(inner);
    }

    @Override
    public Dynamic get(Object key) {
        return new ParentAbsence.Barren<>(this, key);
    }

    @Override
    public String describe() {
        return inner.getClass().getSimpleName();
    }

    @Override
    protected Object keyLiteral() {
        return ROOT_KEY;
    }

    @Override
    public String toString() {
        return keyLiteral() + ":" + describe();
    }

    @Override
    public Stream<Dynamic> children() {
        return Stream.empty();
    }

    static class Child extends DynamicSomething implements DynamicChild {

        private final Dynamic parent;
        private final Object key;

        Child(Dynamic parent, Object key, Object inner) {
            super(inner);
            this.parent = parent;
            this.key = key;
        }

        @Override
        public Dynamic parent() {
            return parent;
        }

        @Override
        public Object keyLiteral() {
            return key;
        }

        @Override
        public String toString() {
            return LiteJoiner.on(ARROW).join(using(this).getAscendingKeyChainWithRoot()) + ":" +
                describe();
        }
    }
}
